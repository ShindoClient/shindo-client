package me.miki.shindo.injection.mixin.interfaces.client.gui;

import java.lang.ref.WeakReference;
import java.util.HashSet;

public interface IMixinChatLine {
    HashSet<WeakReference<Object>> chatLines = new HashSet<>();

    Object client$getPlayerInfo();
}
