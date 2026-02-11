package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
import me.miki.shindo.libs.spotify.model_objects.miscellaneous.PlaylistTracksInformation;
import me.miki.shindo.libs.spotify.requests.data.playlists.RemoveItemsFromPlaylistRequest;
import me.miki.shindo.libs.spotify.requests.data.search.interfaces.ISearchModelObject;

import java.util.Arrays;
import java.util.Objects;
@JsonDeserialize(builder = PlaylistSimplified.Builder.class)
public class PlaylistSimplified extends AbstractModelObject implements ISearchModelObject {
    private final Boolean collaborative;
    private final String description;
    private final ExternalUrl externalUrls;
    private final String href;
    private final String id;
    private final Image[] images;
    private final String name;
    private final User owner;
    private final Boolean publicAccess;
    private final String snapshotId;
    private final PlaylistTracksInformation tracks;
    private final ModelObjectType type;
    private final String uri;

    private PlaylistSimplified(final Builder builder) {
        super(builder);

        this.collaborative = builder.collaborative;
        this.description = builder.description;
        this.externalUrls = builder.externalUrls;
        this.href = builder.href;
        this.id = builder.id;
        this.images = builder.images;
        this.name = builder.name;
        this.owner = builder.owner;
        this.publicAccess = builder.publicAccess;
        this.snapshotId = builder.snapshotId;
        this.tracks = builder.tracks;
        this.type = builder.type;
        this.uri = builder.uri;
    }
    public Boolean getIsCollaborative() {
        return collaborative;
    }
    public String getDescription() {
        return description;
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
    public User getOwner() {
        return owner;
    }
    public Boolean getIsPublicAccess() {
        return publicAccess;
    }
    public String getSnapshotId() {
        return snapshotId;
    }
    public PlaylistTracksInformation getTracks() {
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
        return "PlaylistSimplified(name=" + name + ", tracks=" + tracks + ", collaborative=" + collaborative
                + ", description=" + description + ", externalUrls=" + externalUrls + ", href=" + href + ", id=" + id
                + ", images=" + Arrays.toString(images) + ", owner=" + owner + ", publicAccess=" + publicAccess
                + ", snapshotId=" + snapshotId + ", type=" + type + ", uri=" + uri + ")";
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
        PlaylistSimplified playlist = (PlaylistSimplified) o;
        return Objects.equals(id, playlist.id) && Objects.equals(name, playlist.name) && Objects.equals(uri, playlist.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, uri);
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Boolean collaborative;
        private String description;
        private ExternalUrl externalUrls;
        private String href;
        private String id;
        private Image[] images;
        private String name;
        private User owner;
        private Boolean publicAccess;
        private String snapshotId;
        private PlaylistTracksInformation tracks;
        private ModelObjectType type;
        private String uri;
        public Builder setCollaborative(Boolean collaborative) {
            this.collaborative = collaborative;
            return this;
        }
        public Builder setDescription(String description) {
            this.description = description;
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
        public Builder setOwner(User owner) {
            this.owner = owner;
            return this;
        }
        public Builder setPublicAccess(Boolean publicAccess) {
            this.publicAccess = publicAccess;
            return this;
        }
        public Builder setSnapshotId(String snapshotId) {
            this.snapshotId = snapshotId;
            return this;
        }
        public Builder setTracks(PlaylistTracksInformation tracks) {
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
        public PlaylistSimplified build() {
            return new PlaylistSimplified(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<PlaylistSimplified> {
        public PlaylistSimplified createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new PlaylistSimplified.Builder()
                    .setCollaborative(
                            hasAndNotNull(jsonObject, "collaborative")
                                    ? jsonObject.get("collaborative").getAsBoolean()
                                    : null)
                    .setDescription(
                            hasAndNotNull(jsonObject, "description")
                                    ? jsonObject.get("description").getAsString()
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
                    .setOwner(
                            hasAndNotNull(jsonObject, "owner")
                                    ? new User.JsonUtil().createModelObject(
                                    jsonObject.getAsJsonObject("owner"))
                                    : null)
                    .setPublicAccess(
                            hasAndNotNull(jsonObject, "public")
                                    ? jsonObject.get("public").getAsBoolean()
                                    : null)
                    .setSnapshotId(
                            hasAndNotNull(jsonObject, "snapshot_id")
                                    ? jsonObject.get("snapshot_id").getAsString()
                                    : null)
                    .setTracks(
                            hasAndNotNull(jsonObject, "tracks")
                                    ? new PlaylistTracksInformation.JsonUtil().createModelObject(
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
