package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Shinconic

class Skin3DMod : Mod(TranslateText.SKIN_3D, TranslateText.SKIN_3D_DESCRIPTION, ModCategory.RENDER, Shinconic.MOD_SKIN3_D) {
    private val baseVoxelSize = 1.15f
    private val bodyVoxelWidthSize = 1.05f
    private val headVoxelSize = 1.18f

    private val renderDistanceLOD = 14

    init {
        instance = this
    }

    override fun onEnable() {
        super.onEnable()

        val mobends = MoBendsMod.instance
        if (mobends != null && mobends.isToggled()) {
            mobends.setToggled(false)
        }
    }

    fun getBaseVoxelSize(): Float = baseVoxelSize

    fun getBodyVoxelWidthSize(): Float = bodyVoxelWidthSize

    fun getHeadVoxelSize(): Float = headVoxelSize

    fun getRenderDistanceLOD(): Int = renderDistanceLOD

    companion object {
        @JvmField
        var instance: Skin3DMod? = null

        @JvmStatic
        fun getInstance(): Skin3DMod? = instance
    }
}
