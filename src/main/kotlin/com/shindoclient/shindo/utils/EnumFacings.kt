package com.shindoclient.shindo.utils

import net.minecraft.util.EnumFacing

object EnumFacings {
    @JvmField
    val FACINGS =
        arrayOf(
            EnumFacing.DOWN,
            EnumFacing.UP,
            EnumFacing.NORTH,
            EnumFacing.SOUTH,
            EnumFacing.WEST,
            EnumFacing.EAST,
        )

    @JvmField
    val HORIZONTAL_FACINGS =
        arrayOf(
            EnumFacing.NORTH,
            EnumFacing.EAST,
            EnumFacing.SOUTH,
            EnumFacing.WEST,
        )

    @JvmField
    val VERTICAL_FACINGS = arrayOf(EnumFacing.UP, EnumFacing.DOWN)
}
