package me.miki.shindo.injection.mixin.interfaces.client.renderer.entity

import me.miki.shindo.management.mods.impl.skin3d.layers.BodyLayerFeatureRenderer
import me.miki.shindo.management.mods.impl.skin3d.layers.HeadLayerFeatureRenderer

interface IMixinRenderPlayer {
    fun hasThinArms(): Boolean

    fun getHeadLayer(): HeadLayerFeatureRenderer

    fun getBodyLayer(): BodyLayerFeatureRenderer
}
