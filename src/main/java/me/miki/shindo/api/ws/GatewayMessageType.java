package me.miki.shindo.api.ws;

public enum GatewayMessageType {
    AUTH("auth"),
    AUTH_OK("auth.ok"),
    AUTH_ERROR("auth.error"),
    ROLES_UPDATE("roles.update");

    private final String type;

    GatewayMessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public boolean matches(String other) {
        return type.equals(other);
    }
}
