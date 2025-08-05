package me.miki.shindo.api.endpoints;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.miki.shindo.api.ApiSettings;
import me.miki.shindo.logger.ShindoLogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckStatus {

    public JsonObject connect() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(ApiSettings.CHECK_STATUS).openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);

            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                ShindoLogger.error("[API] Erro ao verificar o status da API : HTTP " + responseCode);
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();

                ensureDefault(json, "success", false);
                ensureDefault(json, "ping", false);
                ShindoLogger.info(response.toString());
                return json;
            }

        } catch (Exception e) {
            ShindoLogger.error("[API] Erro ao verificar o status da API : " + e.getMessage(), e);
            return null;
        }
    }

    private void ensureDefault(JsonObject obj, String key, Boolean defaultValue) {
        if (!obj.has(key)) {
            obj.addProperty(key, defaultValue);
        }
    }
}
