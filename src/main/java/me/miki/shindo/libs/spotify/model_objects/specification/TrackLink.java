package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
@JsonDeserialize(builder = TrackLink.Builder.class)
public class TrackLink extends AbstractModelObject {
    private final ExternalUrl externalUrls;
    private final String href;
    private final String id;
    private final ModelObjectType type;
    private final String uri;

    private TrackLink(final Builder builder) {
        super(builder);

        this.externalUrls = builder.externalUrls;
        this.href = builder.href;
        this.id = builder.id;
        this.type = builder.type;
        this.uri = builder.uri;
    }
    public ExternalUrl getExternalUrls() {
        return externalUrls;
    }
    public String getHref() {
        return href;
    }
    public String getId() {
        return id;
    }
    public ModelObjectType getType() {
        return type;
    }
    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "TrackLink(externalUrls=" + externalUrls + ", href=" + href + ", id=" + id + ", type=" + type + ", uri="
                + uri + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private ExternalUrl externalUrls;
        private String href;
        private String id;
        private ModelObjectType type;
        private String uri;
        public Builder setExternalUrls(ExternalUrl externalUrls) {
            this.externalUrls = externalUrls;
            return this;
        }
        public Builder setHref(String href) {
            this.href = href;
            return this;
        }
        public Builder setId(String id) {
            this.id = id;
            return this;
        }
        public Builder setType(ModelObjectType type) {
            this.type = type;
            return this;
        }
        public Builder setUri(String uri) {
            this.uri = uri;
            return this;
        }

        @Override
        public TrackLink build() {
            return new TrackLink(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<TrackLink> {
        public TrackLink createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new TrackLink.Builder()
                    .setExternalUrls(
                            hasAndNotNull(jsonObject, "external_urls")
                                    ? new ExternalUrl.JsonUtil().createModelObject(
                                    jsonObject.getAsJsonObject("external_urls"))
                                    : null)
                    .setHref(
                            hasAndNotNull(jsonObject, "href")
                                    ? jsonObject.get("href").getAsString()
                                    : null)
                    .setId(
                            hasAndNotNull(jsonObject, "id")
                                    ? jsonObject.get("id").getAsString()
                                    : null)
                    .setType(
                            hasAndNotNull(jsonObject, "type")
                                    ? ModelObjectType.keyOf(
                                    jsonObject.get("type").getAsString().toLowerCase())
                                    : null)
                    .setUri(
                            hasAndNotNull(jsonObject, "uri")
                                    ? jsonObject.get("uri").getAsString()
                                    : null)
                    .build();
        }
    }
}
