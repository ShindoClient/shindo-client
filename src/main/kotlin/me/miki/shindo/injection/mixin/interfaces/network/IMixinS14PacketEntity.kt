package me.miki.shindo.injection.mixin.interfaces.network

interface IMixinS14PacketEntity {
    fun getEntityId(): Int

    fun getPosX(): Byte

    fun getPosY(): Byte

    fun getPosZ(): Byte
}
