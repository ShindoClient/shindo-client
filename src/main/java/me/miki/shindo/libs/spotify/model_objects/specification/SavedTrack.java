package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.SpotifyApi;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
@JsonDeserialize(builder = SavedTrack.Builder.class)
public class SavedTrack extends AbstractModelObject {
    private final Date addedAt;
    private final Track track;

    private SavedTrack(final Builder builder) {
        super(builder);

        this.addedAt = builder.addedAt;
        this.track = builder.track;
    }
    public Date getAddedAt() {
        return addedAt;
    }
    public Track getTrack() {
        return track;
    }

    @Override
    public String toString() {
        return "SavedTrack(addedAt=" + addedAt + ", track=" + track + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Date addedAt;
        private Track track;
        public Builder setAddedAt(Date addedAt) {
            this.addedAt = addedAt;
            return this;
        }
        public Builder setTrack(Track track) {
            this.track = track;
            return this;
        }

        @Override
        public SavedTrack build() {
            return new SavedTrack(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<SavedTrack> {
        public SavedTrack createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            try {
                return new Builder()
                        .setAddedAt(
                                hasAndNotNull(jsonObject, "added_at")
                                        ? SpotifyApi.parseDefaultDate(jsonObject.get("added_at").getAsString())
                                        : null)
                        .setTrack(
                                hasAndNotNull(jsonObject, "track")
                                        ? new Track.JsonUtil().createModelObject(
                                        jsonObject.getAsJsonObject("track"))
                                        : null)
                        .build();
            } catch (ParseException e) {
                SpotifyApi.LOGGER.log(Level.SEVERE, e.getMessage());
                return null;
            }
        }
    }
}
