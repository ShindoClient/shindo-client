package com.shindoclient.shindo.injection.interfaces;


import com.shindoclient.shindo.management.mods.impl.skin3d.layers.BodyLayerFeatureRenderer;
import com.shindoclient.shindo.management.mods.impl.skin3d.layers.HeadLayerFeatureRenderer;

public interface IMixinRenderPlayer {
    boolean hasThinArms();

    HeadLayerFeatureRenderer getHeadLayer();

    BodyLayerFeatureRenderer getBodyLayer();
}