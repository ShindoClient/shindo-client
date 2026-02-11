package me.miki.shindo.libs.romaji;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("WeakerAccess")
public class Transliterator {
    @SuppressWarnings("WeakerAccess")
    public static List<String> transliterate(final String src) {
        if (Objects.isNull(src) || src.isEmpty()) {
            return Collections.emptyList();
        }

        Transliteration lookahead1 = Transliteration.valueOf();
        Transliteration lookahead0 = Transliteration.valueOf();

        IntStream.range(0, src.length())
                .forEachOrdered(index -> {
                    lookahead1.tryToAppend(Substring.lookahead(1, src, index));
                    Substring sub = Substring.valueOf(src, index);
                    lookahead1.tryToAppend(sub);
                    lookahead0.tryToAppend(sub);
                });

        return Stream.concat(lookahead1.romajis().stream(), lookahead0.romajis().stream())
                .filter(s -> !s.equals(src))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

}
