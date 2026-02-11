package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.util.Objects;
@JsonDeserialize(builder = Context.Builder.class)
public class Context extends AbstractModelObject {
    private final ModelObjectType type;
    private final String href;
    private final ExternalUrl externalUrls;
    private final String uri;

    private Context(final Builder builder) {
        super(builder);

        this.type = builder.type;
        this.href = builder.href;
        this.externalUrls = builder.externalUrls;
        this.uri = builder.uri;
    }
    public ModelObjectType getType() {
        return type;
    }
    public String getHref() {
        return href;
    }
    public ExternalUrl getExternalUrls() {
        return externalUrls;
    }
    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "Context(type=" + type + ", href=" + href + ", externalUrls=" + externalUrls + ", uri=" + uri + ")";
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
        Context context = (Context) o;
        return Objects.equals(uri, context.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private ModelObjectType type;
        private String href;
        private ExternalUrl externalUrls;
        private String uri;
        public Builder setType(ModelObjectType type) {
            this.type = type;
            return this;
        }
        public Builder setHref(String href) {
            this.href = href;
            return this;
        }
        public Builder setExternalUrls(ExternalUrl externalUrls) {
            this.externalUrls = externalUrls;
            return this;
        }
        public Builder setUri(String uri) {
            this.uri = uri;
            return this;
        }

        @Override
        public Context build() {
            return new Context(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<Context> {
        public Context createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Context.Builder()
                    .setType(
                            hasAndNotNull(jsonObject, "type")
                                    ? ModelObjectType.keyOf(
                                    jsonObject.get("type").getAsString().toLowerCase())
                                    : null)
                    .setHref(
                            hasAndNotNull(jsonObject, "href")
                                    ? jsonObject.get("href").getAsString()
                                    : null)
                    .setExternalUrls(
                            hasAndNotNull(jsonObject, "external_urls")
                                    ? new ExternalUrl.JsonUtil().createModelObject(
                                    jsonObject.getAsJsonObject("external_urls"))
                                    : null)
                    .setUri(
                            hasAndNotNull(jsonObject, "uri")
                                    ? jsonObject.get("uri").getAsString()
                                    : null)
                    .build();
        }
    }
}
