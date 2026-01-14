package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
class Skin3DMod :
    Mod(TranslateText.SKIN_3D, TranslateText.SKIN_3D_DESCRIPTION, ModCategory.RENDER, LegacyIcon.MOD_SKIN3_D) {
    private val baseVoxelSize = 1.15f
    private val bodyVoxelWidthSize = 1.05f
    private val headVoxelSize = 1.18f

    private val renderDistanceLOD = 14

    init {
        instance = this
    }

    public override fun onEnable() {
        super.onEnable()

        val mobends = MoBendsMod.instance
        if (mobends != null && mobends.isToggled()) {
            mobends.setToggled(false)
        }
    }

    fun getBaseVoxelSize(): Float {
        return baseVoxelSize
    }

    fun getBodyVoxelWidthSize(): Float {
        return bodyVoxelWidthSize
    }

    fun getHeadVoxelSize(): Float {
        return headVoxelSize
    }

    fun getRenderDistanceLOD(): Int {
        return renderDistanceLOD
    }

    companion object {
        @JvmField
        var instance: Skin3DMod? = null

        @JvmStatic
        fun getInstance(): Skin3DMod? {
            return instance
        }
    }
}




