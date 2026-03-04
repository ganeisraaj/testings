package service

import entity.Card
import entity.CardSuit
import entity.CardValue
import entity.Game
import entity.Player
import entity.ScoreTable
import kotlin.random.Random

/**
 * Service for the main game logic and flow.
 * This class handles starting games, dealing cards, and switching turns.
 */
class GameService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Initializes the service. 
     * Part of the required UML structure.
     */
    fun init() {
        // Preparation logic could go here.
    }

    /**
     * Finds the current game or stops the program with an error.
     */
    private fun requireGame(): Game {
        val currentGame: Game? = rootService.currentGame
        if (currentGame == null) {
            throw IllegalArgumentException("There is no active game.")
        }
        return currentGame
    }

    /**
     * Sets up a brand new game session.
     * It assigns player names, sets the rounds, shuffles the deck, and deals the cards.
     */
    fun startNewGame(playersNames: MutableList<String>, totalRounds: Int) {
        // Validation of input
        if (playersNames.size < 2) {
            throw IllegalArgumentException("At least 2 players are needed.")
        }
        if (playersNames.size > 4) {
            throw IllegalArgumentException("Maximum 4 players allowed.")
        }
        
        for (i in 0 until playersNames.size) {
            val name: String = playersNames[i]
            if (name.isBlank()) {
                throw IllegalArgumentException("Names cannot be empty.")
            }
        }
        
        if (totalRounds < 1) {
            throw IllegalArgumentException("Total rounds must be at least 1.")
        }

        // Creating player objects
        val gamePlayers: MutableList<Player> = mutableListOf<Player>()
        for (i in 0 until playersNames.size) {
            val name: String = playersNames[i]
            val player: Player = Player(name = name)
            gamePlayers.add(player)
        }

        // Creating the main game state
        val newGame: Game = Game(
            totalRounds = totalRounds,
            currentRound = 1,
            currentPlayerIndex = 0,
            players = gamePlayers
        )
        rootService.currentGame = newGame
        
        // Shuffle the deck of 52 cards
        createDrawStack()

        // Place 3 cards into the middle area
        val middle: MutableList<Card> = newGame.centerCards
        middle.clear()
        for (i in 1..3) {
            val card: Card = newGame.drawStack.pop()
            middle.add(card)
        }

        // Deal cards to every player
        for (i in 0 until gamePlayers.size) {
            val p: Player = gamePlayers[i]
            // Two cards that stay hidden
            for (j in 1..2) {
                val card: Card = newGame.drawStack.pop()
                p.hiddenCards.add(card)
            }
            // Three cards that are visible
            for (j in 1..3) {
                val card: Card = newGame.drawStack.pop()
                p.openCards.add(card)
            }
            // Everyone starts with 2 actions
            p.actionsLeft = 2
        }

        // Log the start event
        val logMsg: String = "A new game has started with " + gamePlayers.size + " players."
        updateLog(logMsg)
        
        // Refresh the visual display
        onAllRefreshables { refreshAfterStartNewGame() }
    }

    /**
     * Finishes the game and determines the winners.
     */
    fun endGame() {
        val currentGame: Game = requireGame()

        // Evaluate the hand for every single player
        for (i in 0 until currentGame.players.size) {
            val p: Player = currentGame.players[i]
            evaluateCards(p)
        }

        // Build a list to sort for the scoreboard
        val ranking: MutableList<Player> = mutableListOf<Player>()
        for (i in 0 until currentGame.players.size) {
            ranking.add(currentGame.players[i])
        }
        
        // Use a simple bubble sort to rank the players by their hand's value
        for (i in 0 until ranking.size - 1) {
            for (j in 0 until ranking.size - i - 1) {
                val score1: Int = ranking[j].score.ordinal
                val score2: Int = ranking[j + 1].score.ordinal
                if (score1 < score2) {
                    val temp: Player = ranking[j]
                    ranking[j] = ranking[j + 1]
                    ranking[j + 1] = temp
                }
            }
        }

        // Reset the current game to end the session
        rootService.currentGame = null
        onAllRefreshables { refreshAfterGameEnd(ranking) }
    }

    /**
     * Adds a text message to the game log and notifies the UI.
     */
    fun updateLog(message: String) {
        val currentGame: Game = requireGame()
        currentGame.addLog(message)
        onAllRefreshables { refreshLog(message) }
    }

    /**
     * Creates a full deck of 52 cards and shuffles them randomly.
     */
    private fun createDrawStack() {
        val currentGame: Game = requireGame()
        val cardList: MutableList<Card> = mutableListOf<Card>()

        // Generate all possible cards
        val suits: Array<CardSuit> = CardSuit.entries.toTypedArray()
        val values: Array<CardValue> = CardValue.entries.toTypedArray()
        
        for (s in 0 until suits.size) {
            val currentSuit: CardSuit = suits[s]
            for (v in 0 until values.size) {
                val currentValue: CardValue = values[v]
                val card: Card = Card(currentSuit, currentValue)
                cardList.add(card)
            }
        }

        // Randomly mix the card order
        val seed: Long = System.nanoTime()
        cardList.shuffle(Random(seed))
        
        // Fill the game's draw stack
        currentGame.drawStack.clear()
        for (i in 0 until cardList.size) {
            val card: Card = cardList[i]
            currentGame.drawStack.add(card)
        }
    }

    /**
     * Takes the cards from the discard pile, shuffles them, and puts them back into the deck.
     */
    fun refillDrawStack() {
        val currentGame: Game = requireGame()
        if (currentGame.discardStack.isEmpty()) {
            throw IllegalStateException("Discard stack is empty. Cannot refill the deck.")
        }

        val temp: MutableList<Card> = mutableListOf<Card>()
        while (currentGame.discardStack.isNotEmpty()) {
            val card: Card = currentGame.discardStack.pop()
            temp.add(card)
        }

        val seed: Long = System.nanoTime()
        temp.shuffle(Random(seed))
        
        for (i in 0 until temp.size) {
            val card: Card = temp[i]
            currentGame.drawStack.add(card)
        }
        
        updateLog("The deck was refilled from the discard pile.")
    }

    /**
     * Determines which poker hand combination the player currently has.
     * The result is stored directly inside the player's score property.
     */
    fun evaluateCards(player: Player) {
        requireGame()
        
        // Pick up all 5 cards of the player
        val hand: MutableList<Card> = mutableListOf<Card>()
        for (i in 0 until player.hiddenCards.size) {
            hand.add(player.hiddenCards[i])
        }
        for (i in 0 until player.openCards.size) {
            hand.add(player.openCards[i])
        }
        
        // We must have exactly 5 cards to check
        if (hand.size != 5) {
            player.score = ScoreTable.NONE
            return
        }

        // Turn card values into numbers (2..14)
        val rankNumbers: MutableList<Int> = mutableListOf<Int>()
        for (i in 0 until hand.size) {
            val card: Card = hand[i]
            val value: Int = getCardRank(card.value)
            rankNumbers.add(value)
        }
        
        // Sort the numbers from lowest to highest using bubble sort
        for (i in 0 until rankNumbers.size - 1) {
            for (j in 0 until rankNumbers.size - i - 1) {
                if (rankNumbers[j] > rankNumbers[j + 1]) {
                    val tempValue: Int = rankNumbers[j]
                    rankNumbers[j] = rankNumbers[j + 1]
                    rankNumbers[j + 1] = tempValue
                }
            }
        }

        // Check for a FLUSH: Do all cards have the same suit?
        var flushFlag: Boolean = true
        val suitZero: CardSuit = hand[0].suit
        for (i in 0 until hand.size) {
            if (hand[i].suit != suitZero) {
                flushFlag = false
            }
        }

        // Check for a STRAIGHT: Are the cards in numerical order?
        val straightFlag: Boolean = checkStraight(rankNumbers)
        
        // Count how many cards exist for each rank (e.g., three Jacks)
        val countStore: MutableMap<Int, Int> = mutableMapOf<Int, Int>()
        for (i in 0 until rankNumbers.size) {
            val r: Int = rankNumbers[i]
            val oldVal: Int? = countStore[r]
            if (oldVal == null) {
                countStore[r] = 1
            } else {
                countStore[r] = oldVal + 1
            }
        }
        
        // Collect the counts and sort them descending (e.g. 3, 1, 1)
        val sortedCounts: MutableList<Int> = mutableListOf<Int>()
        for (c in countStore.values) {
            sortedCounts.add(c)
        }
        for (i in 0 until sortedCounts.size - 1) {
            for (j in 0 until sortedCounts.size - i - 1) {
                if (sortedCounts[j] < sortedCounts[j + 1]) {
                    val tempC: Int = sortedCounts[j]
                    sortedCounts[j] = sortedCounts[j + 1]
                    sortedCounts[j + 1] = tempC
                }
            }
        }

        // Assign the poker rank based on the found flags and counts
        var result: ScoreTable = ScoreTable.HIGHCARD

        // Four of a kind (4, 1)
        if (sortedCounts.size == 2 && sortedCounts[0] == 4) {
            result = ScoreTable.FOUROFAKIND
        }
        // Full house (3, 2)
        else if (sortedCounts.size == 2 && sortedCounts[0] == 3) {
            result = ScoreTable.FULLHOUSE
        }
        // Three of a kind (3, 1, 1)
        else if (sortedCounts.size == 3 && sortedCounts[0] == 3) {
            result = ScoreTable.SET
        }
        // Two pairs (2, 2, 1)
        else if (sortedCounts.size == 3 && sortedCounts[0] == 2) {
            result = ScoreTable.TWOPAIR
        }
        // One pair (2, 1, 1, 1)
        else if (sortedCounts.size == 4 && sortedCounts[0] == 2) {
            result = ScoreTable.PAIR
        }

        // Specials for Straights and Flushes
        if (straightFlag && flushFlag) {
            // Check for Royal Flush (10, 11, 12, 13, 14)
            if (rankNumbers[0] == 10 && rankNumbers[4] == 14) {
                result = ScoreTable.ROYALFLUSH
            } else {
                result = ScoreTable.STRAIGHTFLUSH
            }
        } else if (flushFlag) {
            // If we have a flush but not a straight, and no four/full-house found yet
            if (result.ordinal < ScoreTable.FLUSH.ordinal) {
               result = ScoreTable.FLUSH
            }
        } else if (straightFlag) {
            // If we have a straight but not a flush
            if (result.ordinal < ScoreTable.STRAIGHT.ordinal) {
                result = ScoreTable.STRAIGHT
            }
        }

        player.score = result
    }

    /**
     * Helper to verify if ranks are consecutive.
     */
    private fun checkStraight(sorted: List<Int>): Boolean {
        // Normal check for incremental cards
        var isNormal: Boolean = true
        for (i in 0 until sorted.size - 1) {
            if (sorted[i + 1] != sorted[i] + 1) {
                isNormal = false
            }
        }
        if (isNormal) {
            return true
        }

        // Ace-low straight (A, 2, 3, 4, 5) -> In numbers: 2, 3, 4, 5, 14
        if (sorted[0] == 2 && sorted[1] == 3 && sorted[2] == 4 && sorted[3] == 5 && sorted[4] == 14) {
             return true
        }
        
        return false
    }

    /**
     * Translates the value enum into a comparable number.
     */
    private fun getCardRank(v: CardValue): Int {
        if (v == CardValue.TWO) return 2
        if (v == CardValue.THREE) return 3
        if (v == CardValue.FOUR) return 4
        if (v == CardValue.FIVE) return 5
        if (v == CardValue.SIX) return 6
        if (v == CardValue.SEVEN) return 7
        if (v == CardValue.EIGHT) return 8
        if (v == CardValue.NINE) return 9
        if (v == CardValue.TEN) return 10
        if (v == CardValue.JACK) return 11
        if (v == CardValue.QUEEN) return 12
        if (v == CardValue.KING) return 13
        if (v == CardValue.ACE) return 14
        return 0
    }

    /**
     * Moves the game focus to the next player. 
     * Handles round incrementing when full circle reached.
     */
    fun startTurn() {
        val currentGame: Game = requireGame()
        
        var nextIdx: Int = currentGame.currentPlayerIndex + 1
        if (nextIdx >= currentGame.players.size) {
            nextIdx = 0
            // Reset to first player means a new round starts
            currentGame.currentRound = currentGame.currentRound + 1
        }
        
        currentGame.currentPlayerIndex = nextIdx

        // Check if the game is over
        if (currentGame.currentRound > currentGame.totalRounds) {
            endGame()
            return
        }

        // Each player gets 2 new actions
        val p: Player = currentGame.players[nextIdx]
        p.actionsLeft = 2

        val logMsg: String = "It is now the turn of " + p.name + "."
        updateLog(logMsg)
        
        onAllRefreshables { refreshAfterTurnStart() }
    }

    /**
     * Helper method to trigger the switch of turns.
     */
    fun endTurn() {
        startTurn()
        onAllRefreshables { refreshAfterTurnEnd() }
    }

}