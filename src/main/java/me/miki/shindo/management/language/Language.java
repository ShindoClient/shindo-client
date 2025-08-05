package me.miki.shindo.management.language;

import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import net.minecraft.util.ResourceLocation;

public enum Language {

    ENGLISH("en-us", "English (United States)", new ResourceLocation("shindo/flag/us.png")),
    PORTUGUESE("pt-br", "Português (Brasileiro)", new ResourceLocation("shindo/flag/br.png"));

    private final SimpleAnimation animation = new SimpleAnimation();

    private final String id;
    private final String nameTranslate;
    private final ResourceLocation flag;

    Language(String id, String nameTranslate, ResourceLocation flag) {
        this.id = id;
        this.nameTranslate = nameTranslate;
        this.flag = flag;
    }

    public static Language getLanguageById(String id) {

        for (Language lang : Language.values()) {
            if (lang.getId().equals(id)) {
                return lang;
            }
        }

        return Language.ENGLISH;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return nameTranslate;
    }

    public ResourceLocation getFlag() {
        return flag;
    }

    public SimpleAnimation getAnimation() {
        return animation;
    }

    public String getNameTranslate() {
        return nameTranslate;
    }
}
