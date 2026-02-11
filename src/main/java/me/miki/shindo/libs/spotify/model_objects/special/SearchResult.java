package me.miki.shindo.libs.spotify.model_objects.special;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
import me.miki.shindo.libs.spotify.model_objects.specification.*;
import me.miki.shindo.libs.spotify.requests.data.personalization.interfaces.IArtistTrackModelObject;
import me.miki.shindo.libs.spotify.requests.data.search.SearchItemRequest;
import me.miki.shindo.libs.spotify.requests.data.search.interfaces.ISearchModelObject;
@JsonDeserialize(builder = SearchResult.Builder.class)
public class SearchResult extends AbstractModelObject implements IArtistTrackModelObject, ISearchModelObject {
    private final Paging<AlbumSimplified> albums;
    private final Paging<Artist> artists;
    private final Paging<EpisodeSimplified> episodes;
    private final Paging<PlaylistSimplified> playlists;
    private final Paging<ShowSimplified> shows;
    private final Paging<Track> tracks;

    private SearchResult(final Builder builder) {
        super(builder);

        this.albums = builder.albums;
        this.artists = builder.artists;
        this.episodes = builder.episodes;
        this.playlists = builder.playlists;
        this.shows = builder.shows;
        this.tracks = builder.tracks;
    }
    public Paging<AlbumSimplified> getAlbums() {
        return albums;
    }
    public Paging<Artist> getArtists() {
        return artists;
    }
    public Paging<EpisodeSimplified> getEpisodes() {
        return episodes;
    }
    public Paging<PlaylistSimplified> getPlaylists() {
        return playlists;
    }
    public Paging<ShowSimplified> getShows() {
        return shows;
    }
    public Paging<Track> getTracks() {
        return tracks;
    }

    @Override
    public String toString() {
        return "SearchResult(albums=" + albums + ", artists=" + artists + ", episodes=" + episodes + ", playlists="
                + playlists + ", shows=" + shows + ", tracks=" + tracks + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Paging<AlbumSimplified> albums;
        private Paging<Artist> artists;
        private Paging<EpisodeSimplified> episodes;
        private Paging<PlaylistSimplified> playlists;
        private Paging<ShowSimplified> shows;
        private Paging<Track> tracks;
        public Builder setAlbums(Paging<AlbumSimplified> albums) {
            this.albums = albums;
            return this;
        }
        public Builder setArtists(Paging<Artist> artists) {
            this.artists = artists;
            return this;
        }
        public Builder setEpisodes(Paging<EpisodeSimplified> episodes) {
            this.episodes = episodes;
            return this;
        }
        public Builder setPlaylists(Paging<PlaylistSimplified> playlists) {
            this.playlists = playlists;
            return this;
        }
        public Builder setShows(Paging<ShowSimplified> shows) {
            this.shows = shows;
            return this;
        }
        public Builder setTracks(Paging<Track> tracks) {
            this.tracks = tracks;
            return this;
        }

        @Override
        public SearchResult build() {
            return new SearchResult(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<SearchResult> {
        public SearchResult createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new SearchResult.Builder()
                    .setAlbums(
                            hasAndNotNull(jsonObject, "albums")
                                    ? new AlbumSimplified.JsonUtil().createModelObjectPaging(
                                    jsonObject.getAsJsonObject("albums"))
                                    : null)
                    .setArtists(
                            hasAndNotNull(jsonObject, "artists")
                                    ? new Artist.JsonUtil().createModelObjectPaging(
                                    jsonObject.getAsJsonObject("artists"))
                                    : null)
                    .setEpisodes(
                            hasAndNotNull(jsonObject, "episodes")
                                    ? new EpisodeSimplified.JsonUtil().createModelObjectPaging(
                                    jsonObject.getAsJsonObject("episodes"))
                                    : null)
                    .setPlaylists(
                            hasAndNotNull(jsonObject, "playlists")
                                    ? new PlaylistSimplified.JsonUtil().createModelObjectPaging(
                                    jsonObject.getAsJsonObject("playlists"))
                                    : null)
                    .setShows(
                            hasAndNotNull(jsonObject, "shows")
                                    ? new ShowSimplified.JsonUtil().createModelObjectPaging(
                                    jsonObject.getAsJsonObject("shows"))
                                    : null)
                    .setTracks(
                            hasAndNotNull(jsonObject, "tracks")
                                    ? new Track.JsonUtil().createModelObjectPaging(
                                    jsonObject.getAsJsonObject("tracks"))
                                    : null)
                    .build();
        }
    }

}
