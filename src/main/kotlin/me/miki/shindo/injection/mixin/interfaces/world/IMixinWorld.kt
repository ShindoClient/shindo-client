package me.miki.shindo.injection.mixin.interfaces.world

interface IMixinWorld {
    fun `client$isLoaded`(x: Int, z: Int, allowEmpty: Boolean): Boolean
}
