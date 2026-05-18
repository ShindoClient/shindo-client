package me.miki.shindo.utils.network

object PunycodeUtils {
    private const val TMIN = 1
    private const val TMAX = 26
    private const val SKEW = 38
    private const val DAMP = 700
    private const val INITIAL_BIAS = 72
    private const val INITIAL_N = 128

    @JvmStatic
    fun punycode(url: String): String {
        var protoEnd = url.indexOf("://")
        protoEnd = if (protoEnd < 0) 0 else protoEnd + 3

        var hostEnd = url.indexOf('/', protoEnd)
        if (hostEnd < 0) {
            hostEnd = url.length
        }

        val hostname = url.substring(protoEnd, hostEnd)

        if (hostname.none { it.toInt() >= 128 }) {
            return url
        }

        val parts = hostname.split('.')
        val sb = StringBuilder()
        var first = true

        sb.append(url, 0, protoEnd)

        for (p in parts) {
            val transform = p.any { it.toInt() >= 128 }

            if (first) {
                first = false
            } else {
                sb.append('.')
            }

            if (transform) {
                sb.append(encodePunycode(p.codePoints().toArray()))
            } else {
                sb.append(p)
            }
        }

        sb.append(url, hostEnd, url.length)
        return sb.toString()
    }

    private fun adaptBias(
        delta: Int,
        numPoints: Int,
        firstTime: Boolean,
    ): Int {
        var d = if (firstTime) delta / DAMP else delta / 2
        d += d / numPoints

        var k = 0
        while (d > ((36 - TMIN) * TMAX) / 2) {
            d /= 36 - TMIN
            k += 36
        }

        return k + ((36 - TMIN + 1) * d) / (d + SKEW)
    }

    private fun encodeNumber(
        dst: StringBuilder,
        qValue: Int,
        bias: Int,
    ) {
        var q = qValue
        var k = 36
        var keepGoing = true

        while (keepGoing) {
            var t = k - bias
            if (t < TMIN) {
                t = TMIN
            } else if (t > TMAX) {
                t = TMAX
            }

            val digit: Int
            if (q < t) {
                digit = q
                keepGoing = false
            } else {
                digit = t + (q - t) % (36 - t)
                q = (q - t) / (36 - t)
            }

            if (digit < 26) {
                dst.append(('a'.toInt() + digit).toChar())
            } else {
                dst.append(('0'.toInt() + digit - 26).toChar())
            }

            k += 36
        }
    }

    private fun encodePunycode(input: IntArray): String {
        val output = StringBuilder()

        for (j in input) {
            if (j < 128) {
                output.append(j.toChar())
            }
        }

        var n = INITIAL_N
        var delta = 0
        var bias = INITIAL_BIAS
        var h = output.length
        val b = h

        if (b > 0) {
            output.append('-')
        }

        while (h < input.size) {
            var m = Int.MAX_VALUE
            for (j in input) {
                if (j in n until m) {
                    m = j
                }
            }

            delta += (m - n) * (h + 1)
            n = m

            for (c in input) {
                if (c < n) {
                    delta++
                } else if (c == n) {
                    encodeNumber(output, delta, bias)
                    bias = adaptBias(delta, h + 1, h == b)
                    delta = 0
                    h++
                }
            }

            delta++
            n++
        }

        return "xn--$output"
    }
}
