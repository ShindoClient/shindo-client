package me.miki.extensions

import me.miki.extensions.manager.ExtensionManager

/**
 * Bootstrap point for the extensions library.
 */
object ExtensionLibrary {
    const val BASE_PACKAGE: String = "me.miki.extensions"
    const val VERSION: String = "0.1.0"

    fun bootstrap(manager: ExtensionManager) {
        registerCore(manager)
        registerManagement(manager)
        registerUi(manager)
        registerNetwork(manager)
        registerUtils(manager)
        registerSerialization(manager)
    }

    private fun registerCore(manager: ExtensionManager) {
        manager.register {
            id = "me.miki.extensions.core"
            name = "Shindo Kotlin Extensions"
            version = VERSION
            description = "Helper extensions that bridge Shindo core services with Kotlin consumers."
            tags("core", "shindo", "kotlin")
        }

        manager.register {
            id = "me.miki.extensions.core.shindo"
            namespace = "$BASE_PACKAGE"
            name = "Shindo Service Accessors"
            version = VERSION
            description = "Kotlin helpers that expose Shindo manager accessors via `ShindoExtensions`."
            tags("core", "shindo", "accessors")
        }
    }

    private fun registerManagement(manager: ExtensionManager) {
        val base = "$BASE_PACKAGE.management"

        manager.register {
            id = "me.miki.extensions.management.modules"
            namespace = "$base.modules"
            name = "Module & ModManager Extensions"
            version = VERSION
            description = "Helpers for Mod/Module queries, settings, and owner metadata."
            tags("modules", "mods", "settings")
        }

        manager.register {
            id = "me.miki.extensions.management.addons"
            namespace = "$base.addons"
            name = "AddonManager Extensions"
            version = VERSION
            description = "Helpers focused on addon lookup, filtering and projection."
            tags("addons", "ui", "management")
        }

        manager.register {
            id = "me.miki.extensions.management.profiles"
            namespace = "$base.profiles"
            name = "ProfileManager Extensions"
            version = VERSION
            description = "Profile helpers that make persistence/lookup easier for UI flows."
            tags("profiles", "persistence", "settings")
        }

        manager.register {
            id = "me.miki.extensions.management.settings"
            namespace = "$base.settings"
            name = "Settings Registry Extensions"
            version = VERSION
            description = "Typed filters and metadata helpers for the core settings registry."
            tags("settings", "registry", "metadata")
        }

        listOf("color", "notification", "event").forEach { key ->
            manager.register {
                id = "me.miki.extensions.management.$key"
                namespace = "$base.$key"
                name = "${key.capitalize()} Extensions"
                version = VERSION
                description = "Placeholder helpers for the ${key} system."
                tags(key, "placeholder")
            }
        }
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
            id = "me.miki.extensions.ui.layout"
            namespace = "$base.layout"
            name = "Layout Extensions"
            version = VERSION
            description = "Layout helpers to compose UILayoutManager scenes and coordinates."
            tags("layout", "scene", "ui")
        }

        manager.register {
            id = "me.miki.extensions.ui.accent"
            namespace = "$base.accent"
            name = "Accent Color Extensions"
            version = VERSION
            description = "Helpers for accent color/palette decisions across the UI."
            tags("accent", "color", "ui")
        }

        manager.register {
            id = "me.miki.extensions.ui.theme"
            namespace = "$base.theme"
            name = "Theme Extensions"
            version = VERSION
            description = "Extensions that support theme selection and palette interrogations."
            tags("theme", "color", "ui")
        }

        manager.register {
            id = "me.miki.extensions.ui.graphics.nanovg"
            namespace = "$base.graphics.nanovg"
            name = "NanoVG Extensions"
            version = VERSION
            description = "Extensions that wrap NanoVG manager helpers for shared render primitives."
            tags("nanovg", "graphics", "ui")
        }
    }

    private fun registerNetwork(manager: ExtensionManager) {
        val base = "$BASE_PACKAGE.network"

        manager.register {
            id = "me.miki.extensions.network.core"
            namespace = "$base.core"
            name = "NetworkManager Extensions"
            version = VERSION
            description = "Extension helpers focused on centralized network operations."
            tags("network", "core", "manager")
        }

        manager.register {
            id = "me.miki.extensions.network.http"
            namespace = "$base.http"
            name = "HTTP Extension Placeholders"
            version = VERSION
            description = "High-level HTTP helpers that wrap the OkHttp stack."
            tags("http", "okhttp", "network")
        }

        manager.register {
            id = "me.miki.extensions.network.http.okhttp"
            namespace = "$base.http.okhttp"
            name = "OkHttp Extensions"
            version = VERSION
            description = "Helpers that speak directly with OkHttp requests/responses."
            tags("http", "okhttp", "request")
        }

        manager.register {
            id = "me.miki.extensions.network.websocket"
            namespace = "$base.websocket"
            name = "WebSocket Extensions"
            version = VERSION
            description = "Helpers that simplify websocket usage (JavaWebSocket, proxies)."
            tags("websocket", "network", "ws")
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

        manager.register {
            id = "me.miki.extensions.serialization.kotlinx"
            namespace = "$base.kotlinx"
            name = "Kotlinx Serialization Extensions"
            version = VERSION
            description = "Placeholders for kotlinx.serialization helpers when adopted."
            tags("kotlinx", "serialization", "json")
        }
    }
}
