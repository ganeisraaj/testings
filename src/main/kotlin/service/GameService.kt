package service

import entity.Card
import entity.CardSuit
import entity.CardValue
import entity.Game
import entity.Player
import entity.ScoreTable
import kotlin.random.Random

/**
 * Service responsible for managing the global game flow according to the class diagram.
 *
 * This class belongs to the service layer and contains game logic that modifies the entity layer.
 * It operates on the currently active [Game] stored in [RootService.currentGame].
 *
 * Responsibilities (UML):
 * - startNewGame(playersNames, totalRounds)
 * - endGame()
 * - updateLog(message)
 * - refillDrawStack()
 * - evaluateCards(player)
 * - endTurn()
 *
 * @property rootService Reference to the central [RootService].
 */
class GameService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Returns the currently active game.
     *
     * @throws IllegalArgumentException if no game is active.
     */
    private fun requireGame(): Game =
        rootService.currentGame ?: throw IllegalArgumentException("No active game.")

    /**
     * Starts a new game with the given player names and selected total rounds.
     *
     * Preconditions:
     * - playerNames.size in 2..4
     * - all names are non-blank
     * - totalRounds > 0
     *
     * Postconditions:
     * - [RootService.currentGame] is initialized with:
     *   - currentRound = 1
     *   - totalRounds = totalRounds
     *   - currentPlayerIndex = 0
     * - A shuffled drawStack is created.
     * - centerCards contains exactly 3 cards (drawn from drawStack).
     * - Each player receives exactly 5 hidden cards.
     * - Log + refresh callbacks are triggered.
     *
     * @param playerNames Player names (2..4 players).
     * @param totalRounds Total number of rounds for the game.
     *
     * @throws IllegalArgumentException if preconditions are violated.
     */
    fun startNewGame(playerNames: MutableList<String>, totalRounds: Int) {
        require(playerNames.size in 2..4) { "A game requires 2 to 4 players." }
        require(playerNames.all { it.isNotBlank() }) { "Player names must not be blank." }
        require(totalRounds > 0) { "Total rounds must be positive." }

        val players = playerNames.map { Player(name = it) }.toMutableList()

        val game = Game(
            totalRounds = totalRounds,
            currentRound = 1,
            currentPlayerIndex = 0,
            players = players
        )

        rootService.currentGame = game

        createDrawStack()

        game.centerCards.clear()
        repeat(3) { game.centerCards.add(game.drawStack.pop()) }

        repeat(5) {
            players.forEach { p -> p.hiddenCards.add(game.drawStack.pop()) }
        }

        updateLog("New game started with ${players.size} players and $totalRounds rounds.")
        onAllRefreshables { refreshAfterStartNewGame() }
    }

    /**
     * Ends the currently active game.
     *
     * Preconditions:
     * - A game must be active.
     *
     * Postconditions:
     * - A ranking is computed by score strength (based on [ScoreTable] ordinal).
     * - [RootService.currentGame] is set to null.
     * - [Refreshable.refreshAfterGameEnd] is called.
     *
     * @throws IllegalArgumentException if no game is active.
     */
    fun endGame() {
        val game = requireGame()

        val ranking = game.players.sortedByDescending { it.score.ordinal }

        rootService.currentGame = null
        onAllRefreshables { refreshAfterGameEnd(ranking) }
    }

    /**
     * Appends a message to the game log and notifies all refreshables.
     *
     * Preconditions:
     * - A game must be active.
     *
     * Postconditions:
     * - message is appended to game.log
     * - [Refreshable.refreshLog] is called with the message
     *
     * @param message The log message to append.
     *
     * @throws IllegalArgumentException if no game is active.
     */
    fun updateLog(message: String) {
        val game = requireGame()
        game.addLog(message)
        onAllRefreshables { refreshLog(message) }
    }

    /**
     * Refills the draw stack from the discard stack.
     *
     * Preconditions:
     * - A game must be active.
     * - discardStack is not empty.
     *
     * Postconditions:
     * - All cards from discardStack are moved to drawStack.
     * - The moved cards are shuffled before being added.
     *
     * @throws IllegalArgumentException if no game is active.
     * @throws IllegalArgumentException if discardStack is empty.
     */
    fun refillDrawStack() {
        val game = requireGame()
        require(game.discardStack.isNotEmpty()) { "Discard stack is empty." }

        val refillCards = mutableListOf<Card>()
        while (game.discardStack.isNotEmpty()) {
            refillCards.add(game.discardStack.pop())
        }

        refillCards.shuffle(Random(System.nanoTime()))
        game.drawStack.addAll(refillCards)
    }

    /**
     * Evaluates the given player's hand and updates the player's [ScoreTable].
     *
     * The evaluation is based on the player's private cards:
     * - hiddenCards (2) + openCards (3) = 5-card hand
     *
     * Preconditions:
     * - A game must be active.
     *
     * Postconditions:
     * - player.score is set according to standard 5-card poker ranking:
     *   NONE, HIGHCARD, PAIR, TWOPAIR, SET, STRAIGHT, FLUSH, FULLHOUSE,
     *   FOUROFAKIND, STRAIGHTFLUSH, ROYALFLUSH
     *
     * If the player does not currently hold 5 cards in total, the method falls back to:
     * - NONE if the player has no cards at all
     * - HIGHCARD otherwise
     *
     * @param player The player whose cards are evaluated.
     *
     * @throws IllegalArgumentException if no game is active.
     */
    fun evaluateCards(player: Player) {
        requireGame()

        val hand = (player.hiddenCards + player.openCards)
        if (hand.isEmpty()) {
            player.score = ScoreTable.NONE
            return
        }
        if (hand.size != 5) {
            player.score = ScoreTable.HIGHCARD
            return
        }

        val isFlush = hand.all { it.suit == hand.first().suit }

        val ranks = hand.map { it.value.toRank() }.sorted()
        val rankCounts = ranks.groupingBy { it }.eachCount()
        val countsDesc = rankCounts.values.sortedDescending()

        val isStraight = isStraight(ranks)
        val isRoyal = isFlush && isStraight && ranks == listOf(10, 11, 12, 13, 14)

        player.score = when {
            isRoyal -> ScoreTable.ROYALFLUSH
            isFlush && isStraight -> ScoreTable.STRAIGHTFLUSH
            countsDesc == listOf(4, 1) -> ScoreTable.FOUROFAKIND
            countsDesc == listOf(3, 2) -> ScoreTable.FULLHOUSE
            isFlush -> ScoreTable.FLUSH
            isStraight -> ScoreTable.STRAIGHT
            countsDesc == listOf(3, 1, 1) -> ScoreTable.SET
            countsDesc == listOf(2, 2, 1) -> ScoreTable.TWOPAIR
            countsDesc == listOf(2, 1, 1, 1) -> ScoreTable.PAIR
            else -> ScoreTable.HIGHCARD
        }
    }

    /**
     * Ends the current player's turn and switches to the next player.
     *
     * Preconditions:
     * - A game must be active.
     * - The game must have at least 2 players.
     *
     * Postconditions:
     * - currentPlayerIndex advances by 1 (mod number of players).
     * - If the index wraps to 0, currentRound increases by 1.
     * - If currentRound exceeds totalRounds after incrementing, the game ends.
     * - Otherwise, the next player's actionsLeft is reset to 2.
     * - refreshAfterTurnEnd + refreshAfterTurnStart are called.
     *
     * @throws IllegalArgumentException if no game is active.
     * @throws IllegalArgumentException if invalid player count.
     */
    fun endTurn() {
        val game = requireGame()
        require(game.players.size >= 2) { "Game has an invalid number of players." }

        val oldIndex = game.currentPlayerIndex
        val newIndex = (oldIndex + 1) % game.players.size
        game.currentPlayerIndex = newIndex

        if (newIndex == 0) {
            game.currentRound += 1
            if (game.currentRound > game.totalRounds) {
                endGame()
                return
            }
        }

        game.players[newIndex].actionsLeft = 2

        updateLog("Turn ended. Next player: ${game.players[newIndex].name}")
        onAllRefreshables { refreshAfterTurnEnd() }
        onAllRefreshables { refreshAfterTurnStart() }
    }

    /**
     * Creates and shuffles the draw stack for the currently active game.
     *
     * UML: createDrawStack() is private.
     *
     * Preconditions:
     * - A game must be active.
     *
     * Postconditions:
     * - drawStack contains 52 shuffled cards.
     *
     * @throws IllegalArgumentException if no game is active.
     */
    private fun createDrawStack() {
        val game = requireGame()

        val cards = mutableListOf<Card>()
        for (suit in CardSuit.entries) {
            for (value in CardValue.entries) {
                cards.add(Card(suit, value))
            }
        }
        cards.shuffle(Random(System.nanoTime()))

        game.drawStack.clear()
        game.drawStack.addAll(cards)
    }

    /**
     * Returns whether the given sorted ranks represent a straight.
     *
     * Supports the special case A-2-3-4-5.
     */
    private fun isStraight(sortedRanks: List<Int>): Boolean {
        val distinct = sortedRanks.distinct()
        if (distinct.size != 5) return false

        val isNormal = distinct.zipWithNext().all { (a, b) -> b == a + 1 }
        if (isNormal) return true

        return distinct == listOf(2, 3, 4, 5, 14)
    }

    /**
     * Maps [CardValue] to a numeric rank used for hand evaluation.
     */
    private fun CardValue.toRank(): Int = when (this) {
        CardValue.TWO -> 2
        CardValue.THREE -> 3
        CardValue.FOUR -> 4
        CardValue.FIVE -> 5
        CardValue.SIX -> 6
        CardValue.SEVEN -> 7
        CardValue.EIGHT -> 8
        CardValue.NINE -> 9
        CardValue.TEN -> 10
        CardValue.JACK -> 11
        CardValue.QUEEN -> 12
        CardValue.KING -> 13
        CardValue.ACE -> 14
    }
}