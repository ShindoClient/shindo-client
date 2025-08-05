package me.miki.shindo.injection.mixin.mixins.gui;

import me.miki.shindo.injection.interfaces.IMixinChatLine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

@Mixin(ChatLine.class)
public class MixinChatLine implements IMixinChatLine {

    @Unique
    private NetworkPlayerInfo client$playerInfo;

    @Unique
    private static NetworkPlayerInfo client$getPlayerFromNickname(String word, NetHandlerPlayClient connection, Map<String, NetworkPlayerInfo> nicknameCache) {

        if (nicknameCache.isEmpty()) {
            for (NetworkPlayerInfo p : connection.getPlayerInfoMap()) {

                IChatComponent displayName = p.getDisplayName();

                if (displayName != null) {
                    String nickname = displayName.getUnformattedTextForChat();

                    if (word.equals(nickname)) {
                        nicknameCache.clear();
                        return p;
                    }

                    nicknameCache.put(nickname, p);
                }
            }
        } else {
            return nicknameCache.get(word);
        }

        return null;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(int i, IChatComponent iChatComponent, int j, CallbackInfo ci) {

        chatLines.add(new WeakReference<>((ChatLine) (Object) this));
        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
        if (netHandler == null) return;
        Map<String, NetworkPlayerInfo> nicknameCache = new HashMap<>();

        try {
            for (String word : iChatComponent.getFormattedText().split("(§.)|\\W")) {

                if (word.isEmpty()) {
                    continue;
                }

                client$playerInfo = netHandler.getPlayerInfo(word);

                if (client$playerInfo == null) {
                    client$playerInfo = client$getPlayerFromNickname(word, netHandler, nicknameCache);
                }

                if (client$playerInfo != null) {
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public NetworkPlayerInfo client$getPlayerInfo() {
        return client$playerInfo;
    }
}
