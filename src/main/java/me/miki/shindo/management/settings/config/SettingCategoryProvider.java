package me.miki.shindo.management.settings.config;

import me.miki.shindo.management.language.TranslateText;

/**
 * Allows a {@link ConfigOwner} to supply localized labels and default collapsed states for the
 * categories generated from {@link me.miki.shindo.management.settings.metadata.Property} metadata.
 */
public interface SettingCategoryProvider {

    /**
     * Resolves a translated label for the given category key.
     *
     * @param categoryKey lower-case key provided in the {@code category} attribute
     * @return translation to use, or {@link TranslateText#NONE} / {@code null} to fall back to the raw key
     */
    TranslateText resolveCategoryLabel(String categoryKey);

    /**
     * Allows the provider to define the default collapsed state for a category.
     *
     * @param categoryKey lower-case key provided in the {@code category} attribute
     * @return {@code true} if the category should default collapsed, {@code false} otherwise
     */
    default boolean isCategoryInitiallyCollapsed(String categoryKey) {
        return false;
    }
}
