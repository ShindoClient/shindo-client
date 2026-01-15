/**
 * Package de easings para o Animation Engine v2.
 * 
 * Este package contém todas as funções de easing convertidas do sistema antigo,
 * organizadas por tipo para facilitar o uso e manutenção.
 * 
 * ## Estrutura
 * 
 * - `EasingFunction.kt` - Tipo base e utilitários
 * - `Easings.kt` - Exportação central de todos os easings
 * - `StandardEasings.kt` - Quad, Cubic, Quart, Quint
 * - `CircularEasings.kt` - Easing circular
 * - `ExponentialEasings.kt` - Easing exponencial
 * - `SineEasings.kt` - Easing senoidal
 * - `BackEasings.kt` - Easing com overshoot
 * - `ElasticEasings.kt` - Easing elástico com bounce
 * - `SmoothStepEasing.kt` - Smooth step (Hermite)
 * 
 * ## Uso
 * 
 * ```kotlin
 * import me.miki.shindo.ui.animation.engine.easing.Easings
 * 
 * // Usar easing padrão
 * val easing = Easings.EASE_OUT_CUBIC
 * 
 * // Usar easing customizado
 * val backEasing = Easings.easeOutBack(2.5f)
 * val elasticEasing = Easings.easeInElastic(0.4f, 0.6f, true)
 * ```
 */
package me.miki.shindo.ui.animation.engine.easing
