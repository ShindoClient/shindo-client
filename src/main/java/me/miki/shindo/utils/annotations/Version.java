package me.miki.shindo.utils.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Versão lógica de um componente. Não substitui o versionamento de build,
 * mas ajuda a marcar mudanças relevantes na API.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.FIELD
})
public @interface Version {
    /**
     * Versão em formato livre (ex.: "1.0", "2.1.3", "beta-1").
     */
    String value();
}


