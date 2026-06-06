package com.shindoclient.viashindo.platform.viaversion;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;

import com.shindoclient.viashindo.ViaLoadingBase;
import com.shindoclient.viashindo.platform.providers.VLBMovementTransmitterProvider;
import com.shindoclient.viashindo.provider.VLBBaseVersionProvider;

public class VLBViaProviders implements ViaPlatformLoader {

    @Override
    public void load() {

        final ViaProviders providers = Via.getManager().getProviders();
        providers.use(VersionProvider.class, new VLBBaseVersionProvider());
        providers.use(MovementTransmitterProvider.class, new VLBMovementTransmitterProvider());

        if (ViaLoadingBase.getInstance().getProviders() != null) {
            ViaLoadingBase.getInstance().getProviders().accept(providers);
        }
    }

    @Override
    public void unload() {
    }
}
