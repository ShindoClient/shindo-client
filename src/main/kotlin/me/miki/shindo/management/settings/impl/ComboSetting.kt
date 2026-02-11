package me.miki.shindo.management.settings.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.config.ConfigOwner
import me.miki.shindo.management.settings.impl.combo.Option

open class ComboSetting : Setting {

    private val options: MutableList<Option>
    private val defaultOption: Option?
    private var option: Option?

    constructor(text: TranslateText, parent: ConfigOwner, defaultOption: TranslateText, options: List<Option>) : super(
        text,
        parent
    ) {
        this.options = ArrayList(options)
        this.option = getOptionByKey(defaultOption.getKey())
        this.defaultOption = this.option
    }

    constructor(text: TranslateText, parent: ConfigOwner, defaultOptionKey: String, options: List<Option>) : super(
        text,
        parent
    ) {
        this.options = ArrayList(options)
        this.option = getOptionByKey(defaultOptionKey)
        if (this.option == null && this.options.isNotEmpty()) {
            this.option = this.options[0]
        }
        this.defaultOption = this.option
    }

    constructor(name: String, parent: ConfigOwner, defaultOptionKey: String, options: List<Option>) : super(
        name,
        parent
    ) {
        this.options = ArrayList(options)
        this.option = getOptionByKey(defaultOptionKey)
        if (this.option == null && this.options.isNotEmpty()) {
            this.option = this.options[0]
        }
        this.defaultOption = this.option
    }

    override fun reset() {
        option = defaultOption
    }

    fun getOption(): Option? {
        return option
    }

    open fun setOption(option: Option?) {
        this.option = option
    }

    fun getOptions(): List<Option> {
        return options
    }

    fun getDefaultOption(): Option? {
        return defaultOption
    }

    fun getOptionByNameKey(nameKey: String): Option? {
        return getOptionByKey(nameKey)
    }

    private fun getOptionByKey(key: String): Option? {
        for (op in options) {
            if (op.nameKey.equals(key, ignoreCase = true)) {
                return op
            }
        }
        return if (options.isEmpty()) null else options[0]
    }
}
