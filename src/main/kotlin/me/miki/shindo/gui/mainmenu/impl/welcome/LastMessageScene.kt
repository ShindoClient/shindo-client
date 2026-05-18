package me.miki.shindo.gui.mainmenu.impl.welcome

import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.gui.mainmenu.impl.MainScene
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.sound.Sound
import me.miki.shindo.management.sound.Sounds
import me.miki.shindo.ui.animation.v2.Animation
import me.miki.shindo.ui.animation.v2.Direction
import me.miki.shindo.ui.animation.v2.curve.DecelerateAnimation
import me.miki.shindo.utils.TimerUtils
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

class LastMessageScene(
    parent: GuiShindoMainMenu,
) : MainMenuScene(parent) {
    private val blurAnimation: Animation = DecelerateAnimation(800, 13.0)
    private val timer = TimerUtils()
    private var fadeAnimation: Animation? = null
    private var step = 0
    private var message: String? = null
    private var soundPlayed = false

    init {
        blurAnimation.setValue(13.0)
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val sr = ScaledResolution(mc)
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager!!
        val compMessage = "Setup is complete!"
        val welcomeMessage = "Thank you for choosing Shindo Client!"

        BlurUtils.drawBlurScreen(1 + blurAnimation.getValueFloat())

        if (fadeAnimation == null && getParent().isDoneBackgroundAnimation()) {
            fadeAnimation = DecelerateAnimation(800, 1.0)
            fadeAnimation!!.setDirection(Direction.FORWARDS)
            fadeAnimation!!.reset()
            timer.reset()
        }

        if (blurAnimation.isDone(Direction.BACKWARDS)) {
            Shindo.getInstance().getShindoAPI().createFirstLoginFile()
            setCurrentScene(getSceneByClass(MainScene::class.java))
        }

        if (fadeAnimation != null) {
            message =
                when (step) {
                    0 -> compMessage
                    1 -> welcomeMessage
                    else -> message
                }
            if (!soundPlayed) {
                Sound.play(Sounds.SHINDO_AUDIO_SUCCESS, true)
                soundPlayed = true
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

            if (timer.delay(3000) && fadeAnimation!!.getDirection() == Direction.FORWARDS) {
                fadeAnimation!!.setDirection(Direction.BACKWARDS)
                timer.reset()
            }

            if (fadeAnimation!!.isDone(Direction.BACKWARDS)) {
                if (step == 1) {
                    blurAnimation.setDirection(Direction.BACKWARDS)
                    return
                }

                step++
                fadeAnimation!!.setDirection(Direction.FORWARDS)
            }
        }
    }
}
