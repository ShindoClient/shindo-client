package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
@JsonDeserialize(builder = ResumePoint.Builder.class)
public class ResumePoint extends AbstractModelObject {
    private final Boolean fullyPlayed;
    private final Integer resumePositionMs;

    private ResumePoint(final Builder builder) {
        super(builder);
        this.fullyPlayed = builder.fullyPlayed;
        this.resumePositionMs = builder.resumePositionMs;
    }
    public Boolean getFullyPlayed() {
        return fullyPlayed;
    }
    public Integer getResumePositionMs() {
        return resumePositionMs;
    }

    @Override
    public String toString() {
        return "ResumePoint(fullyPlayed=" + fullyPlayed + ", resumePositionMs=" + resumePositionMs + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Boolean fullyPlayed;
        private Integer resumePositionMs;
        public Builder setFullyPlayed(Boolean fullyPlayed) {
            this.fullyPlayed = fullyPlayed;
            return this;
        }
        public Builder setResumePositionMs(Integer resumePositionMs) {
            this.resumePositionMs = resumePositionMs;
            return this;
        }

        @Override
        public ResumePoint build() {
            return new ResumePoint(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<ResumePoint> {
        @Override
        public ResumePoint createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Builder()
                    .setFullyPlayed(
                            hasAndNotNull(jsonObject, "fully_played")
                                    ? jsonObject.get("fully_played").getAsBoolean()
                                    : null)
                    .setResumePositionMs(
                            hasAndNotNull(jsonObject, "resume_position_ms")
                                    ? jsonObject.get("resume_position_ms").getAsInt()
                                    : null)
                    .build();
        }
    }

}
