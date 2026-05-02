package me.miki.shindo.ui.animation.v1

/**
 * Extension point for composite animation types to expose their timelines without altering the engine.
 * Implementations provide the same lifecycle hooks that base timelines support, keeping systems open for new types.
 */
interface AnimationComponent {

    /**
     * Iterates over the underlying timelines that drive this animation component.
     * The iteration order can be used by orchestration utilities such as `TimelineExtensions`.
     */
    fun forEachTimeline(action: (Animation) -> Unit)
}
