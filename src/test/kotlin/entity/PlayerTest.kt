package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the Player entity.
 */
class PlayerTest {

    /** Checks if a player starts with 2 actions. */
    @Test
    fun testDefaultActionsLeft() {
        val player = Player("Alice")
        assertEquals(2, player.actionsLeft)
    }

    /** Checks if the actions count can be changed. */
    @Test
    fun testActionsLeftMutable() {
        val player = Player("Bob")
        player.actionsLeft = 1
        assertEquals(1, player.actionsLeft)
    }
}