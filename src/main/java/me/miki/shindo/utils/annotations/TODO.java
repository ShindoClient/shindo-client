package me.miki.shindo.utils.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca pontos do código que ainda precisam ser trabalhados.
 *
 * Útil como alternativa estruturada a comentários TODO soltos.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.PARAMETER
})
public @interface TODO {
    /**
     * Descrição curta do que falta fazer ou melhorar.
     */
    String value();

    /**
     * Opcionalmente, quem é o responsável sugerido por esta tarefa.
     */
    String assignee() default "";
}


