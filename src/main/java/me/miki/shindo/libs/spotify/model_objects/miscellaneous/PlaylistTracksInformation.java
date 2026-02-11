package me.miki.shindo.libs.spotify.model_objects.miscellaneous;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
@JsonDeserialize(builder = PlaylistTracksInformation.Builder.class)
public class PlaylistTracksInformation extends AbstractModelObject {
    private final String href;
    private final Integer total;

    private PlaylistTracksInformation(final Builder builder) {
        super(builder);

        this.href = builder.href;
        this.total = builder.total;
    }
    public String getHref() {
        return href;
    }
    public Integer getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "PlaylistTracksInformation(href=" + href + ", total=" + total + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private String href;
        private Integer total;
        public Builder setHref(String href) {
            this.href = href;
            return this;
        }
        public Builder setTotal(Integer total) {
            this.total = total;
            return this;
        }

        @Override
        public PlaylistTracksInformation build() {
            return new PlaylistTracksInformation(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<PlaylistTracksInformation> {
        public PlaylistTracksInformation createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new PlaylistTracksInformation.Builder()
                    .setHref(
                            hasAndNotNull(jsonObject, "href")
                                    ? jsonObject.get("href").getAsString()
                                    : null)
                    .setTotal(
                            hasAndNotNull(jsonObject, "total")
                                    ? jsonObject.get("total").getAsInt()
                                    : null)
                    .build();
        }
    }
}
