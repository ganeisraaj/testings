package service

import entity.Card
import entity.CardSuit
import entity.CardValue
import entity.Game
import entity.Player
import entity.ScoreTable
import kotlin.random.Random

/**
 * Service class that manages the overall game flow. 
 * Handles starting and ending games and moving through turns.
 */
class GameService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Initialization method required by the UML diagram.
     * Currently not used for specific logic.
     */
    fun init() {
        // Current initialization logic if needed
    }

    private fun requireGame(): Game =
        rootService.currentGame ?: throw IllegalArgumentException("No active game.")

    /**
     * Starts a new game. Sets up players, creates the deck, and
     * deals the initial hidden and open cards.
     */
    fun startNewGame(playersNames: MutableList<String>, totalRounds: Int) {
        // Basic validation
        if (playersNames.size < 2 || playersNames.size > 4) {
            throw IllegalArgumentException("Game requires 2-4 players.")
        }
        for (name in playersNames) {
            if (name.isBlank()) throw IllegalArgumentException("Player names cannot be blank.")
        }

        val gamePlayers = mutableListOf<Player>()
        for (name in playersNames) {
            gamePlayers.add(Player(name = name))
        }

        val newGame = Game(
            totalRounds = totalRounds,
            currentRound = 1,
            currentPlayerIndex = 0,
            players = gamePlayers
        )

        rootService.currentGame = newGame
        
        // Setup game components
        createDrawStack()

        // Distribute cards to center
        val center = newGame.centerCards
        center.clear()
        for (i in 1..3) {
            center.add(newGame.drawStack.pop())
        }

        // Deal cards to players
        for (p in gamePlayers) {
            for (i in 1..2) p.hiddenCards.add(newGame.drawStack.pop())
            for (i in 1..3) p.openCards.add(newGame.drawStack.pop())
            p.actionsLeft = 2
        }

        updateLogMessage("New game started with ${gamePlayers.size} players.")
        onAllRefreshables { it.refreshAfterStartNewGame() }
    }

    /**
     * Wraps up the current game session. Evaluates the hands 
     * of all players and shows the final ranking.
     */
    fun endGame() {
        val currentGame = requireGame()

        // Evaluate all players before finishing
        for (player in currentGame.players) {
            player.score = evaluateCards(player)
        }

        // Sort by score strength. Ties are kept as is (Shared Ranks handle them in GUI).
        val finalRanking = currentGame.players.sortedByDescending { it.score.ordinal }

        rootService.currentGame = null
        onAllRefreshables { it.refreshAfterGameEnd(finalRanking) }
    }

    /**
     * Helper method to send messages to the GUI log.
     */
    fun updateLogMessage(message: String) {
        val currentGame = requireGame()
        currentGame.addLog(message)
        onAllRefreshables { it.refreshLog(message) }
    }

    /**
     * Generates a 52-card deck and shuffles it.
     */
    fun createDrawStack() {
        val currentGame = requireGame()
        val cards = mutableListOf<Card>()

        for (suit in CardSuit.entries) {
            for (value in CardValue.entries) {
                cards.add(Card(suit, value))
            }
        }

        cards.shuffle(Random(System.nanoTime()))
        
        currentGame.drawStack.clear()
        for (card in cards) {
            currentGame.drawStack.add(card)
        }
    }

    /**
     * Logic to refill the draw stack from the discard pile 
     * when no cards are left to draw.
     */
    fun refillDrawStack() {
        val currentGame = requireGame()
        if (currentGame.discardStack.isEmpty()) {
            throw IllegalStateException("No cards left in discard stack to refill.")
        }

        val cardsToReshuffle = mutableListOf<Card>()
        while (currentGame.discardStack.isNotEmpty()) {
            cardsToReshuffle.add(currentGame.discardStack.pop())
        }

        cardsToReshuffle.shuffle(Random(System.nanoTime()))
        for (card in cardsToReshuffle) {
            currentGame.drawStack.add(card)
        }
        
        updateLogMessage("The draw stack was empty and has been refilled from the discard pile.")
    }

    /**
     * Evaluates a player's hand and returns the poker category.
     * // Note: Kicker logic is not needed because identical categories
     * // result in a draw according to project rules.
     */
    fun evaluateCards(player: Player): ScoreTable {
        val allCards = player.hiddenCards + player.openCards
        if (allCards.size != 5) return ScoreTable.NONE

        val ranks = mutableListOf<Int>()
        for (card in allCards) {
            ranks.add(getCardRank(card.value))
        }
        ranks.sort()

        val isFlush = allCards.all { it.suit == allCards[0].suit }
        val isStraight = checkStraight(ranks)
        
        // Count frequencies
        val countsMap = mutableMapOf<Int, Int>()
        for (r in ranks) {
            countsMap[r] = (countsMap[r] ?: 0) + 1
        }
        val counts = countsMap.values.sortedDescending()

        return when {
            isFlush && isStraight && ranks == listOf(10, 11, 12, 13, 14) -> ScoreTable.ROYALFLUSH
            isFlush && isStraight -> ScoreTable.STRAIGHTFLUSH
            counts == listOf(4, 1) -> ScoreTable.FOUROFAKIND
            counts == listOf(3, 2) -> ScoreTable.FULLHOUSE
            isFlush -> ScoreTable.FLUSH
            isStraight -> ScoreTable.STRAIGHT
            counts == listOf(3, 1, 1) -> ScoreTable.SET
            counts == listOf(2, 2, 1) -> ScoreTable.TWOPAIR
            counts == listOf(2, 1, 1, 1) -> ScoreTable.PAIR
            else -> ScoreTable.HIGHCARD
        }
    }

    private fun checkStraight(sortedRanks: List<Int>): Boolean {
        // Normal straight
        var normal = true
        for (i in 0 until sortedRanks.size - 1) {
            if (sortedRanks[i + 1] != sortedRanks[i] + 1) {
                normal = false
                break
            }
        }
        if (normal) return true

        // Steel wheel
        return sortedRanks == listOf(2, 3, 4, 5, 14)
    }

    private fun getCardRank(value: CardValue): Int = when (value) {
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

    /**
     * Progresses the game to the next player and handles round counting.
     */
    fun startTurn() {
        val currentGame = requireGame()
        val nextIdx = (currentGame.currentPlayerIndex + 1) % currentGame.players.size
        currentGame.currentPlayerIndex = nextIdx

        // Round handling: starts over when player 1 is active again
        if (nextIdx == 0) {
            currentGame.currentRound += 1
            if (currentGame.currentRound > currentGame.totalRounds) {
                endGame()
                return
            }
        }

        val activePlayer = currentGame.players[nextIdx]
        activePlayer.actionsLeft = 2

        updateLogMessage("Turn started for ${activePlayer.name}.")
        onAllRefreshables { it.refreshAfterTurnStart() }
    }

    /**
     * Finishes a player's turn and triggers the transition to the next player.
     */
    fun endTurn() {
        onAllRefreshables { it.refreshAfterTurnEnd() }
        startTurn()
    }
}