package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.util.Arrays;
import java.util.Objects;
@JsonDeserialize(builder = TrackSimplified.Builder.class)
public class TrackSimplified extends AbstractModelObject {
    private final ArtistSimplified[] artists;
    private final CountryCode[] availableMarkets;
    private final Integer discNumber;
    private final Integer durationMs;
    private final Boolean explicit;
    private final ExternalUrl externalUrls;
    private final String href;
    private final String id;
    private final Boolean isPlayable;
    private final TrackLink linkedFrom;
    private final String name;
    private final String previewUrl;
    private final Integer trackNumber;
    private final ModelObjectType type;
    private final String uri;

    private TrackSimplified(final Builder builder) {
        super(builder);

        this.artists = builder.artists;
        this.availableMarkets = builder.availableMarkets;
        this.discNumber = builder.discNumber;
        this.durationMs = builder.durationMs;
        this.explicit = builder.explicit;
        this.externalUrls = builder.externalUrls;
        this.href = builder.href;
        this.id = builder.id;
        this.isPlayable = builder.isPlayable;
        this.linkedFrom = builder.linkedFrom;
        this.name = builder.name;
        this.previewUrl = builder.previewUrl;
        this.trackNumber = builder.trackNumber;
        this.type = builder.type;
        this.uri = builder.uri;
    }
    public ArtistSimplified[] getArtists() {
        return artists;
    }
    public CountryCode[] getAvailableMarkets() {
        return availableMarkets;
    }
    public Integer getDiscNumber() {
        return discNumber;
    }
    public Integer getDurationMs() {
        return durationMs;
    }
    public Boolean getIsExplicit() {
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
    public Boolean getIsPlayable() {
        return isPlayable;
    }
    public TrackLink getLinkedFrom() {
        return linkedFrom;
    }
    public String getName() {
        return name;
    }
    public String getPreviewUrl() {
        return previewUrl;
    }
    public Integer getTrackNumber() {
        return trackNumber;
    }
    public ModelObjectType getType() {
        return type;
    }
    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "TrackSimplified(name=" + name + ", artists=" + Arrays.toString(artists) + ", availableMarkets="
                + Arrays.toString(availableMarkets) + ", discNumber=" + discNumber + ", durationMs=" + durationMs
                + ", explicit=" + explicit + ", externalUrls=" + externalUrls + ", href=" + href + ", id=" + id
                + ", isPlayable=" + isPlayable + ", linkedFrom=" + linkedFrom + ", previewUrl=" + previewUrl + ", trackNumber="
                + trackNumber + ", type=" + type + ", uri=" + uri + ")";
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
        TrackSimplified track = (TrackSimplified) o;
        return Objects.equals(explicit, track.explicit) && Objects.equals(id, track.id) &&
                Objects.equals(name, track.name) && Objects.equals(uri, track.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(explicit, id, name, uri);
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private ArtistSimplified[] artists;
        private CountryCode[] availableMarkets;
        private Integer discNumber;
        private Integer durationMs;
        private Boolean explicit;
        private ExternalUrl externalUrls;
        private String href;
        private String id;
        private Boolean isPlayable;
        private TrackLink linkedFrom;
        private String name;
        private String previewUrl;
        private Integer trackNumber;
        private ModelObjectType type;
        private String uri;
        public Builder setArtists(ArtistSimplified... artists) {
            this.artists = artists;
            return this;
        }
        public Builder setAvailableMarkets(CountryCode... availableMarkets) {
            this.availableMarkets = availableMarkets;
            return this;
        }
        public Builder setDiscNumber(Integer discNumber) {
            this.discNumber = discNumber;
            return this;
        }
        public Builder setDurationMs(Integer durationMs) {
            this.durationMs = durationMs;
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
        public Builder setIsPlayable(Boolean isPlayable) {
            this.isPlayable = isPlayable;
            return this;
        }
        public Builder setLinkedFrom(TrackLink linkedFrom) {
            this.linkedFrom = linkedFrom;
            return this;
        }
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        public Builder setPreviewUrl(String previewUrl) {
            this.previewUrl = previewUrl;
            return this;
        }
        public Builder setTrackNumber(Integer trackNumber) {
            this.trackNumber = trackNumber;
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
        public TrackSimplified build() {
            return new TrackSimplified(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<TrackSimplified> {
        public TrackSimplified createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Builder()
                    .setArtists(
                            hasAndNotNull(jsonObject, "artists")
                                    ? new ArtistSimplified.JsonUtil().createModelObjectArray(
                                    jsonObject.getAsJsonArray("artists"))
                                    : null)
                    .setAvailableMarkets(
                            hasAndNotNull(jsonObject, "available_markets")
                                    ? new Gson().fromJson(jsonObject.getAsJsonArray(
                                    "available_markets"), CountryCode[].class)
                                    : null)
                    .setDiscNumber(
                            hasAndNotNull(jsonObject, "disc_number")
                                    ? jsonObject.get("disc_number").getAsInt()
                                    : null)
                    .setDurationMs(
                            hasAndNotNull(jsonObject, "duration_ms")
                                    ? jsonObject.get("duration_ms").getAsInt()
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
                    .setIsPlayable(
                            hasAndNotNull(jsonObject, "is_playable")
                                    ? jsonObject.get("is_playable").getAsBoolean()
                                    : null)
                    .setLinkedFrom(
                            hasAndNotNull(jsonObject, "linked_from")
                                    ? new TrackLink.JsonUtil().createModelObject(
                                    jsonObject.get("linked_from").getAsJsonObject())
                                    : null)
                    .setName(
                            hasAndNotNull(jsonObject, "name")
                                    ? jsonObject.get("name").getAsString()
                                    : null)
                    .setPreviewUrl(
                            hasAndNotNull(jsonObject, "preview_url")
                                    ? jsonObject.get("preview_url").getAsString()
                                    : null)
                    .setTrackNumber(
                            hasAndNotNull(jsonObject, "track_number")
                                    ? jsonObject.get("track_number").getAsInt()
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
