package me.miki.shindo.utils.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Descreve, em texto livre, a responsabilidade principal de um
 * componente (classe, método, campo, etc.).
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.PARAMETER
})
public @interface Description {
    String value();
}


