package me.miki.shindo.viaversion;

import com.viaversion.viaversion.api.connection.UserConnection;
import me.miki.shindo.viaversion.netty.VLBPipeline;

public class MCPVLBPipeline extends VLBPipeline {

    public MCPVLBPipeline(UserConnection user) {
        super(user);
    }

    @Override
    public String getDecoderHandlerName() {
        return "decoder";
    }

    @Override
    public String getEncoderHandlerName() {
        return "encoder";
    }

    @Override
    public String getDecompressionHandlerName() {
        return "decompress";
    }

    @Override
    public String getCompressionHandlerName() {
        return "compress";
    }
}
