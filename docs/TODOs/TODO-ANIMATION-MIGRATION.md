# TODO — Animation System Migration Plan

Kotlin 1.3.72 / Java 8. No code changes in this document.

## Phase 1: Audit and Map Existing UI Animation System
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
