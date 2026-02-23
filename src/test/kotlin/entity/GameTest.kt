package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/** Test class for [Game]. */
class GameTest {

    /** Tests basic game construction and defaults. */
    @Test
    fun testGameCreation() {
        val game = Game(
            nRounds = 5,
            players = mutableListOf(Player("Alice"), Player("Bob"))
        )

        assertEquals(5, game.nRounds)
        assertEquals(0, game.curPlayerIdx)
        assertEquals(2, game.players.size)
    }

    /** Tests that addLog actually appends to the log. */
    @Test
    fun testAddLog() {
        val game = Game()
        game.addLog("hello")
        assertEquals(listOf("hello"), game.log)
    }
}