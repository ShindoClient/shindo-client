package me.miki.shindo.gui.modmenu.v2.category.impl.spotify

class SpotifyNavigator {
    private val stack = ArrayDeque<SpotifyScreen>().apply { add(SpotifyScreen.Library) }

    val current: SpotifyScreen get() = stack.last()
    val canGoBack: Boolean get() = stack.size > 1

    fun push(screen: SpotifyScreen) {
        if (stack.last() != screen) stack.addLast(screen)
    }

    fun pop(): SpotifyScreen {
        if (stack.size > 1) stack.removeLast()
        return stack.last()
    }

    fun popTo(screen: SpotifyScreen) {
        while (stack.size > 1 && stack.last() != screen) stack.removeLast()
    }

    fun reset() {
        stack.clear()
        stack.add(SpotifyScreen.Library)
    }
}
