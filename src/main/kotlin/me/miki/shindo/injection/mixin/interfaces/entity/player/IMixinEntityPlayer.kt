package me.miki.shindo.injection.mixin.interfaces.entity.player

import me.miki.shindo.management.mods.impl.skin3d.render.CustomizableModelPart

interface IMixinEntityPlayer {

    fun getSkinLayers(): Array<CustomizableModelPart?>?

    fun setupSkinLayers(box: Array<CustomizableModelPart?>)

    fun getHeadLayers(): CustomizableModelPart

    fun setupHeadLayers(box: CustomizableModelPart)
}
