package me.miki.shindo.libs.spotify.requests.data.playlists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.special.SnapshotResult;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = AddItemsToPlaylistRequest.Builder.class)
public class AddItemsToPlaylistRequest extends AbstractDataRequest<SnapshotResult> {
    private AddItemsToPlaylistRequest(final Builder builder) {
        super(builder);
    }
    public SnapshotResult execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new SnapshotResult.JsonUtil().createModelObject(postJson());
    }
    public static final class Builder extends AbstractDataRequest.Builder<SnapshotResult, Builder> {
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
        public Builder position(final Integer position) {
            return position(position, false);
        }
        public Builder uris(final JsonArray uris) {
            assert (uris != null);
            assert (!uris.isJsonNull());
            assert (uris.size() <= 100);
            return setBodyParameter("uris", uris);
        }
        public Builder position(final Integer position, final Boolean use_body) {
            assert (position >= 0);

            if (use_body) {
                return setBodyParameter("position", position);
            } else {
                return setQueryParameter("position", position);
            }
        }
        @Override
        public AddItemsToPlaylistRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/playlists/{playlist_id}/tracks");
            return new AddItemsToPlaylistRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
