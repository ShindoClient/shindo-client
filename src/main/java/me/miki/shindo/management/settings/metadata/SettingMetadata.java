package me.miki.shindo.management.settings.metadata;

import lombok.Getter;
import lombok.Setter;

@Getter
public class SettingMetadata {
    private final String fieldName;

    @Setter
    private String category = "";

    @Setter
    private String description = "";

    @Setter
    private String keyOverride = "";

    @Setter
    private boolean hidden = false;

    @Setter
    private double min = Double.NaN;

    @Setter
    private double max = Double.NaN;

    @Setter
    private double step = Double.NaN;

    public SettingMetadata(String fieldName) {
        this.fieldName = fieldName;
    }
}
