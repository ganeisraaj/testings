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
 */
class GameService(private val rootService: RootService) : AbstractRefreshingService() {

    private fun requireGame(): Game =
        rootService.currentGame ?: throw IllegalArgumentException("No active game.")

    /**
     * Starts a new game.
     *
     * - 3 center cards
     * - 2 hidden + 3 open cards per player
     */
    fun startNewGame(playersNames: MutableList<String>, totalRounds: Int) {
        require(playersNames.size in 2..4) { "A game requires 2 to 4 players." }
        require(playersNames.all { it.isNotBlank() }) { "Player names must not be blank." }
        require(totalRounds > 0) { "Total rounds must be positive." }

        val players = playersNames.map { Player(name = it) }.toMutableList()

        val game = Game(
            totalRounds = totalRounds,
            currentRound = 1,
            currentPlayerIndex = 0,
            players = players
        )

        rootService.currentGame = game

        createDrawStack()

        // 3 center cards
        game.centerCards.clear()
        repeat(3) { game.centerCards.add(game.drawStack.pop()) }

        // 2 hidden cards per player
        repeat(2) {
            players.forEach { p -> p.hiddenCards.add(game.drawStack.pop()) }
        }

        // 3 open cards per player
        repeat(3) {
            players.forEach { p -> p.openCards.add(game.drawStack.pop()) }
        }

        updateLog("New game started with ${players.size} players and $totalRounds rounds.")
        onAllRefreshables { refreshAfterStartNewGame() }
    }

    /**
     * Ends the game and sends ranking to view layer.
     */
    fun endGame() {
        val game = requireGame()

        // evaluate all players before ranking
        game.players.forEach { evaluateCards(it) }

        val ranking = game.players.sortedByDescending { it.score.ordinal }

        rootService.currentGame = null
        onAllRefreshables { refreshAfterGameEnd(ranking) }
    }

    /**
     * Adds message to log and notifies GUI.
     */
    fun updateLog(message: String) {
        val game = requireGame()
        game.addLog(message)
        onAllRefreshables { refreshLog(message) }
    }

    /**
     * Refills draw stack from discard stack (spec rule).
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
     * Evaluates 5-card poker hand.
     */
    fun evaluateCards(player: Player) {
        requireGame()

        val hand = player.hiddenCards + player.openCards
        if (hand.size != 5) {
            player.score = ScoreTable.NONE
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
     * Ends the current turn.
     * Triggers turn-end refresh and transitions to startTurn.
     */
    fun endTurn() {
        val game = requireGame()
        onAllRefreshables { refreshAfterTurnEnd() }
        startTurn()
    }

    /**
     * Starts the turn for the next player.
     * Updates player index, handles round increments, and resets actions.
     */
    fun startTurn() {
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

        updateLog("Turn started for ${game.players[newIndex].name}")
        onAllRefreshables { refreshAfterTurnStart() }
    }

    /**
     * Creates and shuffles the draw stack.
     */
    fun createDrawStack() {
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

    private fun isStraight(sortedRanks: List<Int>): Boolean {
        val distinct = sortedRanks.distinct()
        if (distinct.size != 5) return false

        val isNormal = distinct.zipWithNext().all { (a, b) -> b == a + 1 }
        if (isNormal) return true

        return distinct == listOf(2, 3, 4, 5, 14)
    }

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