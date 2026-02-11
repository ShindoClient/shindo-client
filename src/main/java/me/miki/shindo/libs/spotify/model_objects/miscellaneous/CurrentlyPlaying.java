package me.miki.shindo.libs.spotify.model_objects.miscellaneous;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.enums.CurrentlyPlayingType;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
import me.miki.shindo.libs.spotify.model_objects.IPlaylistItem;
import me.miki.shindo.libs.spotify.model_objects.special.Actions;
import me.miki.shindo.libs.spotify.model_objects.specification.Context;
import me.miki.shindo.libs.spotify.model_objects.specification.Disallows;
import me.miki.shindo.libs.spotify.model_objects.specification.Episode;
import me.miki.shindo.libs.spotify.model_objects.specification.Track;
@JsonDeserialize(builder = CurrentlyPlaying.Builder.class)
public class CurrentlyPlaying extends AbstractModelObject {
    private final Context context;
    private final Long timestamp;
    private final Integer progress_ms;
    private final Boolean is_playing;
    private final IPlaylistItem item;
    private final CurrentlyPlayingType currentlyPlayingType;
    private final Actions actions;

    private CurrentlyPlaying(final Builder builder) {
        super(builder);

        this.context = builder.context;
        this.timestamp = builder.timestamp;
        this.progress_ms = builder.progress_ms;
        this.is_playing = builder.is_playing;
        this.item = builder.item;
        this.currentlyPlayingType = builder.currentlyPlayingType;
        this.actions = builder.actions;
    }
    public Context getContext() {
        return context;
    }
    public Long getTimestamp() {
        return timestamp;
    }
    public Integer getProgress_ms() {
        return progress_ms;
    }
    public Boolean getIs_playing() {
        return is_playing;
    }
    public IPlaylistItem getItem() {
        return item;
    }
    public CurrentlyPlayingType getCurrentlyPlayingType() {
        return currentlyPlayingType;
    }
    public Actions getActions() {
        return actions;
    }

    @Override
    public String toString() {
        return "CurrentlyPlaying(context=" + context + ", timestamp=" + timestamp + ", progress_ms=" + progress_ms
                + ", is_playing=" + is_playing + ", item=" + item + ", currentlyPlayingType=" + currentlyPlayingType
                + ", actions=" + actions + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Context context;
        private Long timestamp;
        private Integer progress_ms;
        private Boolean is_playing;
        private IPlaylistItem item;
        private CurrentlyPlayingType currentlyPlayingType;
        private Actions actions;
        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }
        public Builder setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        public Builder setProgress_ms(Integer progress_ms) {
            this.progress_ms = progress_ms;
            return this;
        }
        public Builder setIs_playing(Boolean is_playing) {
            this.is_playing = is_playing;
            return this;
        }
        public Builder setItem(IPlaylistItem item) {
            this.item = item;
            return this;
        }
        public Builder setCurrentlyPlayingType(CurrentlyPlayingType currentlyPlayingType) {
            this.currentlyPlayingType = currentlyPlayingType;
            return this;
        }
        public Builder setActions(Actions actions) {
            this.actions = actions;
            return this;
        }

        @Override
        public CurrentlyPlaying build() {
            return new CurrentlyPlaying(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<CurrentlyPlaying> {
        public CurrentlyPlaying createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new CurrentlyPlaying.Builder()
                    .setContext(
                            hasAndNotNull(jsonObject, "context")
                                    ? new Context.JsonUtil().createModelObject(
                                    jsonObject.getAsJsonObject("context"))
                                    : null)
                    .setTimestamp(
                            hasAndNotNull(jsonObject, "timestamp")
                                    ? jsonObject.get("timestamp").getAsLong()
                                    : null)
                    .setProgress_ms(
                            hasAndNotNull(jsonObject, "progress_ms")
                                    ? jsonObject.get("progress_ms").getAsInt()
                                    : null)
                    .setIs_playing(
                            hasAndNotNull(jsonObject, "is_playing")
                                    ? jsonObject.get("is_playing").getAsBoolean()
                                    : null)
                    .setItem(
                            hasAndNotNull(jsonObject, "item") && hasAndNotNull(jsonObject, "currently_playing_type")
                                    ? (jsonObject.get("currently_playing_type").getAsString().equals("track")
                                    ? new Track.JsonUtil().createModelObject(jsonObject.getAsJsonObject("item"))
                                    : jsonObject.get("currently_playing_type").getAsString().equals("episode")
                                    ? new Episode.JsonUtil().createModelObject(jsonObject.getAsJsonObject("item"))
                                    : null)
                                    : null)
                    .setCurrentlyPlayingType(
                            hasAndNotNull(jsonObject, "currently_playing_type")
                                    ? CurrentlyPlayingType.keyOf(
                                    jsonObject.get("currently_playing_type").getAsString().toLowerCase())
                                    : null)
                    .setActions(
                            hasAndNotNull(jsonObject, "actions")
                                    ? new Actions.JsonUtil().createModelObject(
                                    jsonObject.getAsJsonObject("actions"))
                                    : null)
                    .build();
        }
    }
}
