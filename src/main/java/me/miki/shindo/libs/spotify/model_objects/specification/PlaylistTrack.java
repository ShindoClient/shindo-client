package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.SpotifyApi;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
import me.miki.shindo.libs.spotify.model_objects.IPlaylistItem;
import me.miki.shindo.libs.spotify.model_objects.utils.PlaylistItemFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
@JsonDeserialize(builder = PlaylistTrack.Builder.class)
public class PlaylistTrack extends AbstractModelObject {
    private final Date addedAt;
    private final User addedBy;
    private final Boolean isLocal;
    private final IPlaylistItem track;

    private PlaylistTrack(final Builder builder) {
        super(builder);

        this.addedAt = builder.addedAt;
        this.addedBy = builder.addedBy;
        this.isLocal = builder.isLocal;
        this.track = builder.track;
    }
    public Date getAddedAt() {
        return addedAt;
    }
    public User getAddedBy() {
        return addedBy;
    }
    public Boolean getIsLocal() {
        return isLocal;
    }
    public IPlaylistItem getTrack() {
        return track;
    }

    @Override
    public String toString() {
        return "PlaylistTrack(track=" + track + ", addedAt=" + addedAt + ", addedBy=" + addedBy + ", isLocal=" + isLocal
                + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Date addedAt;
        private User addedBy;
        private Boolean isLocal;
        private IPlaylistItem track;
        public Builder setAddedAt(Date addedAt) {
            this.addedAt = addedAt;
            return this;
        }
        public Builder setAddedBy(User addedBy) {
            this.addedBy = addedBy;
            return this;
        }
        public Builder setIsLocal(Boolean isLocal) {
            this.isLocal = isLocal;
            return this;
        }
        public Builder setTrack(IPlaylistItem track) {
            this.track = track;
            return this;
        }

        @Override
        public PlaylistTrack build() {
            return new PlaylistTrack(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<PlaylistTrack> {
        public PlaylistTrack createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            try {
                IPlaylistItem track = null;

                if (hasAndNotNull(jsonObject, "track")) {
                    final JsonObject trackObj = jsonObject.getAsJsonObject("track");

                    track = PlaylistItemFactory.createPlaylistItem(trackObj);
                }

                return new Builder()
                        .setAddedAt(
                                hasAndNotNull(jsonObject, "added_at")
                                        ? SpotifyApi.parseDefaultDate(jsonObject.get("added_at").getAsString())
                                        : null)
                        .setAddedBy(
                                hasAndNotNull(jsonObject, "added_by")
                                        ? new User.JsonUtil().createModelObject(
                                        jsonObject.get("added_by").getAsJsonObject())
                                        : null)
                        .setIsLocal(
                                hasAndNotNull(jsonObject, "is_local")
                                        ? jsonObject.get("is_local").getAsBoolean()
                                        : null)
                        .setTrack(track)
                        .build();
            } catch (ParseException e) {
                SpotifyApi.LOGGER.log(Level.SEVERE, e.getMessage());
                return null;
            }
        }
    }
}
