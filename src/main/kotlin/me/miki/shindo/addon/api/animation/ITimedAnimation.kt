package me.miki.shindo.addon.api.animation

/**
 * Interface de animação com duração fixa (ex: SmoothStepAnimation).
 * Usa getValue() para obter progresso e changeDirection() para iniciar/reverter.
 */
interface ITimedAnimation {

    fun getValue(): Double

    fun reset()

    fun changeDirection()

    fun setDirection(direction: AnimationDirection)

    fun isDone(): Boolean

    /**
     * Callback chamado uma vez quando a animação termina (isDone() = true).
     * O addon deve chamar getValue() ou isDone() periodicamente (ex: no render) para que o callback seja disparado.
     */
    fun setOnComplete(callback: () -> Unit)
}
