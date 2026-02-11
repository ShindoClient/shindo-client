package me.miki.shindo.libs.spotify.enums;

import java.util.HashMap;
import java.util.Map;

public enum ReleaseDatePrecision {

    DAY("day"),
    MONTH("month"),
    YEAR("year");

    private static final Map<String, ReleaseDatePrecision> map = new HashMap<>();

    static {
        for (ReleaseDatePrecision releaseDatePrecision : ReleaseDatePrecision.values()) {
            map.put(releaseDatePrecision.precision, releaseDatePrecision);
        }
    }

    public final String precision;

    ReleaseDatePrecision(final String precision) {
        this.precision = precision;
    }

    public static ReleaseDatePrecision keyOf(String precision) {
        return map.get(precision);
    }

    public String getPrecision() {
        return precision;
    }

}
