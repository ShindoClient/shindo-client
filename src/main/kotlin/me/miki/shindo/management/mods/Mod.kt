package me.miki.shindo.management.mods

import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.notification.NotificationType
import me.miki.shindo.management.settings.config.ConfigOwner
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer

open class Mod
    @JvmOverloads
    constructor(
        private val nameTranslate: TranslateText,
        private val descriptionTranslate: TranslateText,
        private var category: ModCategory,
        private val icon: String? = null,
        private val alias: String = "\u200B",
        private val restricted: Boolean = false,
    ) : ConfigOwner {
        val animation = SimpleAnimation()
        val hoverAnimation = SimpleAnimation()
        val settingsHoverAnimation = SimpleAnimation()

        @JvmField
        val mc: Minecraft = Minecraft.getMinecraft()

        @JvmField
        val fr: FontRenderer = mc.fontRendererObj

        private var toggled = false
        private var hide = false
        private var allowed = true

        init {
            setup()
        }

        open fun setup() {
        }

        open fun onEnable() {
            if (Shindo.getInstance().getRestrictedMod().checkAllowed(this)) {
                Shindo.getInstance().getEventManager().register(this)
                ShindoLogger.info("[MODULE] " + getName() + " was enabled")
            } else {
                setToggled(false)
                Shindo.getInstance().getNotificationManager().post(
                    nameTranslate.getText(),
                    "Disabled due to serverside blacklist",
                    NotificationType.INFO,
                )
            }
        }

        open fun onDisable() {
            Shindo.getInstance().getEventManager().unregister(this)
            ShindoLogger.info("[MODULE] " + getName() + " was disabled")
        }

        fun toggle() {
            setToggled(!toggled, true)
        }

        fun setToggled(
            toggled: Boolean,
            sound: Boolean,
        ) {
            this.toggled = toggled

            if (toggled) {
                onEnable()
                if (sound) Shindo.getInstance().getModManager().playToggleSound(true)
            } else {
                onDisable()
                if (sound) Shindo.getInstance().getModManager().playToggleSound(false)
            }
        }

        fun setToggled(toggled: Boolean) {
            setToggled(toggled, false)
        }

        fun isToggled(): Boolean = toggled

        fun isHide(): Boolean = hide

        fun setHide(hide: Boolean) {
            this.hide = hide
        }

        fun getCategory(): ModCategory = category

        fun setCategory(category: ModCategory) {
            this.category = category
        }

        fun getAlias(): String = alias

        fun getRestricted(): Boolean = restricted

        fun isAllowed(): Boolean = allowed

        fun setAllowed(modAllowed: Boolean) {
            allowed = modAllowed
        }

        fun getName(): String = nameTranslate.getText()

        fun getDescription(): String = descriptionTranslate.getText()

        open fun getIcon(): String? = icon

        fun getMenuIcon(): String? = icon

        fun getNameKey(): String = nameTranslate.getKey()

        fun isRestricted(): Boolean = restricted

        override fun getConfigId(): String = getNameKey()

        override fun getDisplayName(): String = getName()
    }
