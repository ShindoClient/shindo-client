package me.miki.shindo.injection.interfaces;


import me.miki.shindo.management.mods.impl.skin3d.layers.BodyLayerFeatureRenderer;
import me.miki.shindo.management.mods.impl.skin3d.layers.HeadLayerFeatureRenderer;

public interface IMixinRenderPlayer {
	public boolean hasThinArms();
	public HeadLayerFeatureRenderer getHeadLayer();
	public BodyLayerFeatureRenderer getBodyLayer();
}