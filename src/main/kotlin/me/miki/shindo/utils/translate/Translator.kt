package me.miki.shindo.utils.translate

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.miki.shindo.utils.TimerUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.StringJoiner

object Translator {

    const val AUTO_DETECT = ""
    const val ENGLISH = "en"
    const val JAPANESE = "ja"
    const val CHINESE_SIMPLIFIED = "zh-Hans"
    const val CHINESE_TRADITIONAL = "zh-Hant"
    const val POLISH = "pl"

    private var authCache: String? = null
    private var timer: TimerUtils? = null

    @Throws(Exception::class)
    private fun auth(): String {
        if (timer == null) {
            timer = TimerUtils()
        }

        if (timer!!.delay(300 * 1000) || authCache == null) {
            val url = URL("https://edge.microsoft.com/translate/auth")
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"

            val content = StringBuilder()
            BufferedReader(InputStreamReader(con.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    content.append(line)
                }
            }

            authCache = content.toString()
            return authCache!!
        }

        return authCache!!
    }

    @JvmStatic
    @Throws(Exception::class)
    fun translate(text: String, from: String, to: String): String {
        val url = URL("https://api.cognitive.microsofttranslator.com/translate?from=$from&to=$to&api-version=3.0&includeSentenceLength=true")
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = "POST"
        con.setRequestProperty("Content-Type", "application/json")
        con.setRequestProperty("authorization", "Bearer ${auth()}")

        val sj = StringJoiner(",", "[", "]")
        sj.add("{\"Text\":\"$text\"}")
        val jsonInputString = sj.toString()

        con.doOutput = true
        con.outputStream.use { os: OutputStream ->
            val input = jsonInputString.toByteArray(StandardCharsets.UTF_8)
            os.write(input, 0, input.size)
        }

        val responseContent = StringBuilder()
        BufferedReader(InputStreamReader(con.inputStream, StandardCharsets.UTF_8)).use { br ->
            var responseLine: String?
            while (br.readLine().also { responseLine = it } != null) {
                responseContent.append(responseLine!!.trim())
            }
        }

        val jsonArray: JsonArray = JsonParser.parseString(responseContent.toString()).asJsonArray

        val sb = StringBuilder()
        for (json: JsonElement in jsonArray) {
            val jsonObject: JsonObject = json.asJsonObject
            val translations: JsonArray = jsonObject.getAsJsonArray("translations")
            for (trans: JsonElement in translations) {
                val translation = trans.asJsonObject
                sb.append(translation["text"].asString)
                sb.append(",")
            }
        }

        return if (sb.isNotEmpty()) sb.substring(0, sb.length - 1) else ""
    }
}
