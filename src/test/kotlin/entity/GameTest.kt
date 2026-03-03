package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the Game entity.
 */
class GameTest {

    /** Checks if a game starts with the right info. */
    @Test
    fun testGameCreation() {
        val game = Game(
            totalRounds = 5,
            players = mutableListOf(Player("Alice"), Player("Bob"))
        )

        assertEquals(5, game.totalRounds)
        assertEquals(1, game.currentRound)
        assertEquals(0, game.currentPlayerIndex)
        assertEquals(2, game.players.size)
    }

    /** Checks if log messages are added correctly. */
    @Test
    fun testAddLog() {
        val game = Game()
        game.addLog("hello")

        assertEquals(listOf("hello"), game.log)
    }
}