package me.miki.shindo.libs.spotify.requests.data.playlists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.special.SnapshotResult;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = RemoveItemsFromPlaylistRequest.Builder.class)
public class RemoveItemsFromPlaylistRequest extends AbstractDataRequest<SnapshotResult> {
    private RemoveItemsFromPlaylistRequest(final Builder builder) {
        super(builder);
    }
    public SnapshotResult execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new SnapshotResult.JsonUtil().createModelObject(deleteJson());
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
        public Builder tracks(final JsonArray tracks) {
            assert (tracks != null);
            assert (!tracks.isJsonNull());
            assert (tracks.size() <= 100);
            return setBodyParameter("tracks", tracks);
        }
        public Builder snapshotId(final String snapshotId) {
            assert (snapshotId != null);
            assert (!snapshotId.isEmpty());
            return setBodyParameter("snapshot_id", snapshotId);
        }
        @Override
        public RemoveItemsFromPlaylistRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/playlists/{playlist_id}/tracks");
            return new RemoveItemsFromPlaylistRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
