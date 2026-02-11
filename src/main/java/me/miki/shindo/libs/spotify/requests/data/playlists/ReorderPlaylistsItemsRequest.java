package me.miki.shindo.libs.spotify.requests.data.playlists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.special.SnapshotResult;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = ReorderPlaylistsItemsRequest.Builder.class)
public class ReorderPlaylistsItemsRequest extends AbstractDataRequest<SnapshotResult> {
    private ReorderPlaylistsItemsRequest(final Builder builder) {
        super(builder);
    }
    public SnapshotResult execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new SnapshotResult.JsonUtil().createModelObject(putJson());
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
        public Builder range_start(final Integer range_start) {
            assert (range_start != null);
            assert (range_start >= 0);
            return setBodyParameter("range_start", range_start);
        }
        public Builder range_length(final Integer range_length) {
            assert (range_length != null);
            assert (range_length >= 1);
            return setBodyParameter("range_length", range_length);
        }
        public Builder insert_before(final Integer insert_before) {
            assert (insert_before != null);
            assert (insert_before >= 0);
            return setBodyParameter("insert_before", insert_before);
        }
        public Builder snapshot_id(final String snapshot_id) {
            assert (snapshot_id != null);
            assert (!snapshot_id.isEmpty());
            return setBodyParameter("snapshot_id", snapshot_id);
        }
        @Override
        public ReorderPlaylistsItemsRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/playlists/{playlist_id}/tracks");
            return new ReorderPlaylistsItemsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
