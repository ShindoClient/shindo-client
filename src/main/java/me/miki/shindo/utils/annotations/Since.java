package me.miki.shindo.utils.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indica desde qual versão ou data um componente existe.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.FIELD
})
public @interface Since {
    /**
     * Valor livre, por exemplo "5.1.0", "2025-11-26", etc.
     */
    String value();
}


