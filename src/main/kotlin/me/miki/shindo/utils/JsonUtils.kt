package me.miki.shindo.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.util.regex.Pattern

object JsonUtils {

    @JvmStatic
    fun toStringList(jsonArray: JsonArray): List<String> {
        val list: MutableList<String> = ArrayList()
        for (element in jsonArray) {
            if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
                list.add(element.asString)
            }
        }
        return list
    }

    @JvmStatic
    fun hasProperty(jsonObject: JsonObject, key: String): Boolean = getProperty(jsonObject, key) != null

    @JvmStatic
    fun getStringProperty(jsonObject: JsonObject, key: String, defaultValue: String?): String? {
        val value = getProperty(jsonObject, key)
        if (value == null || !value.isJsonPrimitive || !value.asJsonPrimitive.isString) {
            return defaultValue
        }
        return value.asJsonPrimitive.asString
    }

    @JvmStatic
    fun getFloatProperty(jsonObject: JsonObject, key: String, defaultValue: Float): Float {
        return getNumberProperty(jsonObject, key, defaultValue).toFloat()
    }

    @JvmStatic
    fun getDoubleProperty(jsonObject: JsonObject, key: String, defaultValue: Double): Double {
        return getNumberProperty(jsonObject, key, defaultValue).toDouble()
    }

    @JvmStatic
    fun getLongProperty(jsonObject: JsonObject, key: String, defaultValue: Long): Long {
        return getNumberProperty(jsonObject, key, defaultValue).toLong()
    }

    @JvmStatic
    fun getIntProperty(jsonObject: JsonObject, key: String, defaultValue: Int): Int {
        return getNumberProperty(jsonObject, key, defaultValue).toInt()
    }

    @JvmStatic
    fun getNumberProperty(jsonObject: JsonObject, key: String, defaultValue: Number): Number {
        val value = getProperty(jsonObject, key)
        if (value == null || !value.isJsonPrimitive || !value.asJsonPrimitive.isNumber) {
            return defaultValue
        }
        return value.asJsonPrimitive.asNumber
    }

    @JvmStatic
    fun getBooleanProperty(jsonObject: JsonObject, key: String, defaultValue: Boolean): Boolean {
        val value = getProperty(jsonObject, key)
        if (value == null || !value.isJsonPrimitive || !value.asJsonPrimitive.isBoolean) {
            return defaultValue
        }
        return value.asJsonPrimitive.asBoolean
    }

    @JvmStatic
    fun getArrayProperty(jsonObject: JsonObject, key: String): JsonArray {
        val result = getProperty(jsonObject, key)
        return if (result != null && result.isJsonArray) result.asJsonArray else JsonArray()
    }

    @JvmStatic
    fun getObjectProperty(jsonObject: JsonObject, key: String): JsonObject? {
        val result = getProperty(jsonObject, key)
        return if (result != null && result.isJsonObject) result.asJsonObject else null
    }

    @JvmStatic
    fun getProperty(jsonObject: JsonObject, key: String?): JsonElement? {
        if (key == null) {
            throw IllegalArgumentException("Property key cannot be null")
        } else if (key.isEmpty()) {
            return jsonObject
        }

        val tokens = tokenizeKey(key)
        var parent: JsonObject = jsonObject

        for (i in tokens.indices) {

            val keyToken = tokens[i].replace("\\\\,".toRegex(), ",")
            val child = parent.get(keyToken)
            if (i + 1 == tokens.size) {
                return child
            }

            if (child is JsonObject) {
                parent = child
                continue
            }
            break
        }
        return null
    }

    @JvmStatic
    fun parseBooleanGrid(element: JsonElement?): Array<BooleanArray>? {
        if (element == null || !element.isJsonArray) {
            return null
        }
        val rows = element.asJsonArray
        val grid = Array(rows.size()) { BooleanArray(0) }
        for (i in 0 until rows.size()) {
            val rowElement = rows[i]
            if (!rowElement.isJsonArray) {
                grid[i] = BooleanArray(0)
                continue
            }
            val cols = rowElement.asJsonArray
            val row = BooleanArray(cols.size())
            for (j in 0 until cols.size()) {
                row[j] = cols[j].isJsonPrimitive && cols[j].asJsonPrimitive.isBoolean && cols[j].asBoolean
            }
            grid[i] = row
        }
        return grid
    }

    @JvmStatic
    fun parseIntGrid(element: JsonElement?): Array<IntArray>? {
        if (element == null || !element.isJsonArray) {
            return null
        }
        val rows = element.asJsonArray
        val grid = Array(rows.size()) { IntArray(0) }
        for (i in 0 until rows.size()) {
            val rowElement = rows[i]
            if (!rowElement.isJsonArray) {
                grid[i] = IntArray(0)
                continue
            }
            val cols = rowElement.asJsonArray
            val row = IntArray(cols.size())
            for (j in 0 until cols.size()) {
                row[j] = if (cols[j].isJsonPrimitive && cols[j].asJsonPrimitive.isNumber) cols[j].asInt else 0
            }
            grid[i] = row
        }
        return grid
    }

    @JvmStatic
    fun toBooleanGrid(grid: Array<BooleanArray>?): JsonArray {
        val rows = JsonArray()
        if (grid == null) {
            return rows
        }
        for (row in grid) {
            val cols = JsonArray()
            if (row != null) {
                for (value in row) {
                    cols.add(JsonPrimitive(value))
                }
            }
            rows.add(cols)
        }
        return rows
    }

    @JvmStatic
    fun toIntGrid(grid: Array<IntArray>?): JsonArray {
        val rows = JsonArray()
        if (grid == null) {
            return rows
        }
        for (row in grid) {
            val cols = JsonArray()
            if (row != null) {
                for (value in row) {
                    cols.add(JsonPrimitive(value))
                }
            }
            rows.add(cols)
        }
        return rows
    }

    private fun tokenizeKey(key: String): Array<String> {
        return Pattern.compile("(?<!\\\\),").split(key)
    }
}
