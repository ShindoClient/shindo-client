package com.shindoclient.shindo.management.addons

import com.shindoclient.addon.api.ShindoAddon
import com.shindoclient.addon.api.config.PROPERTY_SENTINEL_DOUBLE
import com.shindoclient.addon.api.config.Property
import com.shindoclient.addon.api.config.PropertyType
import com.shindoclient.addon.api.setting.Setting
import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.logger.ShindoLogger
import com.shindoclient.shindo.management.addons.bridge.AddonBridge
import com.shindoclient.shindo.management.addons.bridge.AddonEventBridge
import com.shindoclient.shindo.management.addons.bridge.AddonHUDMod
import com.shindoclient.shindo.management.addons.bridge.command.CommandBridge
import com.shindoclient.shindo.management.addons.bridge.render.AddonNanoVGBridge
import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventRender2D
import com.shindoclient.shindo.management.settings.impl.BooleanSetting
import com.shindoclient.shindo.management.settings.impl.ColorSetting
import com.shindoclient.shindo.management.settings.impl.ComboSetting
import com.shindoclient.shindo.management.settings.impl.KeybindSetting
import com.shindoclient.shindo.management.settings.impl.NumberSetting
import com.shindoclient.shindo.management.settings.impl.TextSetting
import com.shindoclient.shindo.management.settings.impl.combo.Option
import java.awt.Color
import java.lang.reflect.Field
import java.net.URLClassLoader
import kotlin.math.roundToInt

class ExternalAddon(
    name: String,
    description: String,
    icon: String,
    typeName: String,
    val shindoAddon: ShindoAddon,
    val classLoader: URLClassLoader,
) : Addon(name, description, null, icon, parseType(typeName)) {
    override val isBuiltIn = false
    override val showToggle: Boolean = true

    private val hudMods = mutableListOf<AddonHUDMod>()
    private var enabled = false
    private var addonInitialized = false

    private var nanoVGBridge: AddonNanoVGBridge? = null
    private var eventBridge: AddonEventBridge? = null
    private var commandBridge: CommandBridge? = null

    private val addonBridge = AddonBridge()

    fun initAddon() {
        if (addonInitialized) return
        addonInitialized = true
        bridgePropertyFields()
        bridgeSettings()
        bridgeHuds()
    }

    override fun onEnable() {
        super.onEnable()
        if (enabled) return
        enabled = true

        try {
            shindoAddon.onEnable()
        } catch (e: Exception) {
            ShindoLogger.error("$name: error during onEnable", e)
        }

        initBridges()
    }

    override fun onDisable() {
        super.onDisable()
        if (!enabled) return
        enabled = false

        destroyBridges()

        try {
            shindoAddon.onDisable()
        } catch (e: Exception) {
            ShindoLogger.error("$name: error during onDisable", e)
        }
    }

    private fun initBridges() {
        nanoVGBridge = addonBridge.createNanoVGBridge()

        val bridge = AddonEventBridge(shindoAddon.eventManager)
        eventBridge = bridge
        addonBridge.registerEventBridge(bridge)

        bridgeCommands()
    }

    private fun destroyBridges() {
        eventBridge?.let { addonBridge.unregisterEventBridge(it) }
        eventBridge = null
        nanoVGBridge = null
        commandBridge?.unregisterAll()
        commandBridge = null
    }

    private fun bridgeCommands() {
        if (shindoAddon.commands.isEmpty()) return
        val bridge = CommandBridge()
        commandBridge = bridge
        for (addonCommand in shindoAddon.commands) {
            try {
                bridge.registerCommand(addonCommand)
                ShindoLogger.info("$name: registered command '${addonCommand.prefix}'")
            } catch (e: Exception) {
                ShindoLogger.error("$name: failed to register command '${addonCommand.prefix}'", e)
            }
        }
    }

    private fun bridgePropertyFields() {
        val addonClass = shindoAddon::class.java
        for (field in addonClass.declaredFields) {
            val prop = field.getAnnotation(Property::class.java) ?: continue
            try {
                bridgePropertyField(field, prop)
            } catch (e: Exception) {
                ShindoLogger.error("$name: failed to bridge @Property field '${field.name}'", e)
            }
        }
    }

    private fun bridgePropertyField(
        field: Field,
        prop: Property,
    ) {
        field.isAccessible = true
        val settingName = prop.name.ifBlank { field.name }

        when (prop.type) {
            PropertyType.BOOLEAN -> {
                val current = field.getBoolean(shindoAddon)
                BooleanSetting(settingName, this, current)
            }

            PropertyType.NUMBER -> {
                val current = field.getDouble(shindoAddon)
                val min = if (prop.min == PROPERTY_SENTINEL_DOUBLE) 0.0 else prop.min
                val max = if (prop.max == PROPERTY_SENTINEL_DOUBLE) 100.0 else prop.max
                val step = if (prop.step == PROPERTY_SENTINEL_DOUBLE) 1.0 else prop.step
                val integer = step >= 1.0 && step.roundToInt().toDouble() == step
                NumberSetting(settingName, this, current.coerceIn(min, max), min, max, integer)
            }

            PropertyType.TEXT -> {
                val current = field.get(shindoAddon) as? String ?: prop.text
                TextSetting(settingName, this, current)
            }

            PropertyType.COLOR -> {
                val current = field.getInt(shindoAddon)
                ColorSetting(settingName, this, Color(current), prop.showAlpha)
            }

            PropertyType.KEYBIND -> {
                val current = field.getInt(shindoAddon)
                KeybindSetting(settingName, this, current)
            }

            PropertyType.COMBO -> {
                val options =
                    if (prop.enumName.isNotBlank()) {
                        try {
                            val enumClass = shindoAddon::class.java.classLoader.loadClass(prop.enumName)
                            enumClass.enumConstants.map { it.toString() }
                        } catch (_: Exception) {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                if (options.isNotEmpty()) {
                    val optionList = options.map { Option(it) }
                    val fieldValue = field.get(shindoAddon)?.toString() ?: ""
                    val selectedIndex =
                        options.indexOfFirst { it.equals(fieldValue, ignoreCase = true) }.coerceAtLeast(0)
                    val defaultKey = optionList[selectedIndex].nameKey
                    ComboSetting(settingName, this, defaultKey, optionList)
                }
            }

            else -> {
                ShindoLogger.warn("$name: unsupported @Property type ${prop.type} on field '${field.name}'")
            }
        }
    }

    private fun bridgeSettings() {
        for (apiSetting in shindoAddon.settings) {
            try {
                bridgeApiSetting(apiSetting)
            } catch (e: Exception) {
                ShindoLogger.error("$name: failed to bridge setting '${apiSetting.name}'", e)
            }
        }
    }

    private fun bridgeApiSetting(apiSetting: Setting) {
        val name = apiSetting.name

        when (apiSetting) {
            is com.shindoclient.addon.api.setting.impl.BooleanSetting -> {
                BooleanSetting(name, this, apiSetting.get() as Boolean)
            }

            is com.shindoclient.addon.api.setting.impl.NumberSetting -> {
                val value = apiSetting.get() as Double
                NumberSetting(name, this, value, apiSetting.minimum, apiSetting.maximum, apiSetting.step >= 1.0)
            }

            is com.shindoclient.addon.api.setting.impl.TextSetting -> {
                TextSetting(name, this, apiSetting.get() as String)
            }

            is com.shindoclient.addon.api.setting.impl.ColorSetting -> {
                val colorInt = apiSetting.get() as Int
                ColorSetting(name, this, Color(colorInt), false)
            }

            is com.shindoclient.addon.api.setting.impl.ComboSetting -> {
                val options = apiSetting.options.map { Option(it) }
                val selectedIndex = apiSetting.get() as Int
                val defaultKey = if (selectedIndex in options.indices) options[selectedIndex].nameKey else ""
                ComboSetting(name, this, defaultKey, options)
            }

            is com.shindoclient.addon.api.setting.impl.KeybindSetting -> {
                KeybindSetting(name, this, apiSetting.get() as Int)
            }

            else -> {
                ShindoLogger.warn("$name: unknown setting type for '${apiSetting.name}'")
            }
        }
    }

    private fun bridgeHuds() {
        for (hud in shindoAddon.huds) {
            try {
                val mod = addonBridge.wrapHUD(hud)
                hudMods.add(mod)
                addonBridge.registerHUD(mod)
            } catch (e: Exception) {
                ShindoLogger.error("$name: failed to bridge HUD '${hud.name}'", e)
            }
        }
    }

    fun unload() {
        if (enabled) {
            onDisable()
        }
        for (hudMod in hudMods) {
            addonBridge.unregisterHUD(hudMod)
        }
        hudMods.clear()
        try {
            classLoader.close()
        } catch (_: Exception) {
        }
    }

    fun renderHuds(partialTicks: Float) {
        val huds = hudMods.toList()
        if (huds.isEmpty()) return

        val nvgBridge = nanoVGBridge ?: return

        Shindo.getInstance().nanoVGManager.setupAndDraw {
            for (hudMod in huds) {
                if (!hudMod.isToggled()) continue
                hudMod.syncToAddonHUD()
                try {
                    nvgBridge.save()
                    nvgBridge.translate(hudMod.getX().toFloat(), hudMod.getY().toFloat())

                    val context =
                        addonBridge.createHUDContext(
                            hudWidth = hudMod.addonHUD.width,
                            hudHeight = hudMod.addonHUD.height,
                            partialTicks = partialTicks,
                            nanoVG = nvgBridge,
                        )
                    hudMod.addonHUD.onRender(context)

                    nvgBridge.restore()
                } catch (e: Exception) {
                    ShindoLogger.error("Error rendering HUD '${hudMod.getName()}'", e)
                }
            }
        }
    }

    @EventTarget
    fun onRender2D(event: EventRender2D) {
        if (event.isCancelled()) return
        renderHuds(event.partialTicks)
    }

    companion object {
        fun parseType(typeName: String): AddonType =
            when (typeName.uppercase()) {
                "RENDER" -> AddonType.RENDER
                "QOL" -> AddonType.QOL
                else -> AddonType.OTHER
            }
    }
}
