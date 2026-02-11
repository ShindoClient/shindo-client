package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.SpotifyApi;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
@JsonDeserialize(builder = SavedAlbum.Builder.class)
public class SavedAlbum extends AbstractModelObject {
    private final Date addedAt;
    private final Album album;

    private SavedAlbum(final Builder builder) {
        super(builder);

        this.addedAt = builder.addedAt;
        this.album = builder.album;
    }
    public Date getAddedAt() {
        return addedAt;
    }
    public Album getAlbum() {
        return album;
    }

    @Override
    public String toString() {
        return "SavedAlbum(addedAt=" + addedAt + ", album=" + album + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Date addedAt;
        private Album album;
        public Builder setAddedAt(Date addedAt) {
            this.addedAt = addedAt;
            return this;
        }
        public Builder setAlbum(Album album) {
            this.album = album;
            return this;
        }

        @Override
        public SavedAlbum build() {
            return new SavedAlbum(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<SavedAlbum> {
        public SavedAlbum createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            try {
                return new Builder()
                        .setAddedAt(
                                hasAndNotNull(jsonObject, "added_at")
                                        ? SpotifyApi.parseDefaultDate(jsonObject.get("added_at").getAsString())
                                        : null)
                        .setAlbum(
                                hasAndNotNull(jsonObject, "album")
                                        ? new Album.JsonUtil().createModelObject(
                                        jsonObject.getAsJsonObject("album"))
                                        : null)
                        .build();
            } catch (ParseException e) {
                SpotifyApi.LOGGER.log(Level.SEVERE, e.getMessage());
                return null;
            }
        }
    }
}
