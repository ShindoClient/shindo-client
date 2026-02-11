package me.miki.shindo.libs.spotify.enums;

import java.util.HashMap;
import java.util.Map;

public enum Modality {

    MAJOR(1),
    MINOR(0);

    private static final Map<Integer, Modality> map = new HashMap<>();

    static {
        for (Modality modality : Modality.values()) {
            map.put(modality.mode, modality);
        }
    }

    public final int mode;

    Modality(final int mode) {
        this.mode = mode;
    }

    public static Modality keyOf(int mode) {
        return map.get(mode);
    }

    public int getType() {
        return this.mode;
    }

}
