package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
import me.miki.shindo.libs.spotify.requests.data.personalization.interfaces.IArtistTrackModelObject;
import me.miki.shindo.libs.spotify.requests.data.search.interfaces.ISearchModelObject;

import java.util.Arrays;
import java.util.Objects;
@JsonDeserialize(builder = Artist.Builder.class)
public class Artist extends AbstractModelObject implements IArtistTrackModelObject, ISearchModelObject {
    private final ExternalUrl externalUrls;
    private final Followers followers;
    private final String[] genres;
    private final String href;
    private final String id;
    private final Image[] images;
    private final String name;
    private final Integer popularity;
    private final ModelObjectType type;
    private final String uri;

    private Artist(final Builder builder) {
        super(builder);

        this.externalUrls = builder.externalUrls;
        this.followers = builder.followers;
        this.genres = builder.genres;
        this.href = builder.href;
        this.id = builder.id;
        this.images = builder.images;
        this.name = builder.name;
        this.popularity = builder.popularity;
        this.type = builder.type;
        this.uri = builder.uri;
    }
    public ExternalUrl getExternalUrls() {
        return externalUrls;
    }
    public Followers getFollowers() {
        return followers;
    }
    public String[] getGenres() {
        return genres;
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
    public String getName() {
        return name;
    }
    public Integer getPopularity() {
        return popularity;
    }
    public ModelObjectType getType() {
        return type;
    }
    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "Artist(name=" + name + ", externalUrls=" + externalUrls + ", followers=" + followers + ", genres="
                + Arrays.toString(genres) + ", href=" + href + ", id=" + id + ", images=" + Arrays.toString(images)
                + ", popularity=" + popularity + ", type=" + type + ", uri=" + uri + ")";
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
        Artist artist = (Artist) o;
        return Objects.equals(id, artist.id) && Objects.equals(name, artist.name) && Objects.equals(uri, artist.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, uri);
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private ExternalUrl externalUrls;
        private Followers followers;
        private String[] genres;
        private String href;
        private String id;
        private Image[] images;
        private String name;
        private Integer popularity;
        private ModelObjectType type;
        private String uri;
        public Builder setExternalUrls(ExternalUrl externalUrls) {
            this.externalUrls = externalUrls;
            return this;
        }
        public Builder setFollowers(Followers followers) {
            this.followers = followers;
            return this;
        }
        public Builder setGenres(String... genres) {
            this.genres = genres;
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
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        public Builder setPopularity(Integer popularity) {
            this.popularity = popularity;
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
        public Artist build() {
            return new Artist(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<Artist> {
        public Artist createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Artist.Builder()
                    .setExternalUrls(
                            hasAndNotNull(jsonObject, "external_urls")
                                    ? new ExternalUrl.JsonUtil().createModelObject(
                                    jsonObject.getAsJsonObject("external_urls"))
                                    : null)
                    .setFollowers(
                            hasAndNotNull(jsonObject, "followers")
                                    ? new Followers.JsonUtil().createModelObject(
                                    jsonObject.getAsJsonObject("followers"))
                                    : null)
                    .setGenres(
                            hasAndNotNull(jsonObject, "genres")
                                    ? new Gson().fromJson(
                                    jsonObject.getAsJsonArray("genres"), String[].class)
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
                    .setName(
                            hasAndNotNull(jsonObject, "name")
                                    ? jsonObject.get("name").getAsString()
                                    : null)
                    .setPopularity(
                            hasAndNotNull(jsonObject, "popularity")
                                    ? jsonObject.get("popularity").getAsInt()
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
