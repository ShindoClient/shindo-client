package me.miki.shindo.injection.mixin.interfaces.client.gui

import kotlin.jvm.JvmField
import java.lang.ref.WeakReference
import java.util.HashSet
import net.minecraft.client.gui.ChatLine
import net.minecraft.client.network.NetworkPlayerInfo

interface IMixinChatLine {
    companion object {
        @JvmField
        val chatLines = HashSet<WeakReference<ChatLine>>()
    }

    fun `client$getPlayerInfo`(): NetworkPlayerInfo
}
