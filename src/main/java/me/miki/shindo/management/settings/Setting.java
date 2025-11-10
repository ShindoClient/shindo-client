package me.miki.shindo.management.settings;

import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.annotation.NotNull;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.management.settings.metadata.SettingMetadata;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class Setting {

    private static final Pattern KEY_SANITIZE = Pattern.compile("[^a-z0-9]+");

    @Getter
    @NotNull
    private final ConfigOwner parent;
    private final TranslateText nameTranslate;
    private final String displayName;
    private final String nameKey;

    private boolean registered;
    private SettingMetadata metadata;

    protected Setting(TranslateText nameTranslate, ConfigOwner parent) {
        this.parent = parent;
        this.nameTranslate = nameTranslate;
        this.displayName = nameTranslate.getText();
        this.nameKey = nameTranslate.getKey();
        register();
    }

    protected Setting(String name, ConfigOwner parent) {
        this.parent = parent;
        this.nameTranslate = null;
        this.displayName = name;
        this.nameKey = buildKey(parent, name);
        register();
    }

    private void register() {
        if (registered) {
            return;
        }

        if (parent instanceof Mod) {
            Shindo.getInstance().getModManager().addSettings(this);
        } else if (parent instanceof Addon) {
            Shindo.getInstance().getAddonManager().addSettings(this);
        }

        registered = true;
    }

    public void reset() {
    }

    public String getName() {
        return displayName;
    }

    public TranslateText getTranslate() {
        return nameTranslate;
    }

    public String getNameKey() {
        if (metadata != null && !metadata.getKeyOverride().isEmpty()) {
            return metadata.getKeyOverride();
        }
        return nameKey;
    }

    public SettingMetadata getMetadata() {
        return metadata;
    }

    public void applyMetadata(SettingMetadata metadata) {
        this.metadata = metadata;
    }

    private static String buildKey(ConfigOwner parent, String raw) {
        String candidate = normalizeKey(raw);
        return parent.getConfigId() + ":" + candidate;
    }

    public static String normalizeKey(String raw) {
        String candidate = raw == null ? "" : raw;
        candidate = Normalizer.normalize(candidate, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        candidate = KEY_SANITIZE.matcher(candidate).replaceAll("_");
        candidate = candidate.replaceAll("^_+", "").replaceAll("_+$", "");

        if (candidate.isEmpty()) {
            candidate = "setting_" + Math.abs(String.valueOf(raw).hashCode());
        }

        return candidate;
    }
}
