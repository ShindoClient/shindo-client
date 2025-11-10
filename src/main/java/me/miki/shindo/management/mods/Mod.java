package me.miki.shindo.management.mods;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.Shindo;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.annotation.Author;
import me.miki.shindo.management.annotation.Description;
import me.miki.shindo.management.annotation.Name;
import me.miki.shindo.management.annotation.NotNull;
import me.miki.shindo.management.annotation.Since;
import me.miki.shindo.management.annotation.Version;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.notification.NotificationType;
import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

@Name("Mod")
@Description("Base class for every Shindo client module.")
@Author({"MikiDevAHM"})
public class Mod implements ConfigOwner {

    private final TranslateText nameTranslate;
    private final TranslateText descriptionTranslate;

    @Getter
    @NotNull
    private final SimpleAnimation animation = new SimpleAnimation();

    @Getter
    @NotNull
    private final SimpleAnimation hoverAnimation = new SimpleAnimation();

    @Getter
    @NotNull
    private final SimpleAnimation settingsHoverAnimation = new SimpleAnimation();

    @NotNull
    public Minecraft mc = Minecraft.getMinecraft();
    @NotNull
    public FontRenderer fr = mc.fontRendererObj;
    @Getter
    private boolean toggled;

    @Setter
    @Getter
    private boolean hide;

    @Setter
    @Getter
    private ModCategory category;

    @Getter
    private String alias = "\u200B"; // zero width space

    @Getter
    private Boolean restricted = false, allowed = true;

    public Mod(TranslateText nameTranslate, TranslateText descriptionTranslate, ModCategory category) {

        this.nameTranslate = nameTranslate;
        this.descriptionTranslate = descriptionTranslate;
        this.toggled = false;
        this.category = category;

        this.setup();
    }

    public Mod(TranslateText nameTranslate, TranslateText descriptionTranslate, ModCategory category, String alias) {

        this.nameTranslate = nameTranslate;
        this.descriptionTranslate = descriptionTranslate;
        this.toggled = false;
        this.category = category;
        this.alias = alias;

        this.setup();
    }

    public Mod(TranslateText nameTranslate, TranslateText descriptionTranslate, ModCategory category, String alias, boolean restricted) {

        this.nameTranslate = nameTranslate;
        this.descriptionTranslate = descriptionTranslate;
        this.toggled = false;
        this.category = category;
        this.alias = alias;
        this.restricted = restricted;

        this.setup();
    }

    public void setup() {
    }

    public void onEnable() {
        if (Shindo.getInstance().getRestrictedMod().checkAllowed(this)) {
            Shindo.getInstance().getEventManager().register(this);
            ShindoLogger.info("[MODULE] " + getName() + " was enabled");
        } else {
            this.setToggled(false);
            Shindo.getInstance().getNotificationManager().post(this.nameTranslate.getText(), "Disabled due to serverside blacklist", NotificationType.INFO);
        }
    }

    public void onDisable() {
        Shindo.getInstance().getEventManager().unregister(this);
        ShindoLogger.info("[MODULE] " + getName() + " was disabled");
    }

    public void toggle() {
        setToggled(!toggled, true);
    }

    public void setToggled(boolean toggled, boolean sound) {

        this.toggled = toggled;

        if (toggled) {
            onEnable();
            if (sound) Shindo.getInstance().getModManager().playToggleSound(true);
        } else {
            onDisable();
            if (sound) Shindo.getInstance().getModManager().playToggleSound(false);
        }
    }

    public String getName() {
        return nameTranslate.getText();
    }

    public String getDescription() {
        return descriptionTranslate.getText();
    }

    public String getNameKey() {
        return nameTranslate.getKey();
    }

    public void setToggled(boolean toggled) {
        setToggled(toggled, false);
    }

    public Boolean isRestricted() {
        return this.restricted;
    }

    public void setAllowed(boolean modAllowed) {
        this.allowed = modAllowed;
    }

    @Override
    public String getConfigId() {
        return getNameKey();
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

}
