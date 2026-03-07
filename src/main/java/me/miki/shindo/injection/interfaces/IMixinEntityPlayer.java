package me.miki.shindo.injection.interfaces;

import me.miki.shindo.management.mods.impl.skin3d.render.CustomizableModelPart;

public interface IMixinEntityPlayer {
	
	public CustomizableModelPart getHeadLayers();
	public void setupHeadLayers(CustomizableModelPart box);
	public CustomizableModelPart[] getSkinLayers();
	public void setupSkinLayers(CustomizableModelPart[] box);

}
