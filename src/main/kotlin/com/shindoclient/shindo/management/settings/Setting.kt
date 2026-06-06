package com.shindoclient.shindo.management.settings

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.management.addons.Addon
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.settings.config.ConfigOwner
import com.shindoclient.shindo.management.settings.metadata.SettingMetadata
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.abs

open class Setting protected constructor(
    private val nameTranslate: TranslateText?,
    val parent: ConfigOwner,
    private val displayName: String,
    private val nameKey: String,
) {
    private var registered = false
    private var metadata: SettingMetadata? = null

    protected constructor(nameTranslate: TranslateText, parent: ConfigOwner) : this(
        nameTranslate,
        parent,
        nameTranslate.getText(),
        nameTranslate.getKey(),
    ) {
        register()
    }

    protected constructor(name: String, parent: ConfigOwner) : this(
        null,
        parent,
        name,
        buildKey(parent, name),
    ) {
        register()
    }

    private fun register() {
        if (registered) {
            return
        }

        when (parent) {
            is Mod -> Shindo.getInstance().getModManager().addSettings(this)
            is Addon -> Shindo.getInstance().getAddonManager().addSettings(this)
        }

        registered = true
    }

    open fun reset() {
    }

    val name: String
        get() = displayName

    fun getTranslate(): TranslateText? = nameTranslate

    fun getNameKey(): String {
        val data = metadata
        if (data != null && data.keyOverride.isNotEmpty()) {
            return data.keyOverride
        }
        return nameKey
    }

    fun getMetadata(): SettingMetadata? = metadata

    fun applyMetadata(metadata: SettingMetadata) {
        this.metadata = metadata
    }

    companion object {
        private val KEY_SANITIZE = Pattern.compile("[^a-z0-9]+")

        private fun buildKey(
            parent: ConfigOwner,
            raw: String,
        ): String {
            val candidate = normalizeKey(raw)
            return parent.getConfigId() + ":" + candidate
        }

        @JvmStatic
        fun normalizeKey(raw: String?): String {
            var candidate = raw ?: ""
            candidate =
                Normalizer
                    .normalize(candidate, Normalizer.Form.NFD)
                    .replace("\\p{M}".toRegex(), "")
                    .lowercase(Locale.ROOT)
            candidate = KEY_SANITIZE.matcher(candidate).replaceAll("_")
            candidate = candidate.replace("^_+".toRegex(), "").replace("_+$".toRegex(), "")

            if (candidate.isEmpty()) {
                val hashSource = raw ?: "null"
                candidate = "setting_" + abs(hashSource.hashCode())
            }

            return candidate
        }
    }
}
