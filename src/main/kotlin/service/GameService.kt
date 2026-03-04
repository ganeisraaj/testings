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
     * Finds the current game or stops the program with an error.
     */
    private fun requireGame(): Game {
        val currentGame: Game? = rootService.currentGame
        if (currentGame == null) {
            throw IllegalArgumentException("There is no active game.")
        } else {
            return currentGame
        }
    }

    /**
     * Starts a new game with the given players and round count.
     */
    fun startNewGame(playersNames: MutableList<String>, totalRounds: Int) {
        // Validation of input - check player count
        val playerCount: Int = playersNames.size
        if (playerCount < 2) {
            throw IllegalArgumentException("At least 2 players are needed.")
        }
        if (playerCount > 4) {
            throw IllegalArgumentException("Maximum 4 players allowed.")
        }

        // Loop through names to check for blanks
        for (i in 0 until playerCount) {
            val nameStr: String = playersNames[i]
            if (nameStr.isBlank()) {
                throw IllegalArgumentException("Names cannot be empty.")
            }
        }

        // Build a normalized list manually without .map
        val normalizedNames: MutableList<String> = mutableListOf<String>()
        for (i in 0 until playersNames.size) {
            val trimmed: String = playersNames[i].trim().lowercase()
            normalizedNames.add(trimmed)
        }

        // Check for duplicates manually without .toSet()
        var hasDuplicate: Boolean = false
        for (i in 0 until normalizedNames.size) {
            for (j in i + 1 until normalizedNames.size) {
                if (normalizedNames[i] == normalizedNames[j]) {
                    hasDuplicate = true
                }
            }
        }
        if (hasDuplicate) {
            throw IllegalArgumentException("Player names must be unique.")
        }

        // Check round count
        if (totalRounds < 1) {
            throw IllegalArgumentException("Total rounds must be at least 1.")
        }

        // Creating player objects one by one
        val gamePlayers: MutableList<Player> = mutableListOf<Player>()
        for (i in 0 until playerCount) {
            val currentName: String = playersNames[i]
            val newPlayer: Player = Player(name = currentName)
            gamePlayers.add(newPlayer)
        }

        // Creating the main game state object
        val newGame: Game = Game(
            totalRounds = totalRounds,
            currentRound = 1,
            currentPlayerIndex = 0,
            players = gamePlayers
        )

        // Assign to root service
        rootService.currentGame = newGame

        // Shuffle the deck of 52 cards
        createDrawStack()

        // Place 3 cards into the middle area
        val middle: MutableList<Card> = newGame.centerCards
        middle.clear()
        for (i in 1..3) {
            val drawn: Card = newGame.drawStack.pop()
            middle.add(drawn)
        }

        // Deal cards to every player in the list
        for (i in 0 until gamePlayers.size) {
            val playerObj: Player = gamePlayers[i]

            // Give 2 hidden cards to the player
            for (j in 1..2) {
                val hiddenCard: Card = newGame.drawStack.pop()
                playerObj.hiddenCards.add(hiddenCard)
            }
            // Give 3 open cards to the player
            for (j in 1..3) {
                val openCard: Card = newGame.drawStack.pop()
                playerObj.openCards.add(openCard)
            }
            // Reset player actions to 2
            playerObj.actionsLeft = 2
        }

        // Log the event that the game started
        val msg: String = "New game started with " + gamePlayers.size + " players."
        updateLog(msg)

        // Refresh the visual display for the user
        onAllRefreshables { refreshAfterStartNewGame() }
    }

    /**
     * Finishes the game and determines the winners.
     */
    fun endGame() {
        val currentGame: Game = requireGame()
        val allPlayers: MutableList<Player> = currentGame.players

        // Evaluate the hand for every single player in the game
        for (i in 0 until allPlayers.size) {
            val p: Player = allPlayers[i]
            evaluateCards(p)
        }

        // Build a list to sort for the final results scoreboard
        val rankingList: MutableList<Player> = mutableListOf<Player>()
        for (i in 0 until allPlayers.size) {
            val playerToAdd: Player = allPlayers[i]
            rankingList.add(playerToAdd)
        }

        // Use bubble sort to rank the players based on their hand's score value
        val listSize: Int = rankingList.size
        for (i in 0 until listSize - 1) {
            for (j in 0 until listSize - i - 1) {
                val scoreA: Int = rankingList[j].score.ordinal
                val scoreB: Int = rankingList[j + 1].score.ordinal
                if (scoreA < scoreB) {
                    // Swap players in the list
                    val temp: Player = rankingList[j]
                    rankingList[j] = rankingList[j + 1]
                    rankingList[j + 1] = temp
                }
            }
        }

        // Show the finished scene with the final ranking
        onAllRefreshables { refreshAfterGameEnd(rankingList) }

        // Reset the current game to null to end the session
        rootService.currentGame = null
    }

    /**
     * Adds a text message to the game log and notifies the UI.
     */
    fun updateLog(message: String) {
        val activeGame: Game = requireGame()
        // Save to game state log
        activeGame.addLog(message)
        // Update the screen components
        onAllRefreshables { refreshLog(message) }
    }

    /**
     * Creates a full deck of 52 cards and shuffles them randomly.
     */
    private fun createDrawStack() {
        val currentGame: Game = requireGame()
        val cardList: MutableList<Card> = mutableListOf<Card>()

        // Generate all possible cards by using nested loops
        val suitTypes: Array<CardSuit> = CardSuit.entries.toTypedArray()
        val valueTypes: Array<CardValue> = CardValue.entries.toTypedArray()

        for (s in 0 until suitTypes.size) {
            val currentSuit: CardSuit = suitTypes[s]
            for (v in 0 until valueTypes.size) {
                val currentValue: CardValue = valueTypes[v]
                val newCard: Card = Card(currentSuit, currentValue)
                cardList.add(newCard)
            }
        }

        // Mix the card order randomly using a seed
        val nanoTime: Long = System.nanoTime()
        val randomObj: Random = Random(nanoTime)
        cardList.shuffle(randomObj)

        // Fill the game's draw stack after clearing it
        currentGame.drawStack.clear()
        val totalCards: Int = cardList.size
        for (i in 0 until totalCards) {
            val cardToPush: Card = cardList[i]
            currentGame.drawStack.add(cardToPush)
        }
    }

    /**
     * Takes the cards from the discard pile, shuffles them, and puts them back into the deck.
     */
    fun refillDrawStack() {
        val currentGame: Game = requireGame()
        val discardSize: Int = currentGame.discardStack.size
        if (discardSize == 0) {
            throw IllegalStateException("Discard stack is empty. Cannot refill the deck.")
        }

        // Move cards to a list to shuffle them
        val tempCards: MutableList<Card> = mutableListOf<Card>()
        while (currentGame.discardStack.isNotEmpty()) {
            val cardFromDiscard: Card = currentGame.discardStack.pop()
            tempCards.add(cardFromDiscard)
        }

        // Shuffle the list of cards
        val seedVal: Long = System.nanoTime()
        tempCards.shuffle(Random(seedVal))

        // Put the shuffled cards into the draw stack
        for (i in 0 until tempCards.size) {
            val cardToPutBack: Card = tempCards[i]
            currentGame.drawStack.add(cardToPutBack)
        }

        updateLog("The deck was refilled from the discard pile.")
    }

    /**
     * Checks which poker hand the player has and saves the result.
     */
    fun evaluateCards(player: Player) {
        requireGame()

        // Pick up all 5 cards of the player (hidden + open)
        val fullHand: MutableList<Card> = mutableListOf<Card>()
        val hiddenList: MutableList<Card> = player.hiddenCards
        for (i in 0 until hiddenList.size) {
            fullHand.add(hiddenList[i])
        }
        val openList: MutableList<Card> = player.openCards
        for (i in 0 until openList.size) {
            fullHand.add(openList[i])
        }

        // We must have exactly 5 cards to check for a poker hand
        if (fullHand.size != 5) {
            player.score = ScoreTable.NONE
            return
        }

        // Turn card values into rank numbers (2..14)
        val rankNumbers: MutableList<Int> = mutableListOf<Int>()
        for (i in 0 until fullHand.size) {
            val c: Card = fullHand[i]
            val numericalRank: Int = getCardRank(c.value)
            rankNumbers.add(numericalRank)
        }

        // Sort the numbers from lowest to highest using bubble sort logic
        val n: Int = rankNumbers.size
        for (i in 0 until n - 1) {
            for (j in 0 until n - i - 1) {
                val val1: Int = rankNumbers[j]
                val val2: Int = rankNumbers[j + 1]
                if (val1 > val2) {
                    val tempValue: Int = rankNumbers[j]
                    rankNumbers[j] = rankNumbers[j + 1]
                    rankNumbers[j + 1] = tempValue
                }
            }
        }

        // Check for a FLUSH: Do all 5 cards have the same suit?
        var isFlush: Boolean = true
        val suitOfFirst: CardSuit = fullHand[0].suit
        for (i in 0 until fullHand.size) {
            val currentSuit: CardSuit = fullHand[i].suit
            if (currentSuit != suitOfFirst) {
                isFlush = false
            }
        }

        // Check for a STRAIGHT: Are the card ranks in consecutive order?
        val isStraight: Boolean = checkStraight(rankNumbers)

        // Count frequencies of each card rank (e.g., pairs, three of a kind)
        val countStore: MutableMap<Int, Int> = mutableMapOf<Int, Int>()
        for (i in 0 until rankNumbers.size) {
            val rankForCount: Int = rankNumbers[i]
            val existingCount: Int? = countStore[rankForCount]
            if (existingCount == null) {
                countStore[rankForCount] = 1
            } else {
                countStore[rankForCount] = existingCount + 1
            }
        }

        // Collect the counts manually without iterating over .values directly
        val countKeys: MutableList<Int> = mutableListOf<Int>()
        for (key in countStore.keys) {
            countKeys.add(key)
        }
        val collectedCounts: MutableList<Int> = mutableListOf<Int>()
        for (i in 0 until countKeys.size) {
            val key: Int = countKeys[i]
            val countVal: Int = countStore[key]!!
            collectedCounts.add(countVal)
        }

        // Bubble sort for counts descending (e.g., 3, 2 for Full House)
        for (i in 0 until collectedCounts.size - 1) {
            for (j in 0 until collectedCounts.size - i - 1) {
                if (collectedCounts[j] < collectedCounts[j + 1]) {
                    val tempCount: Int = collectedCounts[j]
                    collectedCounts[j] = collectedCounts[j + 1]
                    collectedCounts[j + 1] = tempCount
                }
            }
        }

        // Decide the poker rank enum based on frequencies and flags
        var finalResult: ScoreTable = ScoreTable.HIGHCARD

        val countInfoSize: Int = collectedCounts.size
        // Four of a kind (4, 1)
        if (countInfoSize == 2 && collectedCounts[0] == 4) {
            finalResult = ScoreTable.FOUROFAKIND
        }
        // Full house (3, 2)
        else if (countInfoSize == 2 && collectedCounts[0] == 3) {
            finalResult = ScoreTable.FULLHOUSE
        }
        // Three of a kind (3, 1, 1)
        else if (countInfoSize == 3 && collectedCounts[0] == 3) {
            finalResult = ScoreTable.SET
        }
        // Two pairs (2, 2, 1)
        else if (countInfoSize == 3 && collectedCounts[0] == 2) {
            finalResult = ScoreTable.TWOPAIR
        }
        // One pair (2, 1, 1, 1)
        else if (countInfoSize == 4 && collectedCounts[0] == 2) {
            finalResult = ScoreTable.PAIR
        }

        // Special combinations for Straights and Flushes
        if (isStraight && isFlush) {
            // Check for Royal Flush (10, 11, 12, 13, 14)
            if (rankNumbers[0] == 10 && rankNumbers[4] == 14) {
                finalResult = ScoreTable.ROYALFLUSH
            } else {
                finalResult = ScoreTable.STRAIGHTFLUSH
            }
        } else if (isFlush) {
            // Assign flush if no better rank found
            if (finalResult.ordinal < ScoreTable.FLUSH.ordinal) {
                finalResult = ScoreTable.FLUSH
            }
        } else if (isStraight) {
            // Assign straight if no better rank found
            if (finalResult.ordinal < ScoreTable.STRAIGHT.ordinal) {
                finalResult = ScoreTable.STRAIGHT
            }
        }

        // Set the score in player object
        player.score = finalResult
    }

    /**
     * Checks if the given sorted ranks form a straight.
     */
    private fun checkStraight(sortedRanks: List<Int>): Boolean {
        // Normal check for incremental cards
        var isNormalStraight: Boolean = true
        for (i in 0 until sortedRanks.size - 1) {
            val current: Int = sortedRanks[i]
            val next: Int = sortedRanks[i + 1]
            if (next != current + 1) {
                isNormalStraight = false
            }
        }
        if (isNormalStraight) {
            return true
        }

        // Ace-low straight (A, 2, 3, 4, 5) -> 2, 3, 4, 5, 14
        if (sortedRanks[0] == 2 && sortedRanks[1] == 3 && sortedRanks[2] == 4 && sortedRanks[3] == 5 && sortedRanks[4] == 14) {
            return true
        }

        return false
    }

    /**
     * Turns a card value enum into a number from 2 to 14.
     */
    private fun getCardRank(cardValueEnum: CardValue): Int {
        if (cardValueEnum == CardValue.TWO) return 2
        if (cardValueEnum == CardValue.THREE) return 3
        if (cardValueEnum == CardValue.FOUR) return 4
        if (cardValueEnum == CardValue.FIVE) return 5
        if (cardValueEnum == CardValue.SIX) return 6
        if (cardValueEnum == CardValue.SEVEN) return 7
        if (cardValueEnum == CardValue.EIGHT) return 8
        if (cardValueEnum == CardValue.NINE) return 9
        if (cardValueEnum == CardValue.TEN) return 10
        if (cardValueEnum == CardValue.JACK) return 11
        if (cardValueEnum == CardValue.QUEEN) return 12
        if (cardValueEnum == CardValue.KING) return 13
        if (cardValueEnum == CardValue.ACE) return 14
        return 0
    }

    /**
     * Moves to the next player and increments the round if needed.
     */
    fun startTurn() {
        val gameInstance: Game = requireGame()

        // Calculate the index of the next player
        var nextPlayerIdx: Int = gameInstance.currentPlayerIndex + 1
        if (nextPlayerIdx >= gameInstance.players.size) {
            nextPlayerIdx = 0
            // Increment logic for rounds
            val oldRound: Int = gameInstance.currentRound
            val newRound: Int = oldRound + 1
            gameInstance.currentRound = newRound
        }

        // Update the current player index in game state
        gameInstance.currentPlayerIndex = nextPlayerIdx

        // Check if the total rounds have been exceeded
        if (gameInstance.currentRound > gameInstance.totalRounds) {
            endGame()
            return
        }

        // Every player gets 2 new actions at turn start
        val playersList: MutableList<Player> = gameInstance.players
        val activePlayerObj: Player = playersList[nextPlayerIdx]
        activePlayerObj.actionsLeft = 2

        val logMessageText: String = "It is now the turn of " + activePlayerObj.name + "."
        updateLog(logMessageText)

        onAllRefreshables { refreshAfterTurnStart() }
    }

    /**
     * Ends the current turn and moves to the next player.
     */
    fun endTurn() {
        startTurn()
        if (rootService.currentGame != null) {
            onAllRefreshables { refreshAfterTurnEnd() }
        }
    }

}