package me.miki.shindo.libs.romaji;

import java.util.Map;
import java.util.Set;

class Romaji {

    private final Map<System, String> map;
    private final Set<System> systems;

    private Romaji(final Map<System, String> map) {
        this.map = map;
        this.systems = map.keySet();
    }

    static Romaji valueOf(final Map<System, String> map) {
        return new Romaji(map);
    }

    Set<System> systems() {
        return this.systems;
    }

    String get(final System system) {
        return this.map.get(system);
    }

}
