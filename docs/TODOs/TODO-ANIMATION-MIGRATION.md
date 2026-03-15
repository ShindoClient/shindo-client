# TODO — Animation System Migration Plan

Kotlin 1.3.72 / Java 8. No code changes in this document.

## Phase 1: Audit and Map Existing UI Animation System
- Status: Complete (2026-03-15).
- Objective: Capture a complete inventory of current animation APIs, responsibilities, and gaps before refactoring.
- Files: read-only review of `src/main/kotlin/me/miki/shindo/ui/animation/**/*`.
- Sub-tasks (ordered):
  1) List every public class/object/extension in `ui/animation`.
  2) For each, note inputs/outputs, timing source usage (TimerUtils), and allocation hotspots.
  3) Document missing pieces, inconsistencies, and extension pain points.
  4) Publish findings into this TODO file (append) for traceability.
- Depends on: none.
- Acceptance criteria: Inventory covers 100% of files in `ui/animation`; each public API has role/limit notes; gaps are enumerated; no source files modified.
- Compatibility risks: None (read-only).

## Phase 2: Refactor the Interpolation Engine
- Status: Complete (2026-03-15).
- Objective: Improve `Animation` and `TimedAnimation` internals without breaking callers and decouple interpolation from timing.
- Files: `src/main/kotlin/me/miki/shindo/ui/animation/Animation.kt`, `TimedAnimation.kt`, `AnimationUtils.kt` (if timing mixed), related easing files only if signature-neutral.
- Sub-tasks:
  1) Design separation of timing (TimerUtils-driven) vs interpolation math; outline adapter points.
  2) Add adapter layer if any method signatures must change; keep existing signatures alive.
  3) Remove per-frame allocations; audit lambdas/boxing in update path.
  4) Update KDoc to reflect behavior and compatibility notes.
- Depends on: Phase 1.
- Acceptance criteria: Existing public methods remain callable; timing and interpolation responsibilities are separated in code structure; zero known per-frame allocations in update path; adapters documented; tests/manual checkpoints identified (even if not yet implemented).
- Compatibility risks: Behavior drift in easing timing; potential binary signature changes if not guarded by adapters.

## Phase 3: Expand Animation Types
- Status: Complete (2026-03-15).
- Objective: Provide reusable typed animations (Float, Vector2f, Color) using existing timing.
- Files to create: `src/main/kotlin/me/miki/shindo/ui/animation/value/FloatAnimation.kt`, `.../Vector2fAnimation.kt`, `.../ColorAnimation.kt` (one type per file).
- Sub-tasks:
  1) Specify constructors and easing hooks consistent with current `Animation` base.
  2) Reuse `TimerUtils` for timing; no new timers.
  3) Define clamping/interpolation rules per type; document color space assumptions.
  4) Add minimal factory helpers if needed (without breaking base API).
- Depends on: Phase 2.
- Acceptance criteria: Three new typed animation classes exist in their own files; APIs mirror base animation patterns; no duplicate timing utilities; documented defaults for interpolation/clamping.
- Compatibility risks: Color interpolation expectations (ARGB vs HS(B)) may differ from existing callers; vector math dependencies.

## Phase 4: Create Animation Extensions
- Status: Complete (2026-03-15).
- Objective: Add ergonomic helpers via extensions in dedicated package.
- Files to create: `src/main/kotlin/me/miki/extensions/animation/AnimationExtensions.kt`, `TimedAnimationExtensions.kt`, `FloatAnimationExtensions.kt`, `Vector2fAnimationExtensions.kt`, `ColorAnimationExtensions.kt`, `EasingExtensions.kt`, `TimelineExtensions.kt` (all `@file:JvmName(\"*Extensions\")`).
- Sub-tasks:
  1) Add receiver-based helpers for Animation/TimedAnimation (state checks, chaining, reset).
  2) Add easing shortcuts on `Float`/`Double`.
  3) Timeline helpers: delay, repeat, chain; ensure no extra timers.
  4) KDoc each function and ensure package name `me.miki.extensions.animation`.
- Depends on: Phase 3.
- Acceptance criteria: All extension files created with correct package and JvmName; helpers are receiver-based; no new timing utilities; compiles without touching mixins.
- Compatibility risks: Overlapping names with existing helpers; need to avoid shadowing core APIs.

## Phase 5: Fix Extensions System Inconsistencies
- Status: Complete (2026-03-15).
- Objective: Align naming and content boundaries in the extensions module.
- Files to rename: `src/main/kotlin/me/miki/extensions/ShindoExt.kt` → `ShindoExtensions.kt`; `modules/ModManagerExt.kt` → `ModManagerExtensions.kt`; `modules/ModuleExt.kt` → `ModuleExtensions.kt`; `profiles/ProfileManagerExt.kt` → `ProfileManagerExtensions.kt`. All imports updated repo-wide.
- Files to remove/relocate: `src/main/kotlin/me/miki/extensions/serialization/kotlinx/ExtensionMessage.kt`, `KotlinSerializationRoadmap.kt` (move to non-extensions package or delete after migration decision).
- Sub-tasks:
  1) Plan rename sequence and update import references across codebase.
  2) Ensure `@file:JvmName` stays plural (`*Extensions`) post-rename.
  3) Decide destination for removed serialization artifacts (separate docs or data models package).
  4) Run compilation check after rename (no code changes beyond renames/imports).
- Depends on: Phase 1 (naming map), Phase 4 (package pattern clarity).
- Acceptance criteria: No `*Ext.kt` filenames remain; all extension files use plural JvmName; no non-extension DTOs in extensions packages; build compiles after import updates.
- Compatibility risks: Binary/source incompatibility from moved classes; IDE run configs may need refresh for renamed files.

## Phase 6: Performance Validation
- Status: Complete (2026-03-15).
- Objective: Confirm new/changed animation paths are allocation-safe and frame-safe.
- Files: Review all updated/added animation files and extensions; create `docs/performance-notes.md`.
- Sub-tasks:
  1) Annotate allocation risks inline (comments) in animation files.
  2) Document measured/suspected hotspots and mitigation in `performance-notes.md`.
  3) Verify update/render paths avoid per-frame object creation (profiling plan if available).
- Depends on: Phases 2–4.
- Acceptance criteria: performance-notes.md exists with per-file notes; inline comments mark any remaining allocations; checklist of zero-alloc goals per update path; no new runtime utilities added.
- Compatibility risks: Comment-only changes should be safe; potential behavior change if future optimizations alter timing.

## Phase 7: Standardization and Final Documentation
- Status: Complete (2026-03-15).
- Objective: Finalize naming, documentation, and public API clarity for the animation system.
- Files: All touched animation/extension files; new `docs/animation-engine.md`; update this TODO to mark completion.
- Sub-tasks:
  1) Run naming pass to ensure consistent terms (Animation vs Animated, etc.).
  2) Add comprehensive KDoc to every public API added/modified.
  3) Author `docs/animation-engine.md` summarizing architecture, hooks (EventRender2D + NanoVGManager.setupAndDraw), and usage examples.
  4) Mark each phase item as complete in this TODO file.
- Depends on: Phases 1–6.
- Acceptance criteria: `animation-engine.md` present with hook/timing description; all public APIs documented; TODO updated with completion markers; no unchecked TODOs remain in prior phases.
- Compatibility risks: Documentation-only; low risk unless naming changes are proposed during this pass.

## Phase 8: Future Expansion Hooks
- Status: Complete (2026-03-15).
- Objective: Provide open/closed extension points for animations and easings so future contributors avoid modifying the core files.
- Files: `src/main/kotlin/me/miki/shindo/ui/animation/AnimationComponent.kt`, `src/main/kotlin/me/miki/shindo/ui/animation/EasingFunctions.kt`, `docs/animation-engine.md`.
- Sub-tasks:
  1) Create an interface that exposes animation timelines for grouped types (`AnimationComponent`).
  2) Add a registry for custom easing functions in `EasingFunctions`.
  3) Document the expansion hooks and usage patterns in `docs/animation-engine.md`.
- Depends on: Phases 1–7.
- Acceptance criteria: External animation types can implement `AnimationComponent`, easings registered via `EasingFunctions.registerCustomEasing`, documentation updated.
- Compatibility risks: Conservative; new APIs do not change existing behavior but expand extension surface.

## Phase 1 Findings
- `src/main/kotlin/me/miki/shindo/ui/animation/Animation.kt` — Public abstract class `Animation` (constructors with duration/endPoint/direction); methods `isDone(dir)`, `getLinearOutput`, `reset`, `isDone`, `changeDirection`, `getValue`, `setValue`, `setDirection`, `getValueFloat/Int`; abstract `getEquation`. Uses `TimerUtils` (`reset`, `delay`, `elapsedTime`) to track elapsed milliseconds; per-frame risk: repeated `System.currentTimeMillis()` in `setValue/setDirection` and branching only; no allocations. Limitations: reverse-direction math uses endPoint and `correctOutput` flag; duration not scaled via `GlobalAnimationSettings.animationScale`; uses millis not partialTicks.
- `TimedAnimation.kt` — Public open class extending `Animation`, constructor takes `(ms, endPoint, (elapsed,duration)->Double)` and overrides `getEquation`; calls `reset()` in init. Uses `TimerUtils` from base. No allocations. Limitation: lambda passed at construction could allocate per instance but not per frame.
- `Direction.kt` — Public enum `Direction { FORWARDS, BACKWARDS; opposite() }`. No TimerUtils; no alloc risk.
- `EasingFunctions.kt` — Public object with easing functions: linear, smoothStep, decelerate, in/out/inOut Quad/Cubic/Quart/Quint/Circ/Sine/Expo, inOutCirc, backIn (only “back” variant), elastic(elasticity,smooth,reallyElastic). No TimerUtils. Allocation-free. Missing common easings: backOut/backInOut, bounce (in/out/inOut), swing, overshoot variants.
- `GlobalAnimationSettings.kt` — Public object with `enabled`, `animationScale`, `scaleDuration(durationMs)`. No TimerUtils. Limitation: base `Animation` does not honor `animationScale`; only skip when disabled.
- `AnimationExtensions.kt` — Top-level `val Animation.valueFloat` delegating to `getValueFloat()`. No TimerUtils; no alloc risk.
- `curve/DecelerateAnimation.kt` — Public class extends `TimedAnimation` with `EasingFunctions::decelerate`; optional constructor sets direction. Uses base TimerUtils indirectly. No extra allocation. Straightforward.
- `curve/SmoothStepAnimation.kt` — Same pattern using `EasingFunctions::smoothStep`. No extra risks.
- `easing/*` classes — Each file defines one public class extending `TimedAnimation` with corresponding `EasingFunctions` method; all constructors `(ms, endPoint)` plus optional direction; no TimerUtils usage beyond base. `EaseLiner` uses linear; `EaseBackIn` uses custom lambda to pass easeAmount and overrides `correctOutput`; `EaseElasticAnimation` extends `Animation`, stores `easeAmount/smooth/reallyElastic`, uses `EasingFunctions.elastic`; calls `reset()` in constructor. Allocation risks: none per frame; minor object capture in `EaseBackIn` lambda at construction time only. Limitation: only “in” variant for Back; elastic only via standalone class not TimedAnimation subclass; no direction-aware easing helpers.
- `value/AnimationUtils.kt` — Object with `calculateCompensation(target,current,speed,delta)` moving current toward target; no TimerUtils; allocation-free. Parameter `delta` expects milliseconds caller-provided.
- `value/SimpleAnimation.kt` — Open class storing `value` and `lastMS`; methods `setAnimation(target, speed)` overloads; uses `GlobalAnimationSettings.enabled` to short-circuit; manual millis delta via `System.currentTimeMillis()`; no TimerUtils. Allocation-free; risk: repeated abs/math only. Limitation: speed clamp magic numbers (28, 0.35f) hard-coded; no easing choice.
- `value/ColorAnimation.kt` — Open class; private array of three `SimpleAnimation` instances; public `getColor(color, speed:Int=12)`, `setColor(color)`. No TimerUtils. Allocation risk: returns new `java.awt.Color` each call (per-frame object); three SimpleAnimation instances reused. Limitation: RGB-only, no alpha animation, uses ints without color space choice.
- `screen/ScreenEffect.kt` — Interface `close()`. No TimerUtils.
- `screen/ScreenAnimation.kt` — Open class implementing `ScreenEffect`; many overloads of `wrap` accepting optional `glRender`, tasks, bounds, progress, stencil flag. Uses `NanoVGManager.setupAndDraw`, `ScaledResolution`, framebuffers (`NVGLUFramebuffer`), `BufferUtils.createFloatBuffer`, `NVGPaint`. Allocation risks: `BufferUtils.createFloatBuffer(16)` per call; `NVGPaint.create()` per wrap; potential framebuffer recreation when size changes; multiple overloads create autoboxed `Runnable` lambdas. No TimerUtils. Limitations: heavy GL/NanoVG coupling; ignores `GlobalAnimationSettings.animationScale` beyond enable short-circuit; assumes Minecraft singleton; overload explosion; no cleanup of `glRender` resource sequencing safety.
- `screen/ScreenAlpha.kt` — Similar to ScreenAnimation but only alpha fade; wraps task into framebuffer then draws textured quad with adjustable alpha. Alloc risk: `BufferUtils.createFloatBuffer(16)` and `NVGPaint.create()` per call; framebuffer recreate on size change. No TimerUtils. Limitation: no guard for alpha <0; uses full-screen only; duplicated logic with ScreenAnimation.
- `screen/ScreenStencil.kt` — Provides `wrap(task, x,y,w,h,radius, alpha)` using framebuffer and NanoVG rounded-rect mask; overload accepting `KFunction`. Alloc risk: `BufferUtils.createFloatBuffer` and `NVGPaint.create()` per call; framebuffer recreate on size change. No TimerUtils. Limitation: KFunction to Runnable cast unchecked; factor scaling assumes ScaledResolution; no caching for paint/buffers.
- `screen/ScreenAlpha.kt`/`ScreenAnimation.kt`/`ScreenStencil.kt` all rely on `GlobalAnimationSettings.enabled` only in ScreenAnimation; others always run even when disabled.
- `value/AnimationUtils.kt`, `SimpleAnimation.kt`, `ColorAnimation.kt` do not integrate with `TimerUtils`; operate independently.
- `Animation.kt`/`TimedAnimation.kt`/easing classes form the main timeline engine; screen classes are effect wrappers rather than timeline-driven.

Gap analysis:
- Missing typed animations: `FloatAnimation` (beyond SimpleAnimation’s scalar but no base-class integration), `Vector2fAnimation` (none), `ColorAnimation` exists but not integrated with base `Animation` timeline and omits alpha; no generic value animation.
- Easing coverage: present linear, smoothStep, decelerate, quad/cubic/quart/quint, sine, expo, circ, backIn, elastic. Missing backOut/backInOut, bounce (all variants), spring/overshoot, step, polynomial configurable, custom curves registration.
- Hardest-to-extend issues: `Animation` uses fixed millis timer and embeds direction/endPoint math without duration scaling or delta injection, making alternative time sources (partialTicks) difficult; `GlobalAnimationSettings.animationScale` unused by core; per-frame framebuffer allocations in screen effects discourage reuse; easing classes are rigid per-file wrappers rather than parameterized factories, leading to class explosion for new curves.
