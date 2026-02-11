package me.miki.shindo.libs.spotify.model_objects.miscellaneous;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.SpotifyApi;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
import me.miki.shindo.libs.spotify.model_objects.specification.Episode;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
@JsonDeserialize(builder = SavedEpisode.Builder.class)
public class SavedEpisode extends AbstractModelObject {
    private final Date addedAt;
    private final Episode episode;

    private SavedEpisode(final SavedEpisode.Builder builder) {
        super(builder);
        this.addedAt = builder.addedAt;
        this.episode = builder.episode;
    }
    public Date getAddedAt() {
        return addedAt;
    }
    public Episode getEpisode() {
        return episode;
    }

    @Override
    public String toString() {
        return "SavedEpisode(addedAt=" + addedAt + ", episode=" + episode + ")";
    }

    @Override
    public SavedEpisode.Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Date addedAt;
        private Episode episode;
        public SavedEpisode.Builder setAddedAt(Date addedAt) {
            this.addedAt = addedAt;
            return this;
        }
        public SavedEpisode.Builder setEpisode(Episode episode) {
            this.episode = episode;
            return this;
        }

        @Override
        public SavedEpisode build() {
            return new SavedEpisode(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<SavedEpisode> {
        @Override
        public SavedEpisode createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            try {
                return new Builder()
                        .setAddedAt(
                                hasAndNotNull(jsonObject, "added_at")
                                        ? SpotifyApi.parseDefaultDate(jsonObject.get("added_at").getAsString())
                                        : null)
                        .setEpisode(
                                hasAndNotNull(jsonObject, "episode")
                                        ? new Episode.JsonUtil().createModelObject(
                                        jsonObject.getAsJsonObject("episode"))
                                        : null)
                        .build();
            } catch (ParseException e) {
                SpotifyApi.LOGGER.log(Level.SEVERE, e.getMessage());
                return null;
            }
        }
    }
}
