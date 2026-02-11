package me.miki.shindo.gui.modmenu

import eu.shoroa.contrib.render.Blur
import me.miki.shindo.Shindo
import me.miki.shindo.gui.GuiEditHUD
import me.miki.shindo.gui.IShindoScreen
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.gui.modmenu.category.impl.*
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.event.impl.EventRenderNotification
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.comp.buttons.CompIconButton
import me.miki.shindo.ui.comp.inputs.CompSearchBox
import me.miki.shindo.utils.MathUtils
import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.Direction
import me.miki.shindo.ui.animation.easing.EaseBackIn
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.ui.animation.screen.ScreenAnimation
import me.miki.shindo.utils.file.FileUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.io.IOException
import kotlin.math.min

class GuiModMenu(
    private val initialCategoryClass: Class<out Category>? = null,
    private val initialSearchText: String? = null
) : GuiScreen(), IShindoScreen {

    private val categories = ArrayList<Category>()
    private val moveAnimation = SimpleAnimation()
    private val screenAnimation = ScreenAnimation()
    private val scroll = Scroll()
    private val searchBox = CompSearchBox()
    private val layoutButton = CompIconButton(21f) { LegacyIcon.LAYOUT }
    private val folderButton = CompIconButton(18f) { LegacyIcon.FOLDER }
    private lateinit var introAnimation: Animation
    private var x = 0
    private var y = 0
    private var menuWidth = 0
    private var menuHeight = 0
    private var currentCategory: Category
    private var toEditHUD = false
    private var canClose = false

    init {
        categories.add(HomeCategory(this))
        categories.add(ModuleCategory(this))
        categories.add(AddonCategory(this))
        categories.add(CosmeticsCategory(this))
        categories.add(SpotifyCategory(this))
        categories.add(ProfileCategory(this))

        categories.add(ScreenshotCategory(this))
        categories.add(NetworkCategory(this))
        categories.add(SettingsCategory(this))

        currentCategory = getCategoryByClass(HomeCategory::class.java)
    }

    override fun initGui() {
        val sr = ScaledResolution(mc)

        val addX = 250
        val addY = 160

        x = (sr.scaledWidth / 2) - addX
        y = (sr.scaledHeight / 2) - addY
        menuWidth = addX * 2
        menuHeight = addY * 2

        introAnimation = EaseBackIn(320, 1.0, 2.0f)
        introAnimation.setDirection(Direction.FORWARDS)

        for (c in categories) {
            c.initGui()
        }

        scroll.resetAll()
        toEditHUD = false
        canClose = true
        initialCategoryClass?.let { currentCategory = getCategoryByClass(it) }
        searchBox.setText(initialSearchText ?: "")

        layoutButton.onClick {
            toEditHUD = true
            introAnimation.setDirection(Direction.BACKWARDS)
        }
        layoutButton.setFontSize(14f)
        layoutButton.setRadius(6f)
        layoutButton.enabledWhen { canClose }

        folderButton.setRadius(6f)
        folderButton.setFontSize(9f)
        folderButton.setVisible(false)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg: NanoVGManager = instance.nanoVGManager ?: return

        if (InternalSettingsMod.instance.getBlurSetting()?.isToggled() == true) {
            BlurUtils.drawBlurScreen(min(introAnimation.getValue(), 1.0).toFloat() * 20f + 1f)
        }
        screenAnimation.wrap(Runnable {
            nvg.drawShadow(x.toFloat(), y.toFloat(), menuWidth.toFloat(), menuHeight.toFloat(), 12f)
        }, (2f - introAnimation.getValueFloat()), min(introAnimation.getValueFloat(), 1f))

        screenAnimation.wrap(
            Runnable { drawNanoVG(mouseX, mouseY, partialTicks) },
            x.toFloat(),
            y.toFloat(),
            menuWidth.toFloat(),
            menuHeight.toFloat(),
            (2f - introAnimation.getValueFloat()),
            min(introAnimation.getValueFloat(), 1f),
            true
        )

        EventRenderNotification().call()

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawNanoVG(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg: NanoVGManager = instance.nanoVGManager ?: return
        val colorManager: ColorManager = instance.colorManager
        val palette: ColorPalette = colorManager.getPalette()
        val currentColor: AccentColor = colorManager.getCurrentColor()

        if (introAnimation.isDone(Direction.BACKWARDS)) {
            mc.displayGuiScreen(if (toEditHUD) GuiEditHUD(true) else null)
        }
        nvg.drawRoundedRect(
            x.toFloat(),
            y.toFloat(),
            menuWidth.toFloat(),
            menuHeight.toFloat(),
            12f,
            palette.getBackgroundColor(ColorType.NORMAL)
        )

        if (InternalSettingsMod.instance.getBlurSetting()?.isToggled() == true) {
            Blur.drawBlur {
                nvg.drawRoundedRectVarying(
                    x.toFloat(),
                    y.toFloat(),
                    32f,
                    menuHeight.toFloat(),
                    12f,
                    0f,
                    12f,
                    0f,
                    palette.getBackgroundColor(ColorType.DARK)
                )
            }
            val colsidebar = palette.getBackgroundColor(ColorType.DARK)
            nvg.drawRoundedRectVarying(
                x.toFloat(),
                y.toFloat(),
                32f,
                menuHeight.toFloat(),
                12f,
                0f,
                12f,
                0f,
                Color(colsidebar.red, colsidebar.green, colsidebar.blue, 210)
            )
        } else {
            nvg.drawRoundedRectVarying(
                x.toFloat(),
                y.toFloat(),
                32f,
                menuHeight.toFloat(),
                12f,
                0f,
                12f,
                0f,
                palette.getBackgroundColor(ColorType.DARK)
            )
        }

        nvg.drawGradientRoundedRect(x + 5f, y + 7f, 22f, 22f, 11f, currentColor.getColor1(), currentColor.getColor2())
        nvg.drawText(LegacyIcon.SHINDO, x + 8f, y + 10f, Color.WHITE, 16f, Fonts.LEGACYICON)
        if (currentCategory.isShowTitle()) {
            nvg.save()
            nvg.translate((currentCategory.getTextAnimation().value * 15), 0f)
            nvg.drawText(
                currentCategory.getName(),
                x + 32f,
                y + 10f,
                palette.getFontColor(ColorType.DARK, (currentCategory.getTextAnimation().value * 255).toInt()),
                15f,
                Fonts.SEMIBOLD
            )
            nvg.restore()
        }

        var offsetY = 0

        moveAnimation.setAnimation((categories.indexOf(currentCategory) * 22F), 18.0)

        nvg.save()

        nvg.drawGradientRoundedRect(
            x + 5.5f,
            y + 34.5f + moveAnimation.value,
            21f,
            21f,
            5f,
            currentColor.getColor1(),
            currentColor.getColor2()
        )

        for (c in categories) {
            val textColor = c.getTextColorAnimation().getColor(
                if (MathUtils.isInRange(
                        moveAnimation.value,
                        offsetY - 8f,
                        offsetY + 8f
                    )
                ) Color.WHITE else palette.getFontColor(ColorType.NORMAL),
                18
            )

            c.getTextAnimation().setAnimation(if (c == currentCategory) 1.0f else 0.0f, 14.0)

            nvg.drawText(c.getIcon(), x + 9f, y + 38f + offsetY, textColor, 14f, Fonts.LEGACYICON)

            offsetY += 22
        }

        nvg.restore()

        layoutButton.setBounds(x + 5.5f, y + menuHeight - 30f, 21f, 21f)
        layoutButton.draw(mouseX, mouseY, partialTicks)

        for (c in categories) {
            c.getCategoryAnimation().setAnimation(if (c == currentCategory) 1.0f else 0.0f, 16.0)

            if (c == currentCategory) {
                nvg.save()

                if (!c.isInitialized()) {
                    c.setInitialized(true)
                    c.initCategory()
                    searchBox.setText("")
                    c.setCanClose(true)
                }

                if (c.isShowSearchBox()) {
                    searchBox.setPosition(x + menuWidth - 175f, y + 6.5f, 160f, 18f)
                    searchBox.draw(mouseX, mouseY, partialTicks)
                }
                val yOff = if (currentCategory.isShowTitle()) 31 else 0
                folderButton.setVisible(false)

                if (currentCategory is CosmeticsCategory) {
                    val cosmeticsCategory = currentCategory as CosmeticsCategory
                    if (cosmeticsCategory.shouldShowCustomCapeFolder()) {
                        val folderButtonX = x + menuWidth - 198f
                        val folderButtonY = y + 6.5f
                        folderButton.setVisible(true)
                        folderButton.setBounds(folderButtonX, folderButtonY, 18f, 18f)
                        folderButton.setIconColorSupplier { palette.getFontColor(ColorType.NORMAL) }
                        folderButton.onClick {
                            FileUtils.openFolderAtPath(Shindo.getInstance().fileManager.customCapeDir)
                        }
                        folderButton.draw(mouseX, mouseY, partialTicks)
                    }
                }

                nvg.scissor(
                    (x + 32).toFloat(),
                    (y + yOff).toFloat(),
                    (menuWidth - 32).toFloat(),
                    (menuHeight - yOff).toFloat()
                )
                nvg.translate(0f, 50f - (c.getCategoryAnimation().value * 50f))

                c.drawScreen(mouseX, mouseY, partialTicks)

                nvg.restore()
            } else if (c.isInitialized()) {
                c.setInitialized(false)
            }
        }

        if (MouseUtils.isInside(
                mouseX,
                mouseY,
                x + 32f,
                y + 31f,
                (menuWidth - 32).toFloat(),
                (menuHeight - 31).toFloat()
            )
        ) {
            scroll.onScroll()
        }

        scroll.onAnimation()

        if (currentCategory.isShowSearchBox() && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(
                Keyboard.KEY_F
            )
        ) {
            currentCategory.getSearchBox().setFocused(true)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        var offsetY = 0

        if (!MouseUtils.isInside(
                mouseX,
                mouseY,
                x - 5f,
                y - 5f,
                (menuWidth + 10).toFloat(),
                (menuHeight + 10).toFloat()
            ) && mouseButton == 0 && canClose
        ) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }

        for (c in categories) {
            if (MouseUtils.isInside(mouseX, mouseY, x + 5.5f, y + 34.5f + offsetY, 21f, 21f) && mouseButton == 0) {
                currentCategory = c
            }
            offsetY += 22
        }

        currentCategory.mouseClicked(mouseX, mouseY, mouseButton)
        searchBox.mouseClicked(mouseX, mouseY, mouseButton)

        layoutButton.mouseClicked(mouseX, mouseY, mouseButton)
        folderButton.mouseClicked(mouseX, mouseY, mouseButton)
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton)
        } catch (_: IOException) {
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        currentCategory.mouseReleased(mouseX, mouseY, mouseButton)
        layoutButton.mouseReleased(mouseX, mouseY, mouseButton)
        folderButton.mouseReleased(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        currentCategory.keyTyped(typedChar, keyCode)
        searchBox.keyTyped(typedChar, keyCode)

        if (currentCategory.isShowSearchBox() && canClose) {
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                if (searchBox.getText().isNotEmpty()) {
                    searchBox.setText("")
                    searchBox.setFocused(false)
                    return
                }

                if (searchBox.isFocused()) {
                    searchBox.setFocused(false)
                    return
                }
            }
        }

        if (keyCode == Keyboard.KEY_ESCAPE && canClose) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    override fun onGuiClosed() {
        Shindo.getInstance().profileManager.save()
    }

    fun getX(): Int = x
    fun getY(): Int = y
    fun getWidth(): Int = menuWidth
    fun getHeight(): Int = menuHeight
    fun getCategories(): ArrayList<Category> = categories

    fun getCategoryByClass(clazz: Class<*>): Category {
        for (c in categories) {
            if (c.javaClass == clazz) {
                return c
            }
        }
        throw IllegalStateException("Category not found: " + clazz.name)
    }

    fun getScroll(): Scroll = scroll
    fun getSearchBox(): CompSearchBox = searchBox

    fun isCanClose(): Boolean = canClose
    fun setCanClose(canClose: Boolean) {
        this.canClose = canClose
    }
}
