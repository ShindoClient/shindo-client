package me.miki.shindo.ui.components.v1.layout.settingspanel

data class SettingsPanelStyle(
    val outerMargin: Float = 10f,
    val categoryGap: Float = 14f,
    val categoryHeaderHeight: Float = 22f,
    val categoryHeaderSpacing: Float = 6f,
    val categoryCardRadius: Float = 12f,
    val cardPaddingX: Float = 16f,
    val cardPaddingY: Float = 12f,
    val rowGap: Float = 8f,
    val columnGap: Float = 12f,
    val minRowHeightDefault: Float = 38f,
    val minRowHeightNarrow: Float = 52f,
    val minCardHeight: Float = 36f,
    val titleFontSize: Float = 9f,
    val descriptionFontSize: Float = 7.6f,
    val indicatorWidth: Float = 3.5f,
    val tooltipMaxWidth: Float = 320f,
    val componentPadding: Float = 12f,
    val textGap: Float = 16f,
    val narrowBreakpoint: Float = 360f,
    val virtualizationBuffer: Float = 56f
)
