package gui

import service.Refreshable
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * Example scene showing a greeting.
 */
class HelloScene : BoardGameScene(), Refreshable {

    /** Label with hello message. */
    private val helloLabel = Label(
        width = 1920,
        height = 1080,
        posX = 0,
        posY = 0,
        text = "Hello, SoPra!",
        font = Font(size = 96)
    )

    init {
        background = ColorVisual(108, 168, 59)
        addComponents(helloLabel)
    }
}