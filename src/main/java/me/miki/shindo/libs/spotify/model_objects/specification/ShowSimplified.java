package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
import me.miki.shindo.libs.spotify.requests.data.search.interfaces.ISearchModelObject;

import java.util.Arrays;
import java.util.Objects;
@JsonDeserialize(builder = ShowSimplified.Builder.class)
public class ShowSimplified extends AbstractModelObject implements ISearchModelObject {
    private final CountryCode[] availableMarkets;
    private final Copyright[] copyrights;
    private final String description;
    private final Boolean explicit;
    private final ExternalUrl externalUrls;
    private final String href;
    private final String id;
    private final Image[] images;
    private final Boolean isExternallyHosted;
    private final String[] languages;
    private final String mediaType;
    private final String name;
    private final String publisher;
    private final ModelObjectType type;
    private final String uri;

    public ShowSimplified(Builder builder) {
        super(builder);
        this.availableMarkets = builder.availableMarkets;
        this.copyrights = builder.copyrights;
        this.description = builder.description;
        this.explicit = builder.explicit;
        this.externalUrls = builder.externalUrls;
        this.href = builder.href;
        this.id = builder.id;
        this.images = builder.images;
        this.isExternallyHosted = builder.isExternallyHosted;
        this.languages = builder.languages;
        this.mediaType = builder.mediaType;
        this.name = builder.name;
        this.publisher = builder.publisher;
        this.type = builder.type;
        this.uri = builder.uri;
    }
    public CountryCode[] getAvailableMarkets() {
        return availableMarkets;
    }
    public Copyright[] getCopyrights() {
        return copyrights;
    }
    public String getDescription() {
        return description;
    }
    public Boolean getExplicit() {
        return explicit;
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
    public Image[] getImages() {
        return images;
    }
    public Boolean getExternallyHosted() {
        return isExternallyHosted;
    }
    public String[] getLanguages() {
        return languages;
    }
    public String getMediaType() {
        return mediaType;
    }
    public String getName() {
        return name;
    }
    public String getPublisher() {
        return publisher;
    }
    public ModelObjectType getType() {
        return type;
    }
    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "ShowSimplified(availableMarkets=" + Arrays.toString(availableMarkets) + ", copyrights="
                + Arrays.toString(copyrights) + ", description=" + description + ", explicit=" + explicit + ", externalUrls="
                + externalUrls + ", href=" + href + ", id=" + id + ", images=" + Arrays.toString(images)
                + ", isExternallyHosted=" + isExternallyHosted + ", languages=" + Arrays.toString(languages) + ", mediaType="
                + mediaType + ", name=" + name + ", publisher=" + publisher + ", type=" + type + ", uri=" + uri + ")";
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
        ShowSimplified show = (ShowSimplified) o;
        return Objects.equals(id, show.id) && Objects.equals(name, show.name) && Objects.equals(explicit, show.explicit) &&
                Objects.equals(uri, show.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, explicit, uri);
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private CountryCode[] availableMarkets;
        private Copyright[] copyrights;
        private String description;
        private Boolean explicit;
        private ExternalUrl externalUrls;
        private String href;
        private String id;
        private Image[] images;
        private Boolean isExternallyHosted;
        private String[] languages;
        private String mediaType;
        private String name;
        private String publisher;
        private ModelObjectType type;
        private String uri;
        public Builder setAvailableMarkets(CountryCode... availableMarkets) {
            this.availableMarkets = availableMarkets;
            return this;
        }
        public Builder setCopyrights(Copyright... copyrights) {
            this.copyrights = copyrights;
            return this;
        }
        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }
        public Builder setExplicit(Boolean explicit) {
            this.explicit = explicit;
            return this;
        }
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
        public Builder setImages(Image... images) {
            this.images = images;
            return this;
        }
        public Builder setExternallyHosted(Boolean externallyHosted) {
            isExternallyHosted = externallyHosted;
            return this;
        }
        public Builder setLanguages(String[] languages) {
            this.languages = languages;
            return this;
        }
        public Builder setMediaType(String mediaType) {
            this.mediaType = mediaType;
            return this;
        }
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        public Builder setPublisher(String publisher) {
            this.publisher = publisher;
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
        public ShowSimplified build() {
            return new ShowSimplified(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<ShowSimplified> {
        @Override
        public ShowSimplified createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Builder()
                    .setAvailableMarkets(
                            hasAndNotNull(jsonObject, "available_markets")
                                    ? new Gson().fromJson(
                                    jsonObject.getAsJsonArray("available_markets"), CountryCode[].class)
                                    : null)
                    .setCopyrights(
                            hasAndNotNull(jsonObject, "copyrights")
                                    ? new Gson().fromJson(
                                    jsonObject.getAsJsonArray("copyrights"), Copyright[].class)
                                    : null)
                    .setDescription(
                            hasAndNotNull(jsonObject, "description")
                                    ? jsonObject.get("description").getAsString()
                                    : null)
                    .setExplicit(
                            hasAndNotNull(jsonObject, "explicit")
                                    ? jsonObject.get("explicit").getAsBoolean()
                                    : null)
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
                    .setImages(
                            hasAndNotNull(jsonObject, "images")
                                    ? new Image.JsonUtil().createModelObjectArray(
                                    jsonObject.getAsJsonArray("images"))
                                    : null)
                    .setExternallyHosted(
                            hasAndNotNull(jsonObject, "is_externally_hosted")
                                    ? jsonObject.get("is_externally_hosted").getAsBoolean()
                                    : null)
                    .setLanguages(
                            hasAndNotNull(jsonObject, "languages")
                                    ? new Gson().fromJson(
                                    jsonObject.getAsJsonArray("languages"), String[].class)
                                    : null)
                    .setMediaType(
                            hasAndNotNull(jsonObject, "media_type")
                                    ? jsonObject.get("media_type").getAsString()
                                    : null)
                    .setName(
                            hasAndNotNull(jsonObject, "name")
                                    ? jsonObject.get("name").getAsString()
                                    : null)
                    .setPublisher(
                            hasAndNotNull(jsonObject, "publisher")
                                    ? jsonObject.get("publisher").getAsString()
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
