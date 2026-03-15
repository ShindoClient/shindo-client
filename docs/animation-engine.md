# Animation Engine Overview

## Runtime Hooks
- Render hook: `EventRender2D` triggers NanoVG via `NanoVGManager.setupAndDraw` before UI composition.
- Timing: `TimerUtils` supplies millisecond deltas; `GlobalAnimationSettings.scaleDuration` scales durations globally.
- Screen effects: `ScreenAnimation`, `ScreenAlpha`, and `ScreenStencil` render into cached NanoVG framebuffers and composite back to the main framebuffer.

## Core Timelines
- `Animation`: base timeline tracking direction, duration, and eased progress; respects global enable/scale flags.
- `TimedAnimation`: adapter that plugs custom easing equations into the base timeline without altering timing.
- Easing library: `EasingFunctions` plus extension helpers (`EasingExtensions`) for Float/Double progress values.

## Typed Animations
- `FloatAnimation`: wraps `TimedAnimation` to animate scalar values.
- `Vector2fAnimation`: pairs two float timelines for X/Y transitions.
- `ColorAnimation`: animates RGBA channels through shared `SimpleAnimation` instances with cached color reuse.

## Screen Effects
- `ScreenFramebufferBase`: caches framebuffer, clear-color buffer, and paint objects; recreates framebuffer only on resolution change.
- `ScreenAnimation`: scales and fades offscreen content with optional stencil compositing.
- `ScreenAlpha`: alpha-only fade using the cached framebuffer.
- `ScreenStencil`: rounded-rect mask compositing for offscreen content.

## Extensions and Timelines
- Animation helpers (`AnimationExtensions`, `FloatAnimationExtensions`, `Vector2fAnimationExtensions`, `ColorAnimationExtensions`) provide ergonomic APIs for state checks and snapping.
- Timeline utilities (`TimelineExtensions`) add delayed start, repeat, and chaining using per-animation `TimerUtils` state stored in a `WeakHashMap`.

## Usage Patterns
- Create a timeline (e.g., `FloatAnimation`) with duration/easing; poll `getValue()` or typed getters each frame.
- For UI transitions, wrap rendering inside `ScreenAnimation.wrap` or `ScreenAlpha.wrap` to composite animated content.
- Apply `delayedStart`, `repeatForever`, or `then` from `TimelineExtensions` to orchestrate multi-step animations without extra timers.
