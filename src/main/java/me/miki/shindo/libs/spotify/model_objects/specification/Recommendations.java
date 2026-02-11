package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.util.Arrays;
@JsonDeserialize(builder = Recommendations.Builder.class)
public class Recommendations extends AbstractModelObject {
    private final RecommendationsSeed[] seeds;
    private final Track[] tracks;

    private Recommendations(final Builder builder) {
        super(builder);

        this.seeds = builder.seeds;
        this.tracks = builder.tracks;
    }
    public RecommendationsSeed[] getSeeds() {
        return seeds;
    }
    public Track[] getTracks() {
        return tracks;
    }

    @Override
    public String toString() {
        return "Recommendations(seeds=" + Arrays.toString(seeds) + ", tracks=" + Arrays.toString(tracks) + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private RecommendationsSeed[] seeds;
        private Track[] tracks;
        public Builder setSeeds(RecommendationsSeed... seeds) {
            this.seeds = seeds;
            return this;
        }
        public Builder setTracks(Track... tracks) {
            this.tracks = tracks;
            return this;
        }

        @Override
        public Recommendations build() {
            return new Recommendations(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<Recommendations> {
        public Recommendations createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Recommendations.Builder()
                    .setSeeds(
                            hasAndNotNull(jsonObject, "seeds")
                                    ? new RecommendationsSeed.JsonUtil().createModelObjectArray(
                                    jsonObject.getAsJsonArray("seeds"))
                                    : null)
                    .setTracks(
                            hasAndNotNull(jsonObject, "tracks")
                                    ? new Track.JsonUtil().createModelObjectArray(
                                    jsonObject.getAsJsonArray("tracks"))
                                    : null)
                    .build();
        }
    }
}
