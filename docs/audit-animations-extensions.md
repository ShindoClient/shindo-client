# Animations & Extensions Audit

## 1) Extensions inventory
- `src/main/kotlin/me/miki/extensions/ExtensionLibrary.kt` — Bootstrap singleton holding `BASE_PACKAGE` and `VERSION`; no extensions; object pattern; root package `me.miki.extensions`.
- `src/main/kotlin/me/miki/extensions/ShindoExt.kt` — Top-level extension functions on `Shindo` exposing managers (mod, profile, color, download, event, music, notification, network, file, NanoVG, language); uses `@file:JvmName("ShindoExtensions")`; file name uses `Ext` suffix while JvmName uses plural.
- `src/main/kotlin/me/miki/extensions/addons/AddonManagerExtensions.kt` — Placeholder TODO for AddonManager helpers; `@file:JvmName("AddonManagerExtensions")`; no receivers yet.
- `src/main/kotlin/me/miki/extensions/graphics/nanovg/NanoVGManagerExtensions.kt` — TODO scaffold for NanoVGManager drawing helpers; `@file:JvmName("NanoVGManagerExtensions")`.
- `src/main/kotlin/me/miki/extensions/modules/ModManagerExt.kt` — TODO scaffold for ModManager helpers; `@file:JvmName("ModManagerExtensions")`; filename `ModManagerExt` vs JvmName plural.
- `src/main/kotlin/me/miki/extensions/modules/ModuleExt.kt` — Real extensions on `Mod` (`getModByTranslateKey`, `getSettingsByMod`) delegating to `Shindo.getInstance().modManager`; `@file:JvmName("ModuleExtensions")`.
- `src/main/kotlin/me/miki/extensions/network/NetworkManagerExtensions.kt` — TODO scaffold for network diagnostics/proxy helpers; `@file:JvmName("NetworkManagerExtensions")`.
- `src/main/kotlin/me/miki/extensions/network/okhttp/OkHttpClientExtensions.kt` — Extensions on `OkHttpClient` (`executeOrNull`, `withTimeouts`); exception-safe IO pattern; `@file:JvmName("OkHttpClientExtensions")`.
- `src/main/kotlin/me/miki/extensions/network/okhttp/OkHttpRequestExtensions.kt` — Extensions on `Request.Builder` (`applyHeaders`, `postJson`); builder-style chaining; `@file:JvmName("OkHttpRequestExtensions")`.
- `src/main/kotlin/me/miki/extensions/network/okhttp/OkHttpResponseExtensions.kt` — Extension on `Response` (`bodyStringOrEmpty`) swallowing IO failures; `@file:JvmName("OkHttpResponseExtensions")`.
- `src/main/kotlin/me/miki/extensions/network/proxy/ProxyManagerExtensions.kt` — TODO scaffold for proxy lookups/validation; `@file:JvmName("ProxyManagerExtensions")`.
- `src/main/kotlin/me/miki/extensions/network/websocket/JavaWebSocketExtensions.kt` — Extensions on `WebSocketClient` (`sendJsonObject`, `sendJson`, `closeQuietly`) and nullable `ServerHandshake.statusCodeOr`; `@file:JvmName("JavaWebSocketExtensions")`.
- `src/main/kotlin/me/miki/extensions/profiles/ProfileManagerExt.kt` — TODO scaffold for Profile/ProfileManager helpers; `@file:JvmName("ProfileManagerExtensions")`; filename `ProfileManagerExt`.
- `src/main/kotlin/me/miki/extensions/serialization/gson/GsonExtensions.kt` — Extensions on `Gson` (`fromJsonOrNull`, `toJsonObjectOrNull`, `prettyCopy`); uses reified generics; `@file:JvmName("GsonExtensions")`.
- `src/main/kotlin/me/miki/extensions/serialization/gson/JsonObjectExtensions.kt` — Extensions on `JsonObject` (`stringOrNull`, `booleanOrDefault`, `putIfNotBlank`); `@file:JvmName("JsonObjectExtensions")`.
- `src/main/kotlin/me/miki/extensions/serialization/kotlinx/ExtensionMessage.kt` — Placeholder `@Serializable` DTO; not an extension; package aligns with serialization.
- `src/main/kotlin/me/miki/extensions/serialization/kotlinx/KotlinSerializationRoadmap.kt` — Roadmap singleton with TODOs; not an extension function container.
- `src/main/kotlin/me/miki/extensions/settings/SettingExtensions.kt` — Extensions on `Setting` delegating typed lookups to `SettingRegistry`; `@file:JvmName("SettingExtensions")`.
- `src/main/kotlin/me/miki/extensions/ui/accent/AccentColorExtensions.kt` — TODO scaffold for accent color helpers; `@file:JvmName("AccentColorExtensions")`.
- `src/main/kotlin/me/miki/extensions/ui/theme/ThemeExtensions.kt` — TODO scaffold for theme/color manager helpers; `@file:JvmName("ThemeExtensions")`.

## 2) Animation/easing/interpolation references (search scope: `src/main/kotlin|java/me/miki/shindo/`)
- UI animation framework exists under `me.miki.shindo.ui.animation` (core `Animation`, `TimedAnimation`, `Direction`, `AnimationUtils`, `EasingFunctions`, many easing classes, value animations). Timers rely on `TimerUtils` (System.currentTimeMillis) and expose `isDone`, `setDirection`, `getValue`.
- Numerous UI components use these animations: `ui/comp/buttons/CompAddonToggleButton`, `CompCategory`, `GuiModMenu` (introAnimation, scroll animation), `GuiEditHUD`, `ClickEffects`, `ParticleEngine`, etc.
- Mod menu transitions: `gui/modmenu/category/.../LayoutCarouselScene` uses nanosecond `TRANSITION_DURATION_NS` and progress interpolation; `ModMenuCategoryTransitionCoordinator` orchestrates category transitions; `GuiModMenu` checks `isTransitioning` and drives `scroll.onAnimation()`.
- Render effects and utilities interpolate values: `types/Color.interpolate`, `utils/MathUtils.interpolate*`, `ui/particle/Particle.interpolation`, color easing helpers.
- Third-party style animation set in `mobends/animation` package (many `Animation_*` enums/classes for player actions).
- Conclusion: existing animation systems already present (UI animation & mobends); not a blank slate.

## 3) Naming/structure inconsistencies in Extensions
- Mixed file naming: some use `*Ext.kt` (`ModManagerExt`, `ModuleExt`, `ProfileManagerExt`) while JvmName is `*Extensions`; others use `*Extensions.kt`. Inconsistent suffixing could confuse discoverability.
- Several “extension” files are pure TODO stubs with no receivers (addons, network, proxy, NanoVG, theme, accent) while others contain real helpers; responsibility separation is uneven.
- Serialization/kotlinx package includes a DTO (`ExtensionMessage`) and roadmap object, which are not extension functions—breaks the extension-only convention.
- Root bootstrap (`ExtensionLibrary`) tracks base package/version but is not wired to load/register anything; no consistent module registration pattern.
- ShindoExt uses singleton accessors instead of extension-style helpers on managers (acts as service locator), differing from receiver-focused files.

## 4) Public APIs the new animations system must integrate with
- Event bus: `management/event/EventManager` with `@EventTarget` subscriptions. Relevant events for animation updates/drawing: `EventTick` (logic tick), `EventRenderTick` (render tick without data), `EventRender2D(partialTicks: Float)` for HUD/UI drawing; other render events (3D, overlays) exist if needed.
- Render loop: `management/nanovg/NanoVGManager` provides `setupAndDraw(task: Runnable, scale: Boolean = true)` that wraps NanoVG frame begin/end and scaling; drawing helpers (`drawRect`, `drawRoundedRect`, `drawAlphaBar`, gradients) operate on internal NanoVG context. Hook animations inside the NanoVG-managed frame.
- Timing utilities: `utils/TimerUtils` (millisecond-based, `reset`, `delay`, `elapsedTime`) already used by `Animation`/`TimedAnimation`; reuse for delta/elapsed tracking.
- Existing UI animation core (`ui/animation/*`) already consumes TimerUtils and directions; if augmenting rather than replacing, ensure compatibility with `Animation.isDone(dir)` and `Animation.setDirection(dir)`.

## 5) Package naming pattern for extensions
- Base namespace `me.miki.extensions`, followed by domain folders (`modules`, `network.okhttp`, `ui.theme`, etc.). Files usually carry `@file:JvmName(\"<Type>Extensions\")` even when the filename is `*Ext.kt`. New animation extensions should live under `me.miki.extensions.animation` (or domain-specific subpackage) and follow the same `*Extensions` JvmName/file-header pattern with receiver-based top-level functions.
