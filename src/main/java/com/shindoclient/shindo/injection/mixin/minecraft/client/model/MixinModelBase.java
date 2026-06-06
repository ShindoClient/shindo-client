package com.shindoclient.shindo.injection.mixin.minecraft.client.model;

import com.google.common.collect.Maps;
import com.shindoclient.shindo.injection.interfaces.IMixinModelBase;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.TextureOffset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(ModelBase.class)
public class MixinModelBase implements IMixinModelBase {


    @Shadow
    private final Map<String, TextureOffset> modelTextureMap = Maps.newHashMap();

    @Override
    public void setTextureOffset(String partName, int x, int y) {
        this.modelTextureMap.put(partName, new TextureOffset(x, y));
    }

}


