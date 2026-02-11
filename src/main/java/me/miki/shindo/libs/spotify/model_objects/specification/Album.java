package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.enums.AlbumType;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.enums.ReleaseDatePrecision;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.util.Arrays;
import java.util.Objects;
@JsonDeserialize(builder = Album.Builder.class)
public class Album extends AbstractModelObject {
    private final AlbumType albumType;
    private final ArtistSimplified[] artists;
    private final CountryCode[] availableMarkets;
    private final Copyright[] copyrights;
    private final ExternalId externalIds;
    private final ExternalUrl externalUrls;
    private final String[] genres;
    private final String href;
    private final String id;
    private final Image[] images;
    private final String label;
    private final String name;
    private final Integer popularity;
    private final String releaseDate;
    private final ReleaseDatePrecision releaseDatePrecision;
    private final Paging<TrackSimplified> tracks;
    private final ModelObjectType type;
    private final String uri;

    private Album(final Builder builder) {
        super(builder);

        this.albumType = builder.albumType;
        this.artists = builder.artists;
        this.availableMarkets = builder.availableMarkets;
        this.copyrights = builder.copyrights;
        this.externalIds = builder.externalIds;
        this.externalUrls = builder.externalUrls;
        this.genres = builder.genres;
        this.href = builder.href;
        this.id = builder.id;
        this.images = builder.images;
        this.label = builder.label;
        this.name = builder.name;
        this.popularity = builder.popularity;
        this.releaseDate = builder.releaseDate;
        this.releaseDatePrecision = builder.releaseDatePrecision;
        this.tracks = builder.tracks;
        this.type = builder.type;
        this.uri = builder.uri;
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
    public Copyright[] getCopyrights() {
        return copyrights;
    }
    public ExternalId getExternalIds() {
        return externalIds;
    }
    public ExternalUrl getExternalUrls() {
        return externalUrls;
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
    public String getLabel() {
        return label;
    }
    public String getName() {
        return name;
    }
    public Integer getPopularity() {
        return popularity;
    }
    public String getReleaseDate() {
        return releaseDate;
    }
    public ReleaseDatePrecision getReleaseDatePrecision() {
        return releaseDatePrecision;
    }
    public Paging<TrackSimplified> getTracks() {
        return tracks;
    }
    public ModelObjectType getType() {
        return type;
    }
    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "Album(artists=" + Arrays.toString(artists) + ", name=" + name + ", albumType=" + albumType
                + ", availableMarkets=" + Arrays.toString(availableMarkets) + ", copyrights=" + Arrays.toString(copyrights)
                + ", externalIds=" + externalIds + ", externalUrls=" + externalUrls + ", genres=" + Arrays.toString(genres)
                + ", href=" + href + ", id=" + id + ", images=" + Arrays.toString(images) + ", label=" + label + ", popularity="
                + popularity + ", releaseDate=" + releaseDate + ", releaseDatePrecision=" + releaseDatePrecision + ", tracks="
                + tracks + ", type=" + type + ", uri=" + uri + ")";
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
        Album album = (Album) o;
        return Objects.equals(id, album.id) && Objects.equals(label, album.label) && Objects.equals(name, album.name) &&
                Objects.equals(releaseDate, album.releaseDate) && Objects.equals(uri, album.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, name, releaseDate, uri);
    }
    public static final class Builder extends AbstractModelObject.Builder {

        private AlbumType albumType;
        private ArtistSimplified[] artists;
        private CountryCode[] availableMarkets;
        private Copyright[] copyrights;
        private ExternalId externalIds;
        private ExternalUrl externalUrls;
        private String[] genres;
        private String href;
        private String id;
        private Image[] images;
        private String label;
        private String name;
        private Integer popularity;
        private String releaseDate;
        private ReleaseDatePrecision releaseDatePrecision;
        private Paging<TrackSimplified> tracks;
        private ModelObjectType type;
        private String uri;
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
        public Builder setCopyrights(Copyright... copyrights) {
            this.copyrights = copyrights;
            return this;
        }
        public Builder setExternalIds(ExternalId externalIds) {
            this.externalIds = externalIds;
            return this;
        }
        public Builder setExternalUrls(ExternalUrl externalUrls) {
            this.externalUrls = externalUrls;
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
        public Builder setLabel(String label) {
            this.label = label;
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
        public Builder setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }
        public Builder setReleaseDatePrecision(ReleaseDatePrecision releaseDatePrecision) {
            this.releaseDatePrecision = releaseDatePrecision;
            return this;
        }
        public Builder setTracks(Paging<TrackSimplified> tracks) {
            this.tracks = tracks;
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
        public Album build() {
            return new Album(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<Album> {
        public Album createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Album.Builder()
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
                                    jsonObject.getAsJsonArray("available_markets"), CountryCode[].class)
                                    : null)
                    .setCopyrights(
                            hasAndNotNull(jsonObject, "copyrights")
                                    ? new Copyright.JsonUtil().createModelObjectArray(
                                    jsonObject.getAsJsonArray("copyrights"))
                                    : null)
                    .setExternalIds(
                            hasAndNotNull(jsonObject, "external_ids")
                                    ? new ExternalId.JsonUtil().createModelObject(
                                    jsonObject.getAsJsonObject("external_ids"))
                                    : null)
                    .setExternalUrls(
                            hasAndNotNull(jsonObject, "external_urls")
                                    ? new ExternalUrl.JsonUtil().createModelObject(
                                    jsonObject.getAsJsonObject("external_urls"))
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
                    .setLabel(
                            hasAndNotNull(jsonObject, "label")
                                    ? jsonObject.get("label").getAsString()
                                    : null)
                    .setName(
                            hasAndNotNull(jsonObject, "name")
                                    ? jsonObject.get("name").getAsString()
                                    : null)
                    .setPopularity(
                            hasAndNotNull(jsonObject, "popularity")
                                    ? jsonObject.get("popularity").getAsInt()
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
                    .setTracks(
                            hasAndNotNull(jsonObject, "tracks")
                                    ? new TrackSimplified.JsonUtil().createModelObjectPaging(
                                    jsonObject.getAsJsonObject("tracks"))
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
