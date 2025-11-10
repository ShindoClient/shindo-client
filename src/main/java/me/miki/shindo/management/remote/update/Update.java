package me.miki.shindo.management.remote.update;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.Shindo;
import me.miki.shindo.utils.JsonUtils;
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.utils.network.HttpUtils;

public class Update {

    @Getter
    @Setter
    String updateLink = "https://shindoclient.com/";
    String updateVersionString = "something is broken lmao";
    int updateBuildID = 0;

    public String getVersionString() {
        return updateVersionString;
    }

    public void setVersionString(String in) {
        this.updateVersionString = in;
    }

    public int getBuildID() {
        return updateBuildID;
    }

    public void setBuildID(int in) {
        this.updateBuildID = in;
    }

    public void check() {
        try {
            Multithreading.runAsync(this::checkUpdates);
        } catch (Exception ignored) {
        }
    }

    public void checkForUpdates() {
        Shindo g = Shindo.getInstance();
        if (g.getVerIdentifier() < this.updateBuildID) {
            g.setUpdateNeeded(true);
        }
    }

    private void checkUpdates() {
        JsonObject jsonObject = HttpUtils.readJson("https://cdn.shindoclient.com/data/meta/client.json", null);
        if (jsonObject != null) {
            setUpdateLink(JsonUtils.getStringProperty(jsonObject, "updatelink", "https://shindoclient.com/"));
            setVersionString(JsonUtils.getStringProperty(jsonObject, "latestversionstring", "something is broken lmao"));
            setBuildID(JsonUtils.getIntProperty(jsonObject, "latestversion", 0));
            checkForUpdates();
        }
    }

}
