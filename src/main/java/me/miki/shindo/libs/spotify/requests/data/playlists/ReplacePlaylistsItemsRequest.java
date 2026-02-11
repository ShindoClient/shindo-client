package me.miki.shindo.libs.spotify.requests.data.playlists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = ReplacePlaylistsItemsRequest.Builder.class)
public class ReplacePlaylistsItemsRequest extends AbstractDataRequest<String> {
    private ReplacePlaylistsItemsRequest(final Builder builder) {
        super(builder);
    }
    public String execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return putJson();
    }
    public static final class Builder extends AbstractDataRequest.Builder<String, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }
        public Builder playlist_id(final String playlist_id) {
            assert (playlist_id != null);
            assert (!playlist_id.isEmpty());
            return setPathParameter("playlist_id", playlist_id);
        }
        public Builder uris(final String uris) {
            assert (uris != null);
            assert (!uris.isEmpty());
            assert (uris.split(",").length <= 100);
            return setQueryParameter("uris", uris);
        }
        public Builder uris(final JsonArray uris) {
            assert (uris != null);
            assert (!uris.isJsonNull());
            assert (uris.size() <= 100);
            return setBodyParameter("uris", uris);
        }
        @Override
        public ReplacePlaylistsItemsRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/playlists/{playlist_id}/tracks");
            return new ReplacePlaylistsItemsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
