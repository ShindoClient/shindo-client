package me.miki.shindo.libs.spotify.enums;

import java.util.HashMap;
import java.util.Map;

public enum CopyrightType {

    C("c"),
    P("p");

    private static final Map<String, CopyrightType> map = new HashMap<>();

    static {
        for (CopyrightType copyrightType : CopyrightType.values()) {
            map.put(copyrightType.type, copyrightType);
        }
    }

    public final String type;

    CopyrightType(final String type) {
        this.type = type;
    }

    public static CopyrightType keyOf(String type) {
        return map.get(type);
    }

    public String getType() {
        return type;
    }

}
