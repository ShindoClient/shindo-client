package me.miki.shindo.libs.spotify.model_objects.special;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.util.Arrays;
import java.util.Objects;
@JsonDeserialize(builder = PlaylistTrackPosition.Builder.class)
public class PlaylistTrackPosition extends AbstractModelObject {
    private final String uri;
    private final int[] positions;

    public PlaylistTrackPosition(final Builder builder) {
        super(builder);

        this.uri = builder.uri;
        this.positions = builder.positions;
    }
    public String getUri() {
        return uri;
    }
    public int[] getPositions() {
        return positions;
    }

    @Override
    public String toString() {
        return "PlaylistTrackPosition(uri=" + uri + ", positions=" + Arrays.toString(positions) + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlaylistTrackPosition that = (PlaylistTrackPosition) o;
        return Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private String uri;
        private int[] positions;

        public Builder setUri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder setPositions(int... positions) {
            this.positions = positions;
            return this;
        }

        @Override
        public PlaylistTrackPosition build() {
            return new PlaylistTrackPosition(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<PlaylistTrackPosition> {
        public PlaylistTrackPosition createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new PlaylistTrackPosition.Builder()
                    .setPositions(
                            hasAndNotNull(jsonObject, "positions")
                                    ? new Gson().fromJson(
                                    jsonObject.getAsJsonArray("positions"), int[].class)
                                    : null)
                    .setUri(
                            hasAndNotNull(jsonObject, "uri")
                                    ? jsonObject.get("uri").getAsString()
                                    : null)
                    .build();
        }
    }
}
