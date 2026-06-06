package com.shindoclient.shindo.management.waypoint

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.utils.ColorUtils
import com.shindoclient.shindo.utils.JsonUtils
import com.shindoclient.shindo.utils.ServerUtils
import net.minecraft.client.Minecraft
import java.awt.Color
import java.io.File

class WaypointManager {
    private val mc = Minecraft.getMinecraft()
    private val waypoints = ArrayList<Waypoint>()

    init {
        val fileManager = Shindo.getInstance().getFileManager()
        val waypointFile = File(fileManager.shindoDir, "Waypoint.json")
        fileManager.createFile(waypointFile)
        load(waypointFile)
    }

    fun load(file: File) {
        try {
            file.reader().use { reader ->
                val gson = Gson()
                val jsonObject = gson.fromJson(reader, JsonObject::class.java) ?: return
                if (!jsonObject.isJsonObject) return
                val jsonArray = JsonUtils.getArrayProperty(jsonObject, "Waypoints") ?: return
                for (element in jsonArray) {
                    val wObj = gson.fromJson(element, JsonObject::class.java)
                    waypoints.add(
                        Waypoint(
                            JsonUtils.getStringProperty(wObj, "World", "").toString(),
                            JsonUtils.getStringProperty(wObj, "Name", "").toString(),
                            JsonUtils.getDoubleProperty(wObj, "X", 0.0),
                            JsonUtils.getDoubleProperty(wObj, "Y", 0.0),
                            JsonUtils.getDoubleProperty(wObj, "Z", 0.0),
                            ColorUtils.getColorByInt(JsonUtils.getIntProperty(wObj, "Color", 0)),
                        ),
                    )
                }
            }
        } catch (_: Exception) {
        }
    }

    fun save() {
        val fileManager = Shindo.getInstance().getFileManager()
        val waypointFile = File(fileManager.shindoDir, "Waypoint.json")
        try {
            waypointFile.writer().use { writer ->
                val gson = Gson()
                val jsonObject = JsonObject()
                val jsonArray = JsonArray()
                for (waypoint in waypoints) {
                    val wObj = JsonObject()
                    wObj.addProperty("World", waypoint.getWorld())
                    wObj.addProperty("Name", waypoint.getName())
                    wObj.addProperty("X", waypoint.getX())
                    wObj.addProperty("Y", waypoint.getY())
                    wObj.addProperty("Z", waypoint.getZ())
                    wObj.addProperty("Color", waypoint.getColor().rgb)
                    jsonArray.add(wObj)
                }
                jsonObject.add("Waypoints", jsonArray)
                gson.toJson(jsonObject, writer)
            }
        } catch (_: Exception) {
        }
    }

    fun getWorld(): String =
        if (ServerUtils.isJoinServer()) {
            "server-${ServerUtils.getServerIP()}-${mc.theWorld.provider.dimensionId}"
        } else {
            "local-${mc.theWorld.saveHandler.worldDirectoryName}-${mc.theWorld.provider.dimensionId}"
        }

    fun getWaypoints(): ArrayList<Waypoint> = waypoints

    fun addWaypoint(
        name: String,
        x: Double,
        y: Double,
        z: Double,
        color: Color,
    ) {
        waypoints.add(Waypoint(getWorld(), name, x, y, z, color))
    }
}
