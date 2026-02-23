package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/** Test class for [Player]. */
class PlayerTest {

    /** Tests that a player starts with 2 actions by default. */
    @Test
    fun testDefaultActionsLeft() {
        val player = Player("Alice")
        assertEquals(2, player.actionsLeft)
    }

    /** Tests that actionsLeft is mutable. */
    @Test
    fun testActionsLeftMutable() {
        val player = Player("Bob")
        player.actionsLeft = 1
        assertEquals(1, player.actionsLeft)
    }
}