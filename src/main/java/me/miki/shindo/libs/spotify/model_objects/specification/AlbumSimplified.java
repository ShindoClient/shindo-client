package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.enums.AlbumGroup;
import me.miki.shindo.libs.spotify.enums.AlbumType;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.enums.ReleaseDatePrecision;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
import me.miki.shindo.libs.spotify.model_objects.miscellaneous.Restrictions;
import me.miki.shindo.libs.spotify.requests.data.search.interfaces.ISearchModelObject;

import java.util.Arrays;
import java.util.Objects;
@JsonDeserialize(builder = AlbumSimplified.Builder.class)
public class AlbumSimplified extends AbstractModelObject implements ISearchModelObject {
    private final AlbumGroup albumGroup;
    private final AlbumType albumType;
    private final ArtistSimplified[] artists;
    private final CountryCode[] availableMarkets;
    private final ExternalUrl externalUrls;
    private final String href;
    private final String id;
    private final Image[] images;
    private final String name;
    private final String releaseDate;
    private final ReleaseDatePrecision releaseDatePrecision;
    private final Restrictions restrictions;
    private final ModelObjectType type;
    private final String uri;

    private AlbumSimplified(final Builder builder) {
        super(builder);

        this.albumGroup = builder.albumGroup;
        this.albumType = builder.albumType;
        this.artists = builder.artists;
        this.availableMarkets = builder.availableMarkets;
        this.externalUrls = builder.externalUrls;
        this.href = builder.href;
        this.id = builder.id;
        this.images = builder.images;
        this.name = builder.name;
        this.releaseDate = builder.releaseDate;
        this.releaseDatePrecision = builder.releaseDatePrecision;
        this.restrictions = builder.restrictions;
        this.type = builder.type;
        this.uri = builder.uri;
    }
    public AlbumGroup getAlbumGroup() {
        return albumGroup;
    }
    public AlbumType getAlbumType() {
        return albumType;
    }
    public ArtistSimplified[] getArtists() {
        return artists;
    }
    public CountryCode[] getAvailableMarkets() {
        return availableMarkets;
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
    public String getName() {
        return name;
    }
    public String getReleaseDate() {
        return releaseDate;
    }
    public ReleaseDatePrecision getReleaseDatePrecision() {
        return releaseDatePrecision;
    }
    public Restrictions getRestrictions() {
        return restrictions;
    }
    public ModelObjectType getType() {
        return type;
    }
    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "AlbumSimplified(artists=" + Arrays.toString(artists) + ", name=" + name + ", albumGroup=" + albumGroup
                + ", albumType=" + albumType + ", availableMarkets=" + Arrays.toString(availableMarkets) + ", externalUrls="
                + externalUrls + ", href=" + href + ", id=" + id + ", images=" + Arrays.toString(images) + ", releaseDate="
                + releaseDate + ", releaseDatePrecision=" + releaseDatePrecision + ", restrictions=" + restrictions + ", type="
                + type + ", uri=" + uri + ")";
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
        AlbumSimplified album = (AlbumSimplified) o;
        return Objects.equals(id, album.id) && Objects.equals(name, album.name) &&
                Objects.equals(releaseDate, album.releaseDate) && Objects.equals(uri, album.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, releaseDate, uri);
    }
    public static final class Builder extends AbstractModelObject.Builder {

        private AlbumGroup albumGroup;
        private AlbumType albumType;
        private ArtistSimplified[] artists;
        private CountryCode[] availableMarkets;
        private ExternalUrl externalUrls;
        private String href;
        private String id;
        private Image[] images;
        private String name;
        private String releaseDate;
        private ReleaseDatePrecision releaseDatePrecision;
        private Restrictions restrictions;
        private ModelObjectType type;
        private String uri;
        public Builder setAlbumGroup(AlbumGroup albumGroup) {
            this.albumGroup = albumGroup;
            return this;
        }
        public Builder setAlbumType(AlbumType albumType) {
            this.albumType = albumType;
            return this;
        }
        public Builder setArtists(ArtistSimplified... artists) {
            this.artists = artists;
            return this;
        }
        public Builder setAvailableMarkets(CountryCode... availableMarkets) {
            this.availableMarkets = availableMarkets;
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
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        public Builder setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }
        public Builder setReleaseDatePrecision(ReleaseDatePrecision releaseDatePrecision) {
            this.releaseDatePrecision = releaseDatePrecision;
            return this;
        }
        public Builder setRestrictions(Restrictions restrictions) {
            this.restrictions = restrictions;
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
        public AlbumSimplified build() {
            return new AlbumSimplified(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<AlbumSimplified> {
        public AlbumSimplified createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new AlbumSimplified.Builder()
                    .setAlbumGroup(
                            hasAndNotNull(jsonObject, "album_group")
                                    ? AlbumGroup.keyOf(
                                    jsonObject.get("album_group").getAsString().toLowerCase())
                                    : null)
                    .setAlbumType(
                            hasAndNotNull(jsonObject, "album_type")
                                    ? AlbumType.keyOf(
                                    jsonObject.get("album_type").getAsString().toLowerCase())
                                    : null)
                    .setArtists(
                            hasAndNotNull(jsonObject, "artists")
                                    ? new ArtistSimplified.JsonUtil().createModelObjectArray(
                                    jsonObject.getAsJsonArray("artists"))
                                    : null)
                    .setAvailableMarkets(
                            hasAndNotNull(jsonObject, "available_markets")
                                    ? new Gson().fromJson(
                                    jsonObject.get("available_markets"), CountryCode[].class)
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
                    .setName(
                            hasAndNotNull(jsonObject, "name")
                                    ? jsonObject.get("name").getAsString()
                                    : null)
                    .setReleaseDate(
                            hasAndNotNull(jsonObject, "release_date")
                                    ? jsonObject.get("release_date").getAsString()
                                    : null)
                    .setReleaseDatePrecision(
                            hasAndNotNull(jsonObject, "release_date_precision")
                                    ? ReleaseDatePrecision.keyOf(
                                    jsonObject.get("release_date_precision").getAsString().toLowerCase())
                                    : null)
                    .setRestrictions(
                            hasAndNotNull(jsonObject, "restrictions")
                                    ? new Restrictions.JsonUtil().createModelObject(
                                    jsonObject.getAsJsonObject("restrictions"))
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
