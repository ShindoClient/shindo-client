package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
import me.miki.shindo.libs.spotify.requests.data.playlists.RemoveItemsFromPlaylistRequest;

import java.util.Arrays;
import java.util.Objects;
@JsonDeserialize(builder = Playlist.Builder.class)
public class Playlist extends AbstractModelObject {
    private final Boolean collaborative;
    private final String description;
    private final ExternalUrl externalUrls;
    private final Followers followers;
    private final String href;
    private final String id;
    private final Image[] images;
    private final String name;
    private final User owner;
    private final Boolean publicAccess;
    private final String snapshotId;
    private final Paging<PlaylistTrack> tracks;
    private final ModelObjectType type;
    private final String uri;

    private Playlist(final Builder builder) {
        super(builder);

        this.collaborative = builder.collaborative;
        this.description = builder.description;
        this.externalUrls = builder.externalUrls;
        this.followers = builder.followers;
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
    public Followers getFollowers() {
        return followers;
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
    public Paging<PlaylistTrack> getTracks() {
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
        return "Playlist(name=" + name + ", description=" + description + ", tracks=" + tracks + ", collaborative="
                + collaborative + ", externalUrls=" + externalUrls + ", followers=" + followers + ", href=" + href + ", id="
                + id + ", images=" + Arrays.toString(images) + ", owner=" + owner + ", publicAccess=" + publicAccess
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
        Playlist playlist = (Playlist) o;
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
        private Followers followers;
        private String href;
        private String id;
        private Image[] images;
        private String name;
        private User owner;
        private Boolean publicAccess;
        private String snapshotId;
        private Paging<PlaylistTrack> tracks;
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
        public Builder setFollowers(Followers followers) {
            this.followers = followers;
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
        public Builder setTracks(Paging<PlaylistTrack> tracks) {
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
        public Playlist build() {
            return new Playlist(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<Playlist> {
        public Playlist createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Playlist.Builder()
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
                    .setFollowers(
                            hasAndNotNull(jsonObject, "followers")
                                    ? new Followers.JsonUtil().createModelObject(
                                    jsonObject.getAsJsonObject("followers"))
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
                                    ? new PlaylistTrack.JsonUtil().createModelObjectPaging(
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
