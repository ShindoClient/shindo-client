package me.miki.shindo.management.remote.changelog;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.miki.shindo.Shindo;
import me.miki.shindo.utils.JsonUtils;
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.utils.network.HttpUtils;

import java.util.concurrent.CopyOnWriteArrayList;

public class ChangelogManager {

    private final CopyOnWriteArrayList<Changelog> changelogs = new CopyOnWriteArrayList<Changelog>();

    public ChangelogManager() {
        Multithreading.runAsync(() -> loadChangelog());
    }

    private void loadChangelog() {

        JsonObject jsonObject = HttpUtils.readJson("https://cdn.shindoclient.com/data/changelogs/versions/" + Shindo.getInstance().getVerIdentifier() + ".json", null);


        if (jsonObject != null) {

            JsonArray jsonArray = JsonUtils.getArrayProperty(jsonObject, "changelogs");

            if (jsonArray != null) {

                for (JsonElement jsonElement : jsonArray) {

                    Gson gson = new Gson();
                    JsonObject changelogJsonObject = gson.fromJson(jsonElement, JsonObject.class);

                    changelogs.add(new Changelog(JsonUtils.getStringProperty(changelogJsonObject, "text", "null"),
                            ChangelogType.getTypeById(JsonUtils.getIntProperty(changelogJsonObject, "type", 999))));
                }
            }
        }
    }

    public CopyOnWriteArrayList<Changelog> getChangelogs() {
        return changelogs;
    }
}
