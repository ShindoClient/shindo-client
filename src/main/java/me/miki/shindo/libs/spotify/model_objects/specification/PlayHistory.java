package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.SpotifyApi;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
@JsonDeserialize(builder = PlayHistory.Builder.class)
public class PlayHistory extends AbstractModelObject {
    private final Track track;
    private final Date playedAt;
    private final Context context;

    private PlayHistory(final Builder builder) {
        super(builder);

        this.track = builder.track;
        this.playedAt = builder.playedAt;
        this.context = builder.context;
    }
    public Track getTrack() {
        return track;
    }
    public Date getPlayedAt() {
        return playedAt;
    }
    public Context getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "PlayHistory(track=" + track + ", playedAt=" + playedAt + ", context=" + context + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Track track;
        private Date playedAt;
        private Context context;
        public Builder setTrack(Track track) {
            this.track = track;
            return this;
        }
        public Builder setPlayedAt(Date playedAt) {
            this.playedAt = playedAt;
            return this;
        }
        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        @Override
        public PlayHistory build() {
            return new PlayHistory(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<PlayHistory> {
        public PlayHistory createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            try {
                return new Builder()
                        .setTrack(
                                hasAndNotNull(jsonObject, "track")
                                        ? new Track.JsonUtil().createModelObject(
                                        jsonObject.getAsJsonObject("track"))
                                        : null)
                        .setPlayedAt(
                                hasAndNotNull(jsonObject, "played_at")
                                        ? SpotifyApi.parseDefaultDate(jsonObject.get("played_at").getAsString())
                                        : null)
                        .setContext(
                                hasAndNotNull(jsonObject, "context")
                                        ? new Context.JsonUtil().createModelObject(
                                        jsonObject.getAsJsonObject("context"))
                                        : null)
                        .build();
            } catch (ParseException e) {
                SpotifyApi.LOGGER.log(Level.SEVERE, e.getMessage());
                return null;
            }
        }
    }
}
