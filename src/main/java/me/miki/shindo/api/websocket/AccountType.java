package me.miki.shindo.api.websocket;

import java.util.Locale;

public enum AccountType {
    LOCAL,
    MICROSOFT,
    OFFLINE;

    public static AccountType from(String raw) {
        if (raw == null) {
            return LOCAL;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        for (AccountType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        return LOCAL;
    }

    public String getWireValue() {
        return name();
    }
}
