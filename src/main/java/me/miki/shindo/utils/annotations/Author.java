package me.miki.shindo.utils.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indica o(s) autor(es) primário(s) de um componente.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.FIELD
})
public @interface Author {
    /**
     * Nome(s) do(s) autor(es). Pode ser um ou mais.
     */
    String[] value();
}


