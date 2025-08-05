package me.miki.shindo.utils.network;

public class PunycodeUtils {

    private static final int TMIN = 1;
    private static final int TMAX = 26;
    private static final int SKEW = 38;
    private static final int DAMP = 700;
    private static final int INITIAL_BIAS = 72;
    private static final int INITIAL_N = 128;

    public static String punycode(String url) {
        int protoEnd = url.indexOf("://");

        if (protoEnd < 0) {
            protoEnd = 0;
        } else {
            protoEnd += 3;
        }

        int hostEnd = url.indexOf('/', protoEnd);
        if (hostEnd < 0) {
            hostEnd = url.length();
        }

        String hostname = url.substring(protoEnd, hostEnd);
        boolean doTransform = false;

        for (int i = 0; i < hostname.length(); i++) {
            if (hostname.charAt(i) >= 128) {
                doTransform = true;
                break;
            }
        }

        if (!doTransform) {
            return url;
        }

        String[] parts = hostname.split("\\.");
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        sb.append(url, 0, protoEnd);

        for (String p : parts) {
            doTransform = false;

            for (int i = 0; i < p.length(); i++) {
                if (p.charAt(i) >= 128) {
                    doTransform = true;
                    break;
                }
            }

            if (first)
                first = false;
            else
                sb.append('.');

            if (doTransform)
                sb.append(encodePunycode(p.codePoints().toArray()));
            else
                sb.append(p);
        }

        sb.append(url, hostEnd, url.length());
        return sb.toString();
    }

    private static int adaptBias(int delta, int numPoints, boolean firstTime) {
        if (firstTime) {
            delta /= DAMP;
        } else {
            delta /= 2;
        }

        delta += delta / numPoints;

        int k = 0;
        while (delta > ((36 - TMIN) * TMAX) / 2) {
            delta /= 36 - TMIN;
            k += 36;
        }

        return k + ((36 - TMIN + 1) * delta) / (delta + SKEW);
    }

    private static void encodeNumber(StringBuilder dst, int q, int bias) {
        boolean keepGoing = true;
        for (int k = 36; keepGoing; k += 36) {
            int t = k - bias;
            if (t < TMIN) t = TMIN;
            else if (t > TMAX) t = TMAX;

            int digit;
            if (q < t) {
                digit = q;
                keepGoing = false;
            } else {
                digit = t + (q - t) % (36 - t);
                q = (q - t) / (36 - t);
            }

            if (digit < 26) {
                dst.append((char) ('a' + digit));
            } else {
                dst.append((char) ('0' + digit - 26));
            }
        }
    }

    private static String encodePunycode(int[] input) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < input.length; i++) {
            if (input[i] < 128) {
                output.append((char) input[i]);
            }
        }

        int n = INITIAL_N;
        int delta = 0;
        int bias = INITIAL_BIAS;
        int h = output.length();
        int b = h;

        if (b > 0)
            output.append('-');

        while (h < input.length) {
            int m = Integer.MAX_VALUE;
            for (int i = 0; i < input.length; i++) {
                if (input[i] >= n && input[i] < m)
                    m = input[i];
            }

            delta += (m - n) * (h + 1);
            n = m;

            for (int i = 0; i < input.length; i++) {
                int c = input[i];
                if (c < n) {
                    delta++;
                } else if (c == n) {
                    encodeNumber(output, delta, bias);
                    bias = adaptBias(delta, h + 1, h == b);
                    delta = 0;
                    h++;
                }
            }

            delta++;
            n++;
        }

        return "xn--" + output;
    }
}