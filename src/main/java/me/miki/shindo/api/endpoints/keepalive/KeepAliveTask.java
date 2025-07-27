package me.miki.shindo.api.endpoints.keepalive;

import me.miki.shindo.api.ApiSettings;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class KeepAliveTask {

    private static final String API_URL = ApiSettings.API_BASE + "/keepalive";
    private static final int INTERVAL_MS = 30_000; // 30 segundos
    private static Timer timer;

    public static void start(String uuid) {
        if (timer != null) return; // evita start duplicado

        timer = new Timer("KeepAliveTimer", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendKeepAlive(uuid);
                } catch (Exception e) {
                    System.err.println("[KEEPALIVE] Falha ao enviar ping: " + e.getMessage());
                }
            }
        }, 0, INTERVAL_MS);

        System.out.println("[KEEPALIVE] Timer iniciado");
    }

    public static void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            System.out.println("[KEEPALIVE] Timer parado");
        }
    }

    private static void sendKeepAlive(String uuid) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String jsonInputString = "{\"uuid\": \"" + uuid + "\"}";

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int code = connection.getResponseCode();
        if (code != 200) {
            System.err.println("[KEEPALIVE] Resposta inesperada: " + code);
        } else {
            System.out.println("[KEEPALIVE] Ping enviado com sucesso");
        }

        connection.disconnect();
    }
}