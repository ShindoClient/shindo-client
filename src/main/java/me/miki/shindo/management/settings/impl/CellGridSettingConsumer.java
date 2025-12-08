package me.miki.shindo.management.settings.impl;

/**
 * Allows classes that own a {@link CellGridSetting} to receive the instance created by the
 * metadata binder so they can react to live edits (e.g., rendering with per-cell colors).
 */
public interface CellGridSettingConsumer {

    void onCellGridAvailable(CellGridSetting setting);
}
