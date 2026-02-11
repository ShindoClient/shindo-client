package me.miki.shindo.libs.romaji;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PowerSet {

    private PowerSet() {
    }

    static <T> Set<Set<T>> powerSet(final Set<T> src) {
        Set<Set<T>> power = Stream.of(src).collect(Collectors.toSet()); // add a input set to the result power set.
        src.forEach(exclusion ->
                power.addAll(
                        powerSet(src.stream()
                                .filter(element -> element != exclusion)
                                .collect(Collectors.toSet()))
                )
        );
        return power;
    }

}
