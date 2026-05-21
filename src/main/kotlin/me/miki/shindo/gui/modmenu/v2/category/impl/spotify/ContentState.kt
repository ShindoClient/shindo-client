package me.miki.shindo.gui.modmenu.v2.category.impl.spotify

sealed class ContentState<out T> {
    object Idle : ContentState<Nothing>()

    object Loading : ContentState<Nothing>()

    data class Ready<T>(
        val data: T,
    ) : ContentState<T>()

    data class Error(
        val message: String,
    ) : ContentState<Nothing>()
}
