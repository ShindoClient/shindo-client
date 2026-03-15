package me.miki.shindo.injection.interfaces;

import me.miki.shindo.management.mods.impl.skin3d.render.CustomizableModelPart;

public interface IMixinEntityPlayer {

    CustomizableModelPart getHeadLayers();

    void setupHeadLayers(CustomizableModelPart box);

    CustomizableModelPart[] getSkinLayers();

    void setupSkinLayers(CustomizableModelPart[] box);

}
