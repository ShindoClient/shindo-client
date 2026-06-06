package com.shindoclient.shindo.gui.mainmenu

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.gui.mainmenu.impl.AccountScene
import com.shindoclient.shindo.gui.mainmenu.impl.BackgroundScene
import com.shindoclient.shindo.gui.mainmenu.impl.MainScene
import com.shindoclient.shindo.gui.mainmenu.impl.ShopScene
import com.shindoclient.shindo.gui.mainmenu.impl.UpdateScene
import com.shindoclient.shindo.gui.mainmenu.impl.welcome.AccentColorSelectScene
import com.shindoclient.shindo.gui.mainmenu.impl.welcome.CheckingDataScene
import com.shindoclient.shindo.gui.mainmenu.impl.welcome.LanguageSelectScene
import com.shindoclient.shindo.gui.mainmenu.impl.welcome.LastMessageScene
import com.shindoclient.shindo.gui.mainmenu.impl.welcome.ThemeSelectScene
import com.shindoclient.shindo.gui.mainmenu.impl.welcome.WelcomeMessageScene
import com.shindoclient.shindo.management.color.ColorManager
import com.shindoclient.shindo.management.color.Theme
import com.shindoclient.shindo.management.color.palette.ColorPalette
import com.shindoclient.shindo.management.color.palette.ColorType
import com.shindoclient.shindo.management.event.impl.EventRenderNotification
import com.shindoclient.shindo.management.nanovg.NanoVGManager
import com.shindoclient.shindo.management.nanovg.font.Fonts
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.profile.mainmenu.impl.CustomBackground
import com.shindoclient.shindo.management.profile.mainmenu.impl.DefaultBackground
import com.shindoclient.shindo.management.profile.mainmenu.impl.ShaderBackground
import com.shindoclient.shindo.management.shader.ShaderBackgroundRenderer
import com.shindoclient.shindo.management.sound.Sound
import com.shindoclient.shindo.management.sound.Sounds
import com.shindoclient.shindo.ui.animation.v2.Animation
import com.shindoclient.shindo.ui.animation.v2.Direction
import com.shindoclient.shindo.ui.animation.v2.curve.DecelerateAnimation
import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation
import com.shindoclient.shindo.utils.ColorUtils
import com.shindoclient.shindo.utils.mouse.MouseUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import java.awt.Color

class GuiShindoMainMenu : GuiScreen() {
    private val scenes: ArrayList<MainMenuScene> = ArrayList()

    private val skinFocusAnimation = SimpleAnimation()
    private val backgroundSelectFocusAnimation = SimpleAnimation()
    private val closeFocusAnimation = SimpleAnimation()

    private val backgroundAnimations = Array(2) { SimpleAnimation() }

    var currentScene: MainMenuScene? = null
        private set

    private var soundPlayed = false

    private var fadeIconAnimation: Animation? = null
    private var fadeBackgroundAnimation: Animation? = null

    init {
        val instance = Shindo.getInstance()
        val firstLogin = instance.getShindoAPI().isFirstLogin()
        ensureDefaultColorScheme(instance, firstLogin)

        scenes.add(MainScene(this))
        scenes.add(BackgroundScene(this))
        scenes.add(ShopScene(this))
        scenes.add(AccountScene(this))
        // scenes.add(SkinScene(this))
        scenes.add(UpdateScene(this))
        scenes.add(WelcomeMessageScene(this))
        scenes.add(LanguageSelectScene(this))
        scenes.add(ThemeSelectScene(this))
        scenes.add(AccentColorSelectScene(this))
        scenes.add(CheckingDataScene(this))
        scenes.add(LastMessageScene(this))

        currentScene =
            if (firstLogin) {
                getSceneByClass(WelcomeMessageScene::class.java)
            } else {
                if (instance.isUpdateNeeded()) {
                    getSceneByClass(UpdateScene::class.java)
                } else {
                    getSceneByClass(MainScene::class.java)
                }
            }
    }

    override fun initGui() {
        currentScene?.initGui()
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val sr = ScaledResolution(mc)

        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager

        backgroundAnimations[0].setAnimation(Mouse.getX().toFloat(), 16.0)
        backgroundAnimations[1].setAnimation(Mouse.getY().toFloat(), 16.0)

        nvg!!.setupAndDraw(
            Runnable {
                drawNanoVG(sr, instance, nvg)
            },
        )

        currentScene?.drawScreen(mouseX, mouseY, partialTicks)

        if (fadeBackgroundAnimation == null ||
            (
                fadeBackgroundAnimation != null &&
                    !fadeBackgroundAnimation!!.isDone(
                        Direction.FORWARDS,
                    )
            )
        ) {
            nvg.setupAndDraw(Runnable { drawSplashScreen(sr, nvg) })
            if (!soundPlayed) {
                Sound.play(Sounds.SHINDO_AUDIO_START, true)
                soundPlayed = true
            }
        }

        nvg.setupAndDraw(Runnable { EventRenderNotification().call() })

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawNanoVG(
        sr: ScaledResolution,
        instance: Shindo,
        nvg: NanoVGManager,
    ) {
        val copyright = "Copyright Mojang AB. Do not distribute!"
        when (val currentBackground = instance.getProfileManager().backgroundManager.getCurrentBackground()) {
            is DefaultBackground -> {
                nvg.drawImage(
                    currentBackground.getImage()!!,
                    -21f + backgroundAnimations[0].getValue() / 90,
                    backgroundAnimations[1].getValue() * -1 / 90,
                    sr.scaledWidth + 21f,
                    sr.scaledHeight + 20f,
                )
            }

            is CustomBackground -> {
                nvg.drawImage(
                    currentBackground.getImage(),
                    -21f + backgroundAnimations[0].getValue() / 90,
                    backgroundAnimations[1].getValue() * -1 / 90,
                    sr.scaledWidth + 21f,
                    sr.scaledHeight + 20f,
                )
            }

            is ShaderBackground -> {
                ShaderBackgroundRenderer.renderShaderBackground(
                    nvg,
                    currentBackground.getShaderFile(),
                    -21f + backgroundAnimations[0].getValue() / 90,
                    backgroundAnimations[1].getValue() * -1 / 90,
                    sr.scaledWidth + 21f,
                    sr.scaledHeight + 20f,
                )
            }
        }

        nvg.drawText(
            copyright,
            sr.scaledWidth - (nvg.getTextWidth(copyright, 9f, Fonts.REGULAR)) - 4,
            sr.scaledHeight - 12f,
            Color.WHITE,
            9f,
            Fonts.REGULAR,
        )
        nvg.drawText(
            instance.getBuildInfo().getDisplayString(),
            4f,
            sr.scaledHeight - 12f,
            Color.WHITE,
            9f,
            Fonts.REGULAR,
        )
    }

    private fun drawButtons(
        mouseX: Int,
        mouseY: Int,
        sr: ScaledResolution,
        nvg: NanoVGManager,
    ) {
        val controlColor = getControlFillColor()

        closeFocusAnimation.setAnimation(
            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 28f, 6f, 22f, 22f)) 1.0f else 0.0f,
            16.0,
        )

        nvg.drawRoundedRect(sr.scaledWidth - 28f, 6f, 22f, 22f, 4f, controlColor)
        nvg.drawCenteredText(
            Lucide.X,
            sr.scaledWidth - 19f,
            8f,
            Color(
                255,
                255 - (closeFocusAnimation.getValue() * 200).toInt(),
                255 - (closeFocusAnimation.getValue() * 200).toInt(),
            ),
            18f,
            Fonts.LUCIDE,
        )

        backgroundSelectFocusAnimation.setAnimation(
            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 56f, 6f, 22f, 22f)) 1.0f else 0.0f,
            16.0,
        )

        nvg.drawRoundedRect(sr.scaledWidth - 56f, 6f, 22f, 22f, 4f, controlColor)
        nvg.drawCenteredText(
            Lucide.IMAGE,
            sr.scaledWidth - 52f + 6.5f - 1.5f,
            9.5f - 1.5f,
            Color(
                255 - (backgroundSelectFocusAnimation.getValue() * 200).toInt(),
                255,
                255 - (backgroundSelectFocusAnimation.getValue() * 200).toInt(),
            ),
            18f,
            Fonts.LUCIDE,
        )

        // skinFocusAnimation.setAnimation( if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 84f, 6f, 22f, 22f)) 1.0f else 0.0f, 16 )

        // nvg.drawRoundedRect(sr.scaledWidth - 84f, 6f, 22f, 22f, 4f, controlColor)
        // nvg.drawCenteredText(Lucide.SKIN, sr.scaledWidth - 78f + 4.5f, 9.5f, Color(255 - (skinFocusAnimation.value * 200).toInt(), 255, 255), 15f, Fonts.LUCIDE )
    }

    private fun drawSplashScreen(
        sr: ScaledResolution,
        nvg: NanoVGManager,
    ) {
        if (fadeIconAnimation == null) {
            fadeIconAnimation = DecelerateAnimation(100, 1.0)
            fadeIconAnimation!!.setDirection(Direction.FORWARDS)
            fadeIconAnimation!!.reset()
        }

        if (fadeIconAnimation != null) {
            if (fadeIconAnimation!!.isDone(Direction.FORWARDS) && fadeBackgroundAnimation == null) {
                fadeBackgroundAnimation = DecelerateAnimation(500, 1.0)
                fadeBackgroundAnimation!!.setDirection(Direction.FORWARDS)
                fadeBackgroundAnimation!!.reset()
            }

            nvg.drawRect(
                0f,
                0f,
                sr.scaledWidth.toFloat(),
                sr.scaledHeight.toFloat(),
                Color(
                    0,
                    0,
                    0,
                    if (fadeBackgroundAnimation !=
                        null
                    ) {
                        (255 - (fadeBackgroundAnimation!!.getValue() * 255)).toInt()
                    } else {
                        255
                    },
                ),
            )
            nvg.drawCenteredText(
                Shinconic.SHINDO,
                sr.scaledWidth / 2f,
                (sr.scaledHeight / 2f) - (nvg.getTextHeight(Shinconic.SHINDO, 130f, Fonts.SHINCONIC) / 2) - 1,
                Color(255, 255, 255, (255 - (fadeIconAnimation!!.getValue() * 255)).toInt()),
                130f,
                Fonts.SHINCONIC,
            )
        }
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        val sr = ScaledResolution(mc)

        val isFirstLogin = Shindo.getInstance().getShindoAPI().isFirstLogin()

        if (mouseButton == 0 && !isFirstLogin) {
            // if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 28f, 6f, 22f, 22f)) {
            //    mc.shutdown()
            // }

            // if (MouseUtils.isInside(
            //        mouseX,
            //        mouseY,
            //        sr.scaledWidth - 56f,
            //        6f,
            //        22f,
            //        22f
            //    ) && currentScene != getSceneByClass(BackgroundScene::class.java)
            // ) {
            //    setCurrentScene(getSceneByClass(BackgroundScene::class.java))
            // }

            // if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 84f, 6f, 22f, 22f)) {
            //    setCurrentScene(getSceneByClass(SkinScene::class.java))
            // }
        }

        currentScene?.mouseClicked(mouseX, mouseY, mouseButton)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        currentScene?.mouseReleased(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        currentScene?.keyTyped(typedChar, keyCode)
    }

    override fun onGuiClosed() {
        currentScene?.onGuiClosed()
    }

    fun setCurrentScene(currentScene: MainMenuScene?) {
        this.currentScene?.onSceneClosed()
        this.currentScene = currentScene
        this.currentScene?.initScene()
    }

    fun isDoneBackgroundAnimation(): Boolean = fadeBackgroundAnimation != null && fadeBackgroundAnimation!!.isDone(Direction.FORWARDS)

    fun getSceneByClass(clazz: Class<out MainMenuScene>): MainMenuScene? {
        for (scene in scenes) {
            if (scene.javaClass == clazz) {
                return scene
            }
        }
        return null
    }

    private fun ensureDefaultColorScheme(
        instance: Shindo?,
        forceDefaults: Boolean,
    ) {
        if (instance == null) return

        val colorManager: ColorManager = instance.getColorManager()

        if (forceDefaults) {
            colorManager.setCurrentColor(colorManager.getColorByName("Default"))
            colorManager.setTheme(Theme.DARK)
        }
    }

    private fun getControlFillColor(): Color {
        val instance = Shindo.getInstance()
        val palette: ColorPalette = instance.getColorManager().getPalette()
        val base = palette.getBackgroundColor(ColorType.NORMAL)
        return ColorUtils.applyAlpha(base, 255.coerceAtMost(base.alpha + 5))
    }

    fun getBackgroundColor(): Color {
        val instance = Shindo.getInstance()
        val palette: ColorPalette = instance.getColorManager().getPalette()
        val base = palette.getBackgroundColor(ColorType.DARK)
        return ColorUtils.applyAlpha(base, 235)
    }
}
