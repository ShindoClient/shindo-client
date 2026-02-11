package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.enums.ReleaseDatePrecision;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
import me.miki.shindo.libs.spotify.model_objects.IPlaylistItem;

import java.util.Arrays;
import java.util.Objects;
@JsonDeserialize(builder = Episode.Builder.class)
public class Episode extends AbstractModelObject implements IPlaylistItem {
    private final String audioPreviewUrl;
    private final String description;
    private final Integer durationMs;
    private final Boolean explicit;
    private final ExternalUrl externalUrls;
    private final String href;
    private final String id;
    private final Image[] images;
    private final Boolean isExternallyHosted;
    private final Boolean isPlayable;
    private final String[] languages;
    private final String name;
    private final String releaseDate;
    private final ReleaseDatePrecision releaseDatePrecision;
    private final ResumePoint resumePoint;
    private final ShowSimplified show;
    private final ModelObjectType type;
    private final String uri;

    private Episode(final Builder builder) {
        super(builder);
        this.audioPreviewUrl = builder.audioPreviewUrl;
        this.description = builder.description;
        this.durationMs = builder.durationMs;
        this.explicit = builder.explicit;
        this.externalUrls = builder.externalUrls;
        this.href = builder.href;
        this.id = builder.id;
        this.images = builder.images;
        this.isExternallyHosted = builder.isExternallyHosted;
        this.isPlayable = builder.isPlayable;
        this.languages = builder.languages;
        this.name = builder.name;
        this.releaseDate = builder.releaseDate;
        this.releaseDatePrecision = builder.releaseDatePrecision;
        this.resumePoint = builder.resumePoint;
        this.show = builder.show;
        this.type = builder.type;
        this.uri = builder.uri;
    }
    public String getAudioPreviewUrl() {
        return audioPreviewUrl;
    }
    public String getDescription() {
        return description;
    }
    @Override
    public Integer getDurationMs() {
        return durationMs;
    }
    public Boolean getExplicit() {
        return explicit;
    }
    @Override
    public ExternalUrl getExternalUrls() {
        return externalUrls;
    }
    @Override
    public String getHref() {
        return href;
    }
    @Override
    public String getId() {
        return id;
    }
    public Image[] getImages() {
        return images;
    }
    public Boolean getExternallyHosted() {
        return isExternallyHosted;
    }
    public Boolean getPlayable() {
        return isPlayable;
    }
    public String[] getLanguages() {
        return languages;
    }
    @Override
    public String getName() {
        return name;
    }
    public String getReleaseDate() {
        return releaseDate;
    }
    public ReleaseDatePrecision getReleaseDatePrecision() {
        return releaseDatePrecision;
    }
    public ResumePoint getResumePoint() {
        return resumePoint;
    }
    public ShowSimplified getShow() {
        return show;
    }
    @Override
    public ModelObjectType getType() {
        return type;
    }
    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "Episode(name=" + name + ", description=" + description + ", show=" + show + ", audioPreviewUrl="
                + audioPreviewUrl + ", durationMs=" + durationMs + ", explicit=" + explicit + ", externalUrls=" + externalUrls
                + ", href=" + href + ", id=" + id + ", images=" + Arrays.toString(images) + ", isExternallyHosted="
                + isExternallyHosted + ", isPlayable=" + isPlayable + ", languages=" + Arrays.toString(languages)
                + ", releaseDate=" + releaseDate + ", releaseDatePrecision=" + releaseDatePrecision + ", resumePoint="
                + resumePoint + ", type=" + type + ", uri=" + uri + ")";
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
        Episode episode = (Episode) o;
        return Objects.equals(id, episode.id) && Objects.equals(name, episode.name) &&
                Objects.equals(releaseDate, episode.releaseDate) && Objects.equals(explicit, episode.explicit) &&
                Objects.equals(uri, episode.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, releaseDate, explicit, uri);
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private String audioPreviewUrl;
        private String description;
        private Integer durationMs;
        private Boolean explicit;
        private ExternalUrl externalUrls;
        private String href;
        private String id;
        private Image[] images;
        private Boolean isExternallyHosted;
        private Boolean isPlayable;
        private String[] languages;
        private String name;
        private String releaseDate;
        private ReleaseDatePrecision releaseDatePrecision;
        private ResumePoint resumePoint;
        private ShowSimplified show;
        private ModelObjectType type;
        private String uri;
        public Builder setAudioPreviewUrl(String audioPreviewUrl) {
            this.audioPreviewUrl = audioPreviewUrl;
            return this;
        }
        public Builder setDescription(String description) {
            this.description = description;
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
        public Builder setImages(Image... images) {
            this.images = images;
            return this;
        }
        public Builder setExternallyHosted(Boolean externallyHosted) {
            isExternallyHosted = externallyHosted;
            return this;
        }
        public Builder setPlayable(Boolean playable) {
            isPlayable = playable;
            return this;
        }
        public Builder setLanguages(String... languages) {
            this.languages = languages;
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
        public Builder setResumePoint(ResumePoint resumePoint) {
            this.resumePoint = resumePoint;
            return this;
        }
        public Builder setShow(ShowSimplified show) {
            this.show = show;
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
        public Episode build() {
            return new Episode(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<Episode> {
        @Override
        public Episode createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Builder()
                    .setAudioPreviewUrl(
                            hasAndNotNull(jsonObject, "audio_preview_url")
                                    ? jsonObject.get("audio_preview_url").getAsString()
                                    : null)
                    .setDescription(
                            hasAndNotNull(jsonObject, "description")
                                    ? jsonObject.get("description").getAsString()
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
                    .setImages(
                            hasAndNotNull(jsonObject, "images")
                                    ? new Image.JsonUtil().createModelObjectArray(
                                    jsonObject.getAsJsonArray("images"))
                                    : null)
                    .setExternallyHosted(
                            hasAndNotNull(jsonObject, "is_externally_hosted")
                                    ? jsonObject.get("is_externally_hosted").getAsBoolean()
                                    : null)
                    .setPlayable(
                            hasAndNotNull(jsonObject, "is_playable")
                                    ? jsonObject.get("is_playable").getAsBoolean()
                                    : null)
                    .setLanguages(
                            hasAndNotNull(jsonObject, "languages")
                                    ? new Gson().fromJson(
                                    jsonObject.getAsJsonArray("languages"), String[].class)
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
                    .setResumePoint(
                            hasAndNotNull(jsonObject, "resume_point")
                                    ? new ResumePoint.JsonUtil().createModelObject(
                                    jsonObject.getAsJsonObject("resume_point"))
                                    : null)
                    .setShow(
                            hasAndNotNull(jsonObject, "show")
                                    ? new ShowSimplified.JsonUtil().createModelObject(
                                    jsonObject.getAsJsonObject("show"))
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
