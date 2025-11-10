package me.miki.shindo.management.settings.config;

import me.miki.shindo.management.language.TranslateText;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Property {

    PropertyType type();

    /**
     * Optional translation key. When set to something other than {@link TranslateText#NONE}, this overrides the
     * plain-text {@link #name()} value.
     */
    TranslateText translate() default TranslateText.NONE;

    /**
     * Plain text name displayed for the property when no translation is provided.
     */
    String name() default "";

    String category() default "";

    String description() default "";

    boolean hidden() default false;

    double min() default Double.NaN;

    double max() default Double.NaN;

    double step() default Double.NaN;

    /**
     * Optional override for numeric default/current value. If left as NaN the existing field value is used.
     */
    double current() default Double.NaN;

    /**
     * Optional override for color defaults (ARGB). Ignored when {@link Integer#MIN_VALUE}.
     */
    int color() default Integer.MIN_VALUE;

    boolean showAlpha() default false;

    /**
     * Optional default key code for keybind properties. Ignored when set to {@link Integer#MIN_VALUE}.
     */
    int keyCode() default Integer.MIN_VALUE;

    String text() default "";

    String enumName() default "";

    /**
     * Optional custom configuration key. When blank the key is generated from the owner and field name.
     */
    String key() default "";
}
