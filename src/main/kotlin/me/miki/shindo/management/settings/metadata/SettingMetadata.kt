package me.miki.shindo.management.settings.metadata

class SettingMetadata(
    val fieldName: String,
) {
    var category: String = ""
    var description: String = ""
    var keyOverride: String = ""
    var hidden: Boolean = false
    var min: Double = Double.NaN
    var max: Double = Double.NaN
    var step: Double = Double.NaN
}
