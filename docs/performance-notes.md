# Performance Notes — Animation System

## src/main/kotlin/me/miki/shindo/ui/animation/Animation.kt
- Risk: none detected in per-frame path.
- Mitigation status: N/A (math-only; TimerUtils reuse).

## src/main/kotlin/me/miki/shindo/ui/animation/value/ColorAnimation.kt
- Risk: new `Color` allocation while channel values change.
- Mitigation status: mitigated; cached instance reused once values stabilize.

## src/main/kotlin/me/miki/shindo/ui/animation/value/FloatAnimation.kt
- Risk: none detected in per-frame path.
- Mitigation status: N/A (pure arithmetic on cached fields).

## src/main/kotlin/me/miki/shindo/ui/animation/value/Vector2fAnimation.kt
- Risk: none detected in per-frame path.
- Mitigation status: N/A (delegates to cached `FloatAnimation` instances).

## src/main/kotlin/me/miki/shindo/ui/animation/screen/ScreenFramebufferBase.kt
- Risk: framebuffer recreation when viewport size changes.
- Mitigation status: mitigated; allocation occurs only on resolution change, not per frame.

## src/main/kotlin/me/miki/shindo/ui/animation/screen/ScreenAnimation.kt
- Risk: `ScaledResolution` and `Runnable` wrappers allocated each wrap invocation.
- Mitigation status: intentional; Minecraft 1.8.9 lacks reusable resolution API and NanoVG callbacks require `Runnable`.

## src/main/kotlin/me/miki/shindo/ui/animation/screen/ScreenAlpha.kt
- Risk: `Runnable` allocated per wrap call for NanoVG callback.
- Mitigation status: intentional; required by NanoVG API.

## src/main/kotlin/me/miki/shindo/ui/animation/screen/ScreenStencil.kt
- Risk: `ScaledResolution` and `Runnable` allocated per wrap call.
- Mitigation status: intentional; resolution object lacks cache hooks and NanoVG requires `Runnable`.

## src/main/kotlin/me/miki/extensions/animation/AnimationExtensions.kt
- Risk: none detected in per-frame path.
- Mitigation status: N/A (pure state checks).

## src/main/kotlin/me/miki/extensions/animation/ColorAnimationExtensions.kt
- Risk: none detected in per-frame path.
- Mitigation status: N/A (delegates to `ColorAnimation`).

## src/main/kotlin/me/miki/extensions/animation/EasingExtensions.kt
- Risk: none detected in per-frame path.
- Mitigation status: N/A (pure easing math).

## src/main/kotlin/me/miki/extensions/animation/FloatAnimationExtensions.kt
- Risk: none detected in per-frame path.
- Mitigation status: N/A (pure arithmetic on cached values).

## src/main/kotlin/me/miki/extensions/animation/TimelineExtensions.kt
- Risk: `TimelineState` allocation on first use per animation; WeakHashMap entry churn may GC frequently created animations.
- Mitigation status: acceptable; no per-frame allocations after first access and entries are weakly referenced.

## src/main/kotlin/me/miki/extensions/animation/Vector2fAnimationExtensions.kt
- Risk: none detected in per-frame path.
- Mitigation status: N/A (delegates to underlying animation methods).
