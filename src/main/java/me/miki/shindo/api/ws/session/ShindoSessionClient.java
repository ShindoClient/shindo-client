package me.miki.shindo.api.ws.session;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.miki.shindo.api.ws.ShindoWsService;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.utils.network.HttpUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Responsável por solicitar tokens de sessão seguros para o gateway WebSocket.
 * Usa o endpoint REST configurado (service-role) exposto pelo backend.
 */
public class ShindoSessionClient implements ShindoWsService.SessionProvider {

    private static final String DEFAULT_ENDPOINT = "https://api.shindoclient.com/v1/session";
    private static final String DEFAULT_USER_AGENT = "ShindoClient/1.8.9 (ws-session)";

    private final String endpoint;
    private final String apiKey;
    private final String userAgent;

    public ShindoSessionClient() {
        this(
                System.getProperty("shindo.api.sessionEndpoint", DEFAULT_ENDPOINT),
                System.getProperty("shindo.api.sessionKey", System.getenv("SHINDO_SESSION_KEY")),
                System.getProperty("shindo.api.sessionUserAgent", DEFAULT_USER_AGENT)
        );
    }

    public ShindoSessionClient(String endpoint, String apiKey) {
        this(endpoint, apiKey, DEFAULT_USER_AGENT);
    }

    public ShindoSessionClient(String endpoint, String apiKey, String userAgent) {
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.apiKey = apiKey;
        this.userAgent = userAgent != null ? userAgent : DEFAULT_USER_AGENT;
    }

    @Override
    public CompletableFuture<ShindoWsService.AuthSession> acquireSession(ShindoWsService.PlayerInfo info) {
        return CompletableFuture.supplyAsync(() -> requestSession(info));
    }

    private ShindoWsService.AuthSession requestSession(ShindoWsService.PlayerInfo info) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                ShindoLogger.warn("Session API key is not configured. Set -Dshindo.api.sessionKey or SHINDO_SESSION_KEY.");
                return ShindoWsService.AuthSession.empty();
            }

            JsonObject body = new JsonObject();
            body.addProperty("uuid", info.uuid);
            body.addProperty("name", info.name);
            body.addProperty("accountType", info.accountType);

            JsonArray rolesArray = new JsonArray();
            if (info.roles != null) {
                for (String role : info.roles) {
                    rolesArray.add(role);
                }
            }
            body.add("roles", rolesArray);

            Map<String, String> headers = new HashMap<>();
            headers.put("X-Session-Key", apiKey);
            headers.put("User-Agent", userAgent);

            JsonObject response = HttpUtils.postJson(endpoint, body, headers);
            if (response == null) {
                ShindoLogger.warn("Shindo session endpoint returned null response");
                return ShindoWsService.AuthSession.empty();
            }

            if (response.has("success") && !response.get("success").getAsBoolean()) {
                ShindoLogger.warn("Shindo session endpoint reported failure: " + response);
                return ShindoWsService.AuthSession.empty();
            }

            String token = response.has("token") ? response.get("token").getAsString() : null;
            String sessionId = response.has("sessionId") ? response.get("sessionId").getAsString() : null;
            long expiresAt = response.has("expiresAt") ? response.get("expiresAt").getAsLong() : -1L;

            String[] resolvedRoles = info.roles;
            if (response.has("roles") && response.get("roles").isJsonArray()) {
                JsonArray arr = response.getAsJsonArray("roles");
                resolvedRoles = new String[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    resolvedRoles[i] = arr.get(i).getAsString();
                }
            }

            return ShindoWsService.AuthSession.of(token, sessionId, expiresAt, resolvedRoles);
        } catch (Exception e) {
            ShindoLogger.error("Failed to request session token", e);
            throw new CompletionException(e);
        }
    }
}
