package me.miki.shindo.addon.api.animation

/**
 * Tipos de easing disponíveis para animações.
 * O client implementa as curvas correspondentes.
 */
enum class EasingType {
    /** Progresso linear (t/duration) */
    LINEAR,

    /** Smooth step: -2x³ + 3x² */
    SMOOTH_STEP,

    /** Desaceleração: 1 - (x-1)² */
    DECELERATE,

    /** Circ in-out */
    IN_OUT_CIRC,

    /** Back in (com overshoot) */
    BACK_IN,

    /** Quad in */
    IN_QUAD,
    /** Quad out */
    OUT_QUAD,
    /** Quad in-out */
    IN_OUT_QUAD,

    /** Cubic in */
    IN_CUBIC,
    /** Cubic out */
    OUT_CUBIC,
    /** Cubic in-out */
    IN_OUT_CUBIC,

    /** Circ in */
    IN_CIRC,
    /** Circ out */
    OUT_CIRC,

    /** Sine in */
    IN_SINE,
    /** Sine out */
    OUT_SINE,
    /** Sine in-out */
    IN_OUT_SINE,

    /** Expo in */
    IN_EXPO,
    /** Expo out */
    OUT_EXPO,
    /** Expo in-out */
    IN_OUT_EXPO,

    /** Quart in */
    IN_QUART,
    /** Quart out */
    OUT_QUART,
    /** Quart in-out */
    IN_OUT_QUART,

    /** Quint in */
    IN_QUINT,
    /** Quint out */
    OUT_QUINT,
    /** Quint in-out */
    IN_OUT_QUINT,

    /** Elastic (elástico) - requer parâmetros extras no client */
    ELASTIC,
}
