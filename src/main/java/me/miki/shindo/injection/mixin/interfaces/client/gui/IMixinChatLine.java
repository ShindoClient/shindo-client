package me.miki.shindo.injection.mixin.interfaces.client.gui;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.lang.ref.WeakReference;
import java.util.HashSet;

public interface IMixinChatLine {
    HashSet<WeakReference<ChatLine>> chatLines = new HashSet<>();

    NetworkPlayerInfo client$getPlayerInfo();
}

