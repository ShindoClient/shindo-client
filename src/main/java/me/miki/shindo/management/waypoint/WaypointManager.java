package me.miki.shindo.management.waypoint;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.JsonUtils;
import me.miki.shindo.utils.ServerUtils;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class WaypointManager {

    private final Minecraft mc = Minecraft.getMinecraft();

    private final ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();

    public WaypointManager() {

        FileManager fileManager = Shindo.getInstance().getFileManager();
        File waypointFile = new File(fileManager.getShindoDir(), "Waypoint.json");

        fileManager.createFile(waypointFile);

        load(waypointFile);
    }

    public void load(File file) {

        try (FileReader reader = new FileReader(file)) {

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            if (jsonObject != null && jsonObject.isJsonObject()) {

                JsonArray jsonArray = JsonUtils.getArrayProperty(jsonObject, "Waypoints");

                if (jsonArray != null) {

                    Iterator<JsonElement> iterator = jsonArray.iterator();

                    while (iterator.hasNext()) {

                        JsonElement jsonElement = iterator.next();
                        JsonObject wJsonObject = gson.fromJson(jsonElement, JsonObject.class);

                        waypoints.add(new Waypoint(JsonUtils.getStringProperty(wJsonObject, "World", ""),
                                JsonUtils.getStringProperty(wJsonObject, "Name", ""),
                                JsonUtils.getDoubleProperty(wJsonObject, "X", 0),
                                JsonUtils.getDoubleProperty(wJsonObject, "Y", 0),
                                JsonUtils.getDoubleProperty(wJsonObject, "Z", 0),
                                ColorUtils.getColorByInt(JsonUtils.getIntProperty(wJsonObject, "Color", 0))));
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public void save() {

        FileManager fileManager = Shindo.getInstance().getFileManager();
        File waypointFile = new File(fileManager.getShindoDir(), "Waypoint.json");

        try (FileWriter writer = new FileWriter(waypointFile)) {

            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();
            JsonArray jsonArray = new JsonArray();

            for (Waypoint waypoint : waypoints) {

                JsonObject wJsonObject = new JsonObject();

                wJsonObject.addProperty("World", waypoint.getWorld());
                wJsonObject.addProperty("Name", waypoint.getName());
                wJsonObject.addProperty("X", waypoint.getX());
                wJsonObject.addProperty("Y", waypoint.getY());
                wJsonObject.addProperty("Z", waypoint.getZ());
                wJsonObject.addProperty("Color", waypoint.getColor().getRGB());

                jsonArray.add(wJsonObject);
            }

            jsonObject.add("Waypoints", jsonArray);

            gson.toJson(jsonObject, writer);
        } catch (Exception e) {
        }
    }

    public String getWorld() {

        if (ServerUtils.isJoinServer()) {
            return "server-" + ServerUtils.getServerIP() + "-" + mc.theWorld.provider.getDimensionId();
        }

        return "local-" + mc.getIntegratedServer().getFolderName() + "-" + mc.theWorld.provider.getDimensionId();
    }

    public ArrayList<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void addWaypoint(String name, double x, double y, double z, Color color) {
        waypoints.add(new Waypoint(getWorld(), name, x, y, z, color));
    }
}
