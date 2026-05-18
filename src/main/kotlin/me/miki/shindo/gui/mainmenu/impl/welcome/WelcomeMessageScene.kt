package me.miki.shindo.gui.mainmenu.impl.welcome

import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.v2.Animation
import me.miki.shindo.ui.animation.v2.Direction
import me.miki.shindo.ui.animation.v2.curve.DecelerateAnimation
import me.miki.shindo.utils.TimerUtils
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

class WelcomeMessageScene(
    parent: GuiShindoMainMenu,
) : MainMenuScene(parent) {
    private val timer = TimerUtils()
    private var fadeAnimation: Animation? = null
    private var step = 0
    private var message: String? = null

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val sr = ScaledResolution(mc)
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager!!
        val hello = "Hello!"
        val welcomeMessage = "Welcome to Shindo Client"
        val setupMessage = "An Custom Version of Soar Client"
        val setupMessage2 = "Time to setup Shindo."

        BlurUtils.drawBlurScreen(14F)

        if (fadeAnimation == null && getParent().isDoneBackgroundAnimation()) {
            fadeAnimation = DecelerateAnimation(800, 1.0)
            fadeAnimation!!.setDirection(Direction.FORWARDS)
            fadeAnimation!!.reset()
            timer.reset()
        }

        if (fadeAnimation != null) {
            message =
                when (step) {
                    0 -> hello
                    1 -> welcomeMessage
                    2 -> setupMessage
                    3 -> setupMessage2
                    else -> message
                }

            nvg.setupAndDraw(
                Runnable {
                    nvg.drawCenteredText(
                        message!!,
                        sr.scaledWidth / 2f,
                        (sr.scaledHeight / 2f) - (nvg.getTextHeight(message!!, 26f, Fonts.REGULAR) / 2),
                        Color(255, 255, 255, (fadeAnimation!!.getValueFloat() * 255).toInt()),
                        26f,
                        Fonts.REGULAR,
                    )
                },
            )

            if (timer.delay(2500) && fadeAnimation!!.getDirection() == Direction.FORWARDS) {
                fadeAnimation!!.setDirection(Direction.BACKWARDS)
                timer.reset()
            }

            if (fadeAnimation!!.isDone(Direction.BACKWARDS)) {
                if (step == 3) {
                    setCurrentScene(getSceneByClass(LanguageSelectScene::class.java))
                    return
                }

                step++
                fadeAnimation!!.setDirection(Direction.FORWARDS)
            }
        }
    }
}
