package com.shindoclient.shindo.ui.components.v2.chips

import com.shindoclient.shindo.utils.mouse.MouseUtils

class FilterChip(
    private val onClick: Runnable?,
) {
    private var x = 0f
    private var y = 0f
    private var width = 0f
    private var height = 0f

    fun setBounds(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    fun contains(
        mx: Int,
        my: Int,
    ): Boolean = MouseUtils.isInside(mx, my, x, y, width, height)

    fun click() {
        onClick?.run()
    }
}
