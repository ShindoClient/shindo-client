package me.miki.extensions

import me.miki.extensions.core.ExtensionManager

object ExtensionLibrary {
    const val BASE_PACKAGE: String = "me.miki.extensions"
    const val VERSION: String = "0.1.0"

    fun bootstrap(manager: ExtensionManager) {
        registerUi(manager)
        registerNetwork(manager)
        registerUtils(manager)
        registerSerialization(manager)
    }


    private fun registerUi(manager: ExtensionManager) {
        val base = "$BASE_PACKAGE.ui"

        manager.register {
            id = "me.miki.extensions.ui.animation"
            namespace = "$base.animation"
            name = "Animation Extensions"
            version = VERSION
            description = "Convenience helpers for animation timelines, easing and color animations."
            tags("animation", "ui", "animation")
        }

        manager.register {
            id = "me.miki.extensions.ui.nanovg"
            namespace = "$base.nanovg"
            name = "NanoVG Extensions"
            version = VERSION
            description = "Extensions that wrap NanoVG manager helpers for shared render primitives."
            tags("nanovg", "graphics", "ui")
        }
    }

    private fun registerNetwork(manager: ExtensionManager) {
        val base = "$BASE_PACKAGE.network"

        manager.register {
            id = "me.miki.extensions.network.okhttp"
            namespace = "$base.okhttp"
            name = "OkHttp3 Extensions"
            version = VERSION
            description = "High-level HTTP helpers that wrap the OkHttp stack."
            tags("okhttp", "network")
        }

        manager.register {
            id = "me.miki.extensions.network.http"
            namespace = "$base.http"
            name = "HTTP Extension Placeholders"
            version = VERSION
            description = "High-level HTTP helpers."
            tags("http", "network")
        }


        manager.register {
            id = "me.miki.extensions.network.proxy"
            namespace = "$base.proxy"
            name = "Proxy Extensions"
            version = VERSION
            description = "Helpers around proxy configuration and health checks."
            tags("proxy", "network", "security")
        }
    }

    private fun registerUtils(manager: ExtensionManager) {
        val base = "$BASE_PACKAGE.utils"

        manager.register {
            id = "me.miki.extensions.utils.mouse"
            namespace = "$base.mouse"
            name = "Mouse Utilities Extensions"
            version = VERSION
            description = "Number-friendly helpers for MouseUtils hit tests."
            tags("utils", "mouse", "coordinates")
        }
    }

    private fun registerSerialization(manager: ExtensionManager) {
        val base = "$BASE_PACKAGE.serialization"

        manager.register {
            id = "me.miki.extensions.serialization.gson"
            namespace = "$base.gson"
            name = "Gson Extensions"
            version = VERSION
            description = "Gson helpers that reside alongside serialization DTOs."
            tags("gson", "json", "serialization")
        }
    }
}
