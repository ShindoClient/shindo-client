package com.shindoclient.viashindo.platform.viaversion;

import com.viaversion.viaversion.commands.ViaCommandHandler;

import com.shindoclient.viashindo.command.impl.LeakDetectSubCommand;

public class VLBViaCommandHandler extends ViaCommandHandler {

    public VLBViaCommandHandler() {
        super();
        this.registerVLBDefaults();
    }

    public void registerVLBDefaults() {
        this.registerSubCommand(new LeakDetectSubCommand());
    }
}
