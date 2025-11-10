package me.miki.shindo.management.settings.config;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.Setting;

import java.util.Locale;

/**
 * Helper interface for enums exposed through the {@link Property} system.
 * Implementing enums can provide a translation key or fall back to a prettified
 * name generated from the constant itself.
 */
public interface PropertyEnum {

    /**
     * Optional translation key. When {@link TranslateText#NONE} the enum name is
     * prettified for display.
     */
    default TranslateText getTranslate() {
        return TranslateText.NONE;
    }

    /**
     * Unique key used for persistence. When no translation is provided this will
     * default to a sanitized version of the enum name.
     */
    default String getNameKey() {
        TranslateText translate = getTranslate();
        if (translate != TranslateText.NONE) {
            return translate.getKey();
        }
        return Setting.normalizeKey(((Enum<?>) this).name());
    }

    /**
     * Human readable label shown in UI components. Implementations may override
     * for full control – otherwise the translation text (if present) or a
     * sentence-cased enum name is used.
     */
    default String getDisplayName() {
        TranslateText translate = getTranslate();
        if (translate != TranslateText.NONE) {
            return translate.getText();
        }
        String raw = ((Enum<?>) this).name().toLowerCase(Locale.ROOT).replace('_', ' ');
        if (raw.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }
}
