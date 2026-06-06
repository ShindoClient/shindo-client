package com.shindoclient.shindo.injection.interfaces;

import com.shindoclient.shindo.management.mods.impl.skin3d.render.CustomizableModelPart;

public interface IMixinEntityPlayer {

    CustomizableModelPart getHeadLayers();

    void setupHeadLayers(CustomizableModelPart box);

    CustomizableModelPart[] getSkinLayers();

    void setupSkinLayers(CustomizableModelPart[] box);

}
