package me.miki.shindo.discord;

import me.miki.shindo.Shindo;
import me.miki.shindo.discord.ipc.IPCClient;
import me.miki.shindo.discord.ipc.IPCListener;
import me.miki.shindo.discord.ipc.entities.RichPresence;
import me.miki.shindo.discord.ipc.exceptions.NoDiscordClientException;

import java.time.OffsetDateTime;

public class DiscordRPC {

    private IPCClient client;

    public void start() {

        client = new IPCClient(978250675576258610L);
        client.setListener(new IPCListener() {
            @Override
            public void onReady(IPCClient client) {

                RichPresence.Builder builder = new RichPresence.Builder();

                builder.setState("Playing Shindo Client v" + Shindo.getInstance().getVersion())
                        .setStartTimestamp(OffsetDateTime.now())
                        .setLargeImage("large");

                client.sendRichPresence(builder.build());
            }
        });

        try {
            client.connect();
        } catch (NoDiscordClientException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        client.close();
    }

    public IPCClient getClient() {
        return client;
    }

    public boolean isStarted() {
        return client != null;
    }
}
