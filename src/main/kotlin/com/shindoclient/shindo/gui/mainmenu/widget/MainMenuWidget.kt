package com.shindoclient.shindo.gui.mainmenu.widget

interface MainMenuWidget {
    val anchor: WidgetAnchor

    val anchorPadding: Float get() = 10f

    val width: Float

    val height: Float

    val enabled: Boolean get() = true

    fun draw(
        ctx: MainMenuWidgetContext,
        x: Float,
        y: Float,
    )

    fun mouseClicked(
        ctx: MainMenuWidgetContext,
        x: Float,
        y: Float,
        mouseButton: Int,
    ): Boolean = false

    fun mouseScrolled(
        ctx: MainMenuWidgetContext,
        x: Float,
        y: Float,
        amount: Int,
    ): Boolean = false

    fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ): Boolean = false

    fun onSceneInit() {}
}
