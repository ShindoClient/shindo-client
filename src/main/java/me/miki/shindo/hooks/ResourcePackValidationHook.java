package me.miki.shindo.hooks;

import me.miki.shindo.logger.ShindoLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.server.S48PacketResourcePackSend;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class ResourcePackValidationHook {

    private ResourcePackValidationHook() {
    }

    public static boolean validate(S48PacketResourcePackSend packet) {
        try {
            String url = packet.getURL();
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            boolean levelProtocol = "level".equalsIgnoreCase(scheme);

            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme) && !levelProtocol) {
                return true;
            }

            if (levelProtocol) {
                url = URLDecoder.decode(url.substring("level://".length()), StandardCharsets.UTF_8.name());
                if (url.contains("..") || !url.endsWith("/resources.zip")) {
                    notifyPlayer(url);
                    throw new URISyntaxException(url, "Invalid level resource pack path");
                }
            }

            return true;
        } catch (Exception exception) {
            ShindoLogger.warn("Blocked suspicious resource pack URL", exception);
            return false;
        }
    }

    private static void notifyPlayer(String url) {
        ShindoLogger.warn("Blocked malicious resource pack request: " + url);
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Blocked malicious resource pack request."));
        }
    }
}
