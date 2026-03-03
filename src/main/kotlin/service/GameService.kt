package service

import entity.Card
import entity.CardSuit
import entity.CardValue
import entity.Game
import entity.Player
import entity.ScoreTable
import kotlin.random.Random

/**
 * This is my GameService class. It handles the general stuff like starting a game,
 * ending it, and keeping track of the turn flow. I followed the UML exactly.
 */
class GameService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * I added this init method because it was marked in the UML diagram.
     * Right now it doesn't do much, but it's there for compliance.
     */
    fun init() {
        // Current initialization logic if needed
    }

    private fun requireGame(): Game =
        rootService.currentGame ?: throw IllegalArgumentException("No active game.")

    /**
     * Starts the whole game. It sets up the players, creates the deck, and
     * gives everyone their starting cards (2 hidden, 3 open).
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
     * This method wraps up the game. It evaluates everyone's hands and 
     * then shows the final ranking.
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
     * Helper to show messages in the GUI log. 
     * I call this whenever a player does something important.
     */
    fun updateLogMessage(message: String) {
        val currentGame = requireGame()
        currentGame.addLog(message)
        onAllRefreshables { it.refreshLog(message) }
    }

    /**
     * This makes a deck with all 52 cards and shuffles it. 
     * I use nanoTime to make the random seed a bit better.
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
     * If we run out of cards to draw, we take the discard pile,
     * shuffle it, and it becomes the new draw stack.
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
     * This checks what kind of poker hand a player has.
     * // Note: I skipped the kicker logic here because the rules say 
     * // that if the categories are the same, it's just a draw.
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
     * Moves the game to the next player. It also handles round counting 
     * and resets the actions back to 2 for the new player.
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
     * Just finishes the current turn. It signals the GUI and then
     * calls startTurn to keep the loop going.
     */
    fun endTurn() {
        onAllRefreshables { it.refreshAfterTurnEnd() }
        startTurn()
    }
}