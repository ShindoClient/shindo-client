package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.util.Objects;
@JsonDeserialize(builder = ArtistSimplified.Builder.class)
public class ArtistSimplified extends AbstractModelObject {
    private final ExternalUrl externalUrls;
    private final String href;
    private final String id;
    private final String name;
    private final ModelObjectType type;
    private final String uri;

    private ArtistSimplified(final Builder builder) {
        super(builder);

        this.externalUrls = builder.externalUrls;
        this.href = builder.href;
        this.id = builder.id;
        this.name = builder.name;
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
    public String getName() {
        return name;
    }
    public ModelObjectType getType() {
        return type;
    }
    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "ArtistSimplified(name=" + name + ", externalUrls=" + externalUrls + ", href=" + href + ", id=" + id
                + ", type=" + type + ", uri=" + uri + ")";
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
        ArtistSimplified artist = (ArtistSimplified) o;
        return Objects.equals(id, artist.id) && Objects.equals(name, artist.name) && Objects.equals(uri, artist.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, uri);
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private ExternalUrl externalUrls;
        private String href;
        private String id;
        private String name;
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
        public Builder setName(String name) {
            this.name = name;
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
        public ArtistSimplified build() {
            return new ArtistSimplified(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<ArtistSimplified> {
        public ArtistSimplified createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new ArtistSimplified.Builder()
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
                    .setName(
                            hasAndNotNull(jsonObject, "name")
                                    ? jsonObject.get("name").getAsString()
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
