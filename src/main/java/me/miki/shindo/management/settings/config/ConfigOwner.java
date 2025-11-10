package me.miki.shindo.management.settings.config;

/**
 * Marker interface for components (mods, addons, etc.) that expose configurable settings.
 */
public interface ConfigOwner {

    /**
     * Unique identifier used to group settings belonging to this owner.
     */
    String getConfigId();

    /**
     * Human readable display name used in generic contexts.
     */
    default String getDisplayName() {
        return getConfigId();
    }
}
