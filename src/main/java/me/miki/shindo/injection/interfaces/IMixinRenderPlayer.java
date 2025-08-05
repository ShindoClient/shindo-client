package me.miki.shindo.injection.interfaces;

import me.miki.shindo.management.mods.impl.skin3d.layers.BodyLayerFeatureRenderer;
import me.miki.shindo.management.mods.impl.skin3d.layers.HeadLayerFeatureRenderer;

public interface IMixinRenderPlayer {
    boolean hasThinArms();

    HeadLayerFeatureRenderer getHeadLayer();

    BodyLayerFeatureRenderer getBodyLayer();
}