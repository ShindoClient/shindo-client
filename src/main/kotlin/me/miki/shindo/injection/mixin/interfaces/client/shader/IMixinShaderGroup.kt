package me.miki.shindo.injection.mixin.interfaces.client.shader

import net.minecraft.client.shader.Shader

interface IMixinShaderGroup {
    fun getListShaders(): List<Shader>
}
