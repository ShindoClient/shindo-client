package me.miki.shindo.libs.romaji;

import java.util.*;
import java.util.stream.Collectors;

class Transliteration {

    private final List<Substring> substrings = new ArrayList<>();
    private final Set<System> systems = new HashSet<>(); // All of systems which this transliteration has.
    private int index = 0;

    private Transliteration() {
    }

    static Transliteration valueOf() {
        return new Transliteration();
    }

    static System findSystem(final Substring substring, final Set<System> systems) {
        return systems.stream()
                .sorted(Comparator.reverseOrder())
                .filter(system -> !Objects.isNull(substring.romaji(system)))
                .findFirst()
                .orElse(System.STANDARD);
    }

    static String substringToRomaji(final Substring substring, final Set<System> systems) {
        return substring.hasRomaji()
                ? substring.romaji(findSystem(substring, systems))
                : substring.src();
    }

    static String romaji(final List<Substring> substrings, final Set<System> systems) {
        return substrings.stream()
                .map(sub -> substringToRomaji(sub, systems))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    void tryToAppend(final Substring sub) {
        if (Objects.isNull(sub)) {
            return;
        }
        if (sub.index() != this.index) {
            return;
        }
        this.substrings.add(sub);
        this.systems.addAll(sub.systems());
        this.index += sub.window();
    }

    List<String> romajis() {
        if (this.substrings.isEmpty()) {
            return Collections.emptyList();
        }
        return Filter.expandAndReduce(
                PowerSet.powerSet(this.systems)
                        .stream()
                        .map(systems -> romaji(this.substrings, systems))
                        .distinct()
                        .collect(Collectors.toList())
        );
    }

}

