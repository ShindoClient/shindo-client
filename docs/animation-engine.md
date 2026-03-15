# Animation Engine Overview

The animation engine delivers timeline-driven easing, typed value helpers, and framebuffer-backed screen effects so UI transitions can stay smooth without polluting the client with repeated allocations. Phases 1‑8 built a cohesive stack of base timelines, typed adapters, NanoVG compositors, extension helpers, documentation, and expansion hooks that future contributors can extend without touching the core renderer.

## Public APIs
- `Animation` — core timeline that tracks direction, duration, global scaling, and easing output.
- `TimedAnimation` — adapter that wires custom easing equations into `Animation` without reshaping timing logic.
- `AnimationComponent` — expansion interface for grouped animations to expose their timelines to orchestration utilities.
- `FloatAnimation` — timeline for scalar floats that exposes typed accessors while reusing the shared timeline machinery.
- `Vector2fAnimation` — paired float timelines for X/Y motion that implement `AnimationComponent`.
- `ColorAnimation` — RGBA animator that reuses `SimpleAnimation` channels and caches a `Color` instance.
- `ScreenFramebufferBase` — NanoVG framebuffer cache that minimizes recreation to resolution changes.
- `ScreenAnimation` — scale/fade compositing helper that draws offscreen content and reuses `NanoVGManager`.
- `ScreenAlpha` — alpha-only transition operator using the shared framebuffer resources.
- `ScreenStencil` — rounded-rect stencil compositor that draws masked offscreen content.
- `EasingFunctions` — library of built-in easing equations plus a registry for custom curves.
- `AnimationExtensions` — direction helpers and completion callbacks for `Animation`.
- `FloatAnimationExtensions` — interpolation helpers and snapping shortcuts for float timelines.
- `Vector2fAnimationExtensions` — axis-aware helpers that bridge to the bundled `Vector2fAnimation`.
- `ColorAnimationExtensions` — convenience entry points that flow through `ColorAnimation`.
- `TimelineExtensions` — delay, repeat, and chaining utilities for `Animation` objects (and `AnimationComponent` groups).
- `EasingExtensions` — Float/Double convenience functions that wrap `EasingFunctions` equations.

## Adding a new animation type
1. Create a class that extends `Animation` or `TimedAnimation`, exposing the desired value type and any easing input.
2. Implement `AnimationComponent` so orchestration helpers can iterate the underlying timelines without requiring bespoke hooks.
3. Expose typed getters or extension functions (see `FloatAnimation`/`Vector2fAnimation`) and reuse `TimelineExtensions` for delayed starts, repeats, or chaining.

## Adding a new easing function
1. Define a pure easing function that accepts `elapsed` and `duration` and returns a 0..1 progress ratio.
2. Register it via `EasingFunctions.registerCustomEasing(name, equation)` so callers can reference it without editing the enum-style object.
3. Use `EasingFunctions.customEasing(name, elapsed, duration)` (optionally with a fallback) in typed animations or `EasingExtensions` helpers.

## Runtime hooks
- Hook `EventRender2D` and call `NanoVGManager.setupAndDraw` inside the handler to draw NanoVG-backed animations during the normal render flush.

## Timing utility
- Use `TimerUtils` (shared by `Animation`, `TimelineExtensions`, and `AnimationComponent` helpers) as the canonical source of milliseconds, so durations, delays, and repeats stay synchronized with Minecraft’s tick loop.
