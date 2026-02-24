package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test class for the [Game] entity.
 *
 * This class verifies correct initialization of game state and
 * the behavior of simple data-modifying functions.
 */
class GameTest {

    /**
     * Verifies that a Game instance is correctly initialized
     * with provided constructor arguments and default values.
     *
     * Expected behavior:
     * - The total number of rounds is set correctly.
     * - The number of rounds left is set correctly.
     * - The current round starts at 1.
     * - The current player index is initialized to 0.
     * - The players list contains the provided players.
     */
    @Test
    fun testGameCreation() {
        val game = Game(
            totalRounds = 5,
            roundsLeft = 5,
            players = mutableListOf(Player("Alice"), Player("Bob"))
        )

        assertEquals(5, game.totalRounds)
        assertEquals(5, game.roundsLeft)
        assertEquals(1, game.currRound)
        assertEquals(0, game.currentPlayerIndex)
        assertEquals(2, game.players.size)
    }

    /**
     * Verifies that the addLog() method correctly appends
     * a message to the game log.
     */
    @Test
    fun testAddLog() {
        val game = Game()
        game.addLog("hello")

        assertEquals(listOf("hello"), game.log)
    }
}