package me.miki.shindo.libs.romaji;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Filter {


    private static final Pattern PATTERN_XTU = Pattern.compile("(xtsu|ltsu|ltu)(?!xtsu|ltsu|ltu)([bcdfghjklmnpqrstvwxyz])", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    static String replaceXtu(final String src) {
        return PATTERN_XTU.matcher(src).replaceAll("$2$2");
    }

    static String replaceOuToO(final String src) {
        return src.replace("ou", "o");
    }

    static List<String> expandAndReduce(final List<String> src) {
        return Stream.concat(
                src.stream(), // append the original list.
                src.stream()
                        .map(s -> {
                            s = replaceOuToO(s);
                            s = replaceXtu(s);
                            return s;
                        }))
                .distinct()
                .collect(Collectors.toList());
    }
}
