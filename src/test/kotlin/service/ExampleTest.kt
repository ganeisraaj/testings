package service

import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Basic example test.
 */
class ExampleTest {

    private lateinit var rootService: RootService

    @BeforeTest
    fun setUp() {
        rootService = RootService()
    }

    /** Simple check for setup. */
    @Test
    fun testIfSetUpWorked() {
        assertDoesNotThrow("The root service should be initialized.") { rootService }
    }
}