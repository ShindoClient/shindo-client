package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo
import me.miki.shindo.injection.interfaces.IMixinMinecraft
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventKey
import me.miki.shindo.management.event.impl.EventPreRenderTick
import me.miki.shindo.management.event.impl.EventToggleFullscreen
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.management.settings.impl.ComboSetting
import me.miki.shindo.management.settings.impl.KeybindSetting
import me.miki.shindo.management.settings.impl.NumberSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getBooleanSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getComboSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getKeybindSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getNumberSetting
import me.miki.shindo.ui.animation.v2.GlobalAnimationSettings
import me.miki.shindo.ui.components.v2.layout.SettingsPanel.LayoutMode
import org.lwjgl.LWJGLException
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import java.util.*
import kotlin.math.max
import kotlin.math.min

class InternalSettingsMod : Mod(TranslateText.NONE, TranslateText.NONE, ModCategory.OTHER, LegacyIcon.MOD_INTERNAL_SETTINGS) {
    @Property(type = PropertyType.COMBO, translate = TranslateText.HUD_THEME)
    @JvmField
    val hudTheme: HudTheme = HudTheme.NORMAL

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.UI_BLUR)
    @JvmField
    var blurSetting = false

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.BLUR_STRENGTH,
        min = 10.0,
        max = 20.0,
        current = 1.0,
    )
    @JvmField
    var blurStrengthSetting = 1.0

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ANIMATION)
    @JvmField
    var animationsSetting = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.MC_FONT)
    private val mcFontSetting = false

    @Property(type = PropertyType.NUMBER, translate = TranslateText.VOLUME, min = 0.0, max = 1.0, current = 0.8)
    @JvmField
    var volumeSetting = 0.8

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.KEYBIND, keyCode = Keyboard.KEY_RSHIFT)
    private val modMenuKeybindSetting = Keyboard.KEY_RSHIFT

    @Property(type = PropertyType.TEXT, translate = TranslateText.CUSTOM_CAPE, text = "None")
    var capeConfigName: String? = "None"

    @Property(type = PropertyType.TEXT, name = "Custom Wing", text = "None")
    var wingConfigName: String? = "None"

    @Property(type = PropertyType.TEXT, name = "Custom Bandana", text = "None")
    var bandanaConfigName: String? = "None"

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CLICK_EFFECT)
    @JvmField
    var clickEffectsSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.UI_SOUNDS)
    @JvmField
    var soundsUISetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BORDERLESS_FULSCREEN)
    @JvmField
    val borderlessFullscreenSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.PERFORMANCE_TEXTURE_OPTIMIZATION)
    @JvmField
    var textureOptimizationSetting = true

    @Property(type = PropertyType.COMBO, name = "Settings Layout")
    val settingsLayout: SettingsLayout = SettingsLayout.SINGLE_COLUMN

    @Property(type = PropertyType.COMBO, name = "Visual Preset")
    @JvmField
    val visualPreset: VisualPreset = VisualPreset.MODERN

    @Property(type = PropertyType.COMBO, name = "Module Layout")
    @JvmField
    val moduleLayout = ModuleLayout.SINGLE_COLUMN

    @Property(type = PropertyType.COMBO, translate = TranslateText.NOTIFICATION_POSITION)
    var notificationCorner: NotificationCorner = NotificationCorner.BOTTOM_RIGHT
        get() = readEnumFromCombo(notificationCornerSetting, NotificationCorner.values(), field)
        set(corner) {
            writeEnumToCombo(this.notificationCornerSetting, corner)
            field = corner
        }

    private var prevX = 0
    private var prevY = 0
    private var prevWidth = 0
    private var prevHeight = 0
    private var fullscreenTime: Long = -1
    private var lastBorderlessState = false
    private var borderlessInitialized = false
    private var lastModMenuOpenTime = 0L

    init {
        instance = this
    }

    override fun setup() {
        this.setHide(true)
        this.setToggled(true)
    }

    @EventTarget
    fun onKey(event: EventKey) {
        val keybind = getModMenuKeybindSetting()?.getKeyCode() ?: modMenuKeybindSetting
        if (event.keyCode == keybind) {
            mc.displayGuiScreen(Shindo.getInstance().getShindoAPI().navigationHub)
        }

        if (event.keyCode == Keyboard.KEY_DOWN) {
            val combo = this.modThemeSetting
            if (combo != null) {
                val max = combo.getOptions().size
                var modeIndex = combo.getOptions().indexOf(combo.getOption())
                modeIndex = if (modeIndex > 0) modeIndex - 1 else max - 1
                combo.setOption(combo.getOptions()[modeIndex])
            }
        }
    }

    @EventTarget
    fun onRenderTick(event: EventPreRenderTick?) {
        GlobalAnimationSettings.enabled = animationsSetting
        if (fullscreenTime != -1L && System.currentTimeMillis() - fullscreenTime >= 100) {
            fullscreenTime = -1

            if (mc.inGameHasFocus) {
                mc.mouseHelper.grabMouseCursor()
            }
        }

        if (borderlessInitialized && borderlessFullscreenSetting != lastBorderlessState) {
            applyBorderlessSetting(borderlessFullscreenSetting, false)
        }
    }

    @EventTarget
    fun onFullscreenToggle(event: EventToggleFullscreen) {
        if (!borderlessFullscreenSetting) {
            return
        }
        event.isApplyState = false
        setBorderlessFullscreen(event.state)
    }

    fun getModuleLayout(): ModuleLayout = readEnumFromCombo(moduleLayoutSetting, ModuleLayout.values(), moduleLayout)

    private val settingsLayoutSetting: ComboSetting?
        get() = getComboSetting(this, "settingsLayout")

    private val visualPresetSetting: ComboSetting?
        get() = getComboSetting(this, "visualPreset")

    private val moduleLayoutSetting: ComboSetting?
        get() = getComboSetting(this, "moduleLayout")

    private val notificationCornerSetting: ComboSetting?
        get() = getComboSetting(this, "notificationCorner")

    var settingsLayoutMode: LayoutMode?
        get() =
            when (readEnumFromCombo(settingsLayoutSetting, SettingsLayout.values(), settingsLayout)) {
                SettingsLayout.DOUBLE_COLUMN -> LayoutMode.DOUBLE_COLUMN
                SettingsLayout.ADAPTIVE_GRID -> LayoutMode.STAGGERED_COLUMNS
                else -> LayoutMode.SINGLE_COLUMN
            }
        set(mode) {
            val target =
                when (mode) {
                    LayoutMode.DOUBLE_COLUMN -> SettingsLayout.DOUBLE_COLUMN
                    LayoutMode.STAGGERED_COLUMNS -> SettingsLayout.ADAPTIVE_GRID
                    else -> SettingsLayout.SINGLE_COLUMN
                }
            writeEnumToCombo(this.settingsLayoutSetting, target)
        }

    var moduleGridColumns: Int
        get() {
            if (Objects.requireNonNull<ModuleLayout?>(getModuleLayout()) == ModuleLayout.TWO_COLUMNS) {
                return 2
            }
            return 1
        }
        set(columns) {
            val normalized = max(1, min(columns, 2))
            val target = if (normalized == 2) ModuleLayout.TWO_COLUMNS else ModuleLayout.SINGLE_COLUMN
            writeEnumToCombo(this.moduleLayoutSetting, target)
        }

    fun getVisualPreset(): VisualPreset = readEnumFromCombo(visualPresetSetting, VisualPreset.values(), visualPreset)

    fun setVisualPreset(preset: VisualPreset?) {
        val target = preset ?: VisualPreset.MODERN
        writeEnumToCombo(this.visualPresetSetting, target)
    }

    fun setModuleLayout(layout: ModuleLayout?) {
        val target = layout ?: ModuleLayout.SINGLE_COLUMN
        writeEnumToCombo(this.moduleLayoutSetting, target)
    }

    val modThemeSetting: ComboSetting?
        get() = getComboSetting(this, "hudTheme")

    fun getModMenuKeybindSetting(): KeybindSetting? = getKeybindSetting(this, "modMenuKeybindSetting")

    val mCHUDFont: BooleanSetting?
        get() = getBooleanSetting(this, "mcFontSetting")

    fun getBorderlessFullscreenSetting(): BooleanSetting? = getBooleanSetting(this, "borderlessFullscreenSetting")

    fun getBlurSetting(): BooleanSetting? = getBooleanSetting(this, "blurSetting")

    fun getBlurStrengthSetting(): NumberSetting? = getNumberSetting(this, "blurStrengthSetting")

    fun getAnimationsSetting(): BooleanSetting? = getBooleanSetting(this, "animationsSetting")

    fun getVolumeSetting(): NumberSetting? = getNumberSetting(this, "volumeSetting")

    fun getClickEffectsSetting(): BooleanSetting? = getBooleanSetting(this, "clickEffectsSetting")

    fun getSoundsUISetting(): BooleanSetting? = getBooleanSetting(this, "soundsUISetting")

    fun getTextureOptimizationSetting(): BooleanSetting? = getBooleanSetting(this, "textureOptimizationSetting")

    private fun <T : Enum<T>> readEnumFromCombo(
        combo: ComboSetting?,
        values: Array<T>,
        fallback: T,
    ): T {
        if (combo == null) {
            return fallback
        }
        val selected = combo.getOption() ?: return fallback
        val index = combo.getOptions().indexOf(selected)
        return if (index >= 0 && index < values.size) values[index] else fallback
    }

    private fun <T : Enum<T>> writeEnumToCombo(
        combo: ComboSetting?,
        value: T,
    ) {
        if (combo == null) {
            return
        }
        if (value.ordinal >= 0 && value.ordinal < combo.getOptions().size) {
            combo.setOption(combo.getOptions()[value.ordinal])
        }
    }

    fun applyBorderlessOnStartup() {
        borderlessInitialized = true
        lastBorderlessState = borderlessFullscreenSetting
        applyBorderlessSetting(borderlessFullscreenSetting, true)
    }

    private fun applyBorderlessSetting(
        state: Boolean,
        force: Boolean,
    ) {
        if (!force && state == lastBorderlessState) {
            return
        }

        lastBorderlessState = state
        borderlessInitialized = true

        if (!mc.isFullScreen) {
            return
        }

        if (state) {
            setBorderlessFullscreen(true)
        } else {
            setBorderlessFullscreen(false)
            mc.toggleFullscreen()
            mc.toggleFullscreen()
        }
    }

    private fun setBorderlessFullscreen(state: Boolean) {
        try {
            System.setProperty("org.lwjgl.opengl.Window.undecorated", state.toString())
            Display.setFullscreen(false)
            Display.setResizable(!state)

            if (state) {
                prevX = Display.getX()
                prevY = Display.getY()
                prevWidth = mc.displayWidth
                prevHeight = mc.displayHeight
                Display.setDisplayMode(
                    DisplayMode(
                        Display.getDesktopDisplayMode().width,
                        Display.getDesktopDisplayMode().height,
                    ),
                )
                Display.setLocation(0, 0)
                (mc as IMixinMinecraft).resizeWindow(
                    Display.getDesktopDisplayMode().width,
                    Display.getDesktopDisplayMode().height,
                )
            } else {
                Display.setDisplayMode(DisplayMode(prevWidth, prevHeight))
                Display.setLocation(prevX, prevY)
                (mc as IMixinMinecraft).resizeWindow(prevWidth, prevHeight)

                if (mc.inGameHasFocus) {
                    mc.mouseHelper.ungrabMouseCursor()
                    fullscreenTime = System.currentTimeMillis()
                }
            }
        } catch (error: LWJGLException) {
            ShindoLogger.error("Could not toggle borderless fullscreen", error)
        }
    }

    enum class HudTheme(
        private val translate: TranslateText,
    ) : PropertyEnum {
        NORMAL(TranslateText.NORMAL),
        GLOW(TranslateText.GLOW),
        OUTLINE(TranslateText.OUTLINE),
        VANILLA(TranslateText.VANILLA),
        OUTLINE_GLOW(TranslateText.OUTLINE_GLOW),
        VANILLA_GLOW(TranslateText.VANILLA_GLOW),
        SHADOW(TranslateText.SHADOW),
        DARK(TranslateText.DARK),
        LIGHT(TranslateText.LIGHT),
        RECT(TranslateText.RECT),
        MODERN(TranslateText.MODERN),
        TEXT(TranslateText.TEXT),
        GRADIENT_SIMPLE(TranslateText.GRADIENT_SIMPLE),
        ;

        override fun getTranslate(): TranslateText = translate
    }

    enum class SettingsLayout(
        displayName: String,
    ) : PropertyEnum {
        SINGLE_COLUMN("Single Column"),
        DOUBLE_COLUMN("Double Column"),
        ADAPTIVE_GRID("Adaptive Grid"),
        ;

        private val displayName: String = displayName

        override fun getDisplayName(): String = displayName
    }

    enum class ModuleLayout(
        displayName: String,
    ) : PropertyEnum {
        SINGLE_COLUMN("Single Column"),
        TWO_COLUMNS("Two Columns"),
        ;

        private val displayName: String = displayName

        override fun getDisplayName(): String = displayName
    }

    enum class VisualPreset(
        displayName: String,
    ) : PropertyEnum {
        CLASSIC("Classic"),
        MODERN("Modern"),
        LIGHT("Light"),
        DARK("Dark"),
        ;

        private val displayName: String = displayName

        override fun getDisplayName(): String = displayName
    }

    enum class NotificationCorner(
        private val translate: TranslateText,
    ) : PropertyEnum {
        TOP_LEFT(TranslateText.NOTIFICATION_POSITION_TOP_LEFT),
        TOP_RIGHT(TranslateText.NOTIFICATION_POSITION_TOP_RIGHT),
        BOTTOM_LEFT(TranslateText.NOTIFICATION_POSITION_BOTTOM_LEFT),
        BOTTOM_RIGHT(TranslateText.NOTIFICATION_POSITION_BOTTOM_RIGHT),
        ;

        override fun getTranslate(): TranslateText = translate
    }

    companion object {
        lateinit var instance: InternalSettingsMod
    }
}
