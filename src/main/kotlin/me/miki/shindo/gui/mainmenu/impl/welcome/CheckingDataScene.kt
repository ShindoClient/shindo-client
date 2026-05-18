package me.miki.shindo.gui.mainmenu.impl.welcome

import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.v2.Animation
import me.miki.shindo.ui.animation.v2.Direction
import me.miki.shindo.ui.animation.v2.curve.DecelerateAnimation
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

class CheckingDataScene(
    parent: GuiShindoMainMenu,
) : MainMenuScene(parent) {
    private var fadeAnimation: Animation? = null

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val sr = ScaledResolution(mc)
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager!!
        val message = "Checking the data..."

        BlurUtils.drawBlurScreen(14F)

        if (fadeAnimation == null && getParent().isDoneBackgroundAnimation()) {
            fadeAnimation = DecelerateAnimation(800, 1.0)
            fadeAnimation!!.setDirection(Direction.FORWARDS)
            fadeAnimation!!.reset()
        }

        if (fadeAnimation != null) {
            nvg.setupAndDraw(
                Runnable {
                    nvg.drawCenteredText(
                        message,
                        sr.scaledWidth / 2f,
                        (sr.scaledHeight / 2f) - (nvg.getTextHeight(message, 26f, Fonts.REGULAR) / 2),
                        Color(255, 255, 255, (fadeAnimation!!.getValueFloat() * 255).toInt()),
                        26f,
                        Fonts.REGULAR,
                    )
                },
            )

            if (Shindo
                    .getInstance()
                    .getDownloadManager()
                    .isDownloaded() &&
                fadeAnimation!!.getDirection() == Direction.FORWARDS
            ) {
                fadeAnimation!!.setDirection(Direction.BACKWARDS)
            }

            if (fadeAnimation!!.isDone(Direction.BACKWARDS)) {
                setCurrentScene(getSceneByClass(LastMessageScene::class.java))
            }
        }
    }
}
