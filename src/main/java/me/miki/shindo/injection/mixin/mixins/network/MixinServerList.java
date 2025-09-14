package me.miki.shindo.injection.mixin.mixins.network;

import me.miki.shindo.hooks.ServerDataHook;
import me.miki.shindo.injection.interfaces.IMixinServerList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.util.List;

@Mixin(ServerList.class)
public abstract class MixinServerList implements IMixinServerList {
    private static final Logger logger = LogManager.getLogger("Shindo - ServerList");

    @Shadow @Final
    private Minecraft mc;

    @Shadow @Final
    private List<ServerData> servers;


    @Inject(method = "loadServerList", at = {@At(value = "INVOKE", target = "Ljava/util/List;clear()V", shift = At.Shift.AFTER, ordinal = 0)})
    private void loadFeaturedServers(CallbackInfo ci) {
        this.addServerData(new ServerDataHook("Skytiel","abc.com"));
    }

    /**
     * @author MikiDevAHM
     * @reason Featured Servers Duplication Fix
     */
    @Overwrite
    public void saveServerList() {
        try
        {
            NBTTagList nbttaglist = new NBTTagList();

            for (ServerData serverdata : this.servers)
            {
                if (!(serverdata instanceof ServerDataHook)) {
                    nbttaglist.appendTag(serverdata.getNBTCompound());
                }
            }

            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setTag("servers", nbttaglist);
            CompressedStreamTools.safeWrite(nbttagcompound, new File(this.mc.mcDataDir, "servers.dat"));
        } catch (Exception exception) {
            logger.error("Couldn't save server list", exception);
        }
    }

    @Override
    public int getFeaturedServerCount() {
        int count = 0;
        for (ServerData sd : this.servers) {
            if (sd instanceof ServerDataHook) {
                count++;
            }
        }
        return count;
    }

    /**
     * @author LlamaLad7
     * @reason resolve NPE
     */
    @Overwrite
    public ServerData getServerData(int index) {
        try {
            return this.servers.get(index);
        } catch (Exception e) {
            logger.error("Failed to get server data.", e);
            return null;
        }
    }

    /**
     * @author LlamaLad7
     * @reason resolve NPE
     */
    @Overwrite
    public void removeServerData(int index) {
        try {
            this.servers.remove(index);
        } catch (Exception e) {
            logger.error("Failed to remove server data.", e);
        }
    }

    /**
     * @author LlamaLad7
     * @reason resolve NPE
     */
    @Overwrite
    public void addServerData(ServerData server) {
        try {
            this.servers.add(server);
        } catch (Exception e) {
            logger.error("Failed to add server data.", e);
        }
    }

    /**
     * @author LlamaLad7
     * @reason resolve NPE
     */
    @Overwrite
    public void swapServers(int p_78857_1_, int p_78857_2_) {
        try {
            ServerData serverdata = this.getServerData(p_78857_1_);
            this.servers.set(p_78857_1_, this.getServerData(p_78857_2_));
            this.servers.set(p_78857_2_, serverdata);
            this.saveServerList();
        } catch (Exception e) {
            logger.error("Failed to swap servers.", e);
        }
    }

    /**
     * @author LlamaLad7
     * @reason resolve NPE
     */
    @Overwrite
    public void func_147413_a(int index, ServerData server) {
        try {
            this.servers.set(index, server);
        } catch (Exception e) {
            logger.error("Failed to set server data.", e);
        }
    }
}
