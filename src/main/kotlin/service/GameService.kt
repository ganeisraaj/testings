package service

import entity.Card
import entity.CardSuit
import entity.CardValue
import entity.Game
import entity.Player
import entity.ScoreTable
import java.util.Stack
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
     * - Each player receives exactly 5 hidden cards (temporary initial distribution).
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

        // UML: createDrawStack()
        createDrawStack()

        // UML: centerCards has exactly 3 cards
        game.centerCards.clear()
        repeat(3) { game.centerCards.add(game.drawStack.pop()) }

        // Initial hidden cards (5 each) for development/testing
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
     * - A ranking is computed (temporary: by ordinal of [ScoreTable]).
     * - [RootService.currentGame] is set to null.
     * - [Refreshable.refreshAfterGameEnd] is called.
     *
     * @throws IllegalArgumentException if no game is active.
     */
    fun endGame() {
        val game = rootService.currentGame ?: throw IllegalArgumentException("No active game.")

        val ranking = game.players
            .sortedByDescending { it.score.ordinal }

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
        val game = rootService.currentGame ?: throw IllegalArgumentException("No active game.")
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
     *
     * @throws IllegalArgumentException if no game is active.
     * @throws IllegalArgumentException if discardStack is empty.
     */
    fun refillDrawStack() {
        val game = rootService.currentGame ?: throw IllegalArgumentException("No active game.")
        require(game.discardStack.isNotEmpty()) { "Discard stack is empty." }

        while (game.discardStack.isNotEmpty()) {
            game.drawStack.push(game.discardStack.pop())
        }
    }

    /**
     * Evaluates the given player's cards and updates the player's [ScoreTable].
     *
     * This is a placeholder evaluation (UML requires the method to exist).
     * Full poker evaluation logic will be implemented later.
     *
     * Preconditions:
     * - A game must be active.
     *
     * Postconditions:
     * - player.score is updated (temporary: HIGHCARD if player has any cards, else NONE).
     *
     * @param player The player whose cards are evaluated.
     *
     * @throws IllegalArgumentException if no game is active.
     */
    fun evaluateCards(player: Player) {
        rootService.currentGame ?: throw IllegalArgumentException("No active game.")
        player.score = if (player.hiddenCards.isEmpty() && player.openCards.isEmpty()) {
            ScoreTable.NONE
        } else {
            ScoreTable.HIGHCARD
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
     * - The next player's actionsLeft is reset to 2.
     * - refreshAfterTurnEnd + refreshAfterTurnStart are called.
     *
     * @throws IllegalArgumentException if no game is active.
     * @throws IllegalArgumentException if invalid player count.
     */
    fun endTurn() {
        val game = rootService.currentGame ?: throw IllegalArgumentException("No active game.")
        require(game.players.size >= 2) { "Game has an invalid number of players." }

        val oldIndex = game.currentPlayerIndex
        val newIndex = (oldIndex + 1) % game.players.size
        game.currentPlayerIndex = newIndex

        if (newIndex == 0) {
            game.currentRound += 1
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
     */
    private fun createDrawStack() {
        val game = rootService.currentGame ?: throw IllegalArgumentException("No active game.")

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
}