package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
@JsonDeserialize(builder = Image.Builder.class)
public class Image extends AbstractModelObject {
    private final Integer height;
    private final String url;
    private final Integer width;

    private Image(final Builder builder) {
        super(builder);

        this.height = builder.height;
        this.url = builder.url;
        this.width = builder.width;
    }
    public Integer getHeight() {
        return height;
    }
    public String getUrl() {
        return url;
    }
    public Integer getWidth() {
        return width;
    }

    @Override
    public String toString() {
        return "Image(height=" + height + ", url=" + url + ", width=" + width + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Integer height;
        private String url;
        private Integer width;
        public Builder setHeight(Integer height) {
            this.height = height;
            return this;
        }
        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }
        public Builder setWidth(Integer width) {
            this.width = width;
            return this;
        }

        @Override
        public Image build() {
            return new Image(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<Image> {
        public Image createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Image.Builder()
                    .setHeight(
                            hasAndNotNull(jsonObject, "height")
                                    ? jsonObject.get("height").getAsInt()
                                    : null)
                    .setUrl(
                            hasAndNotNull(jsonObject, "url")
                                    ? jsonObject.get("url").getAsString()
                                    : null)
                    .setWidth(
                            hasAndNotNull(jsonObject, "width")
                                    ? jsonObject.get("width").getAsInt()
                                    : null)
                    .build();
        }
    }
}
