package me.miki.shindo.libs.spotify.requests.data.follow.legacy;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = UnfollowPlaylistRequest.Builder.class)
public class UnfollowPlaylistRequest extends AbstractDataRequest<String> {
    private UnfollowPlaylistRequest(final Builder builder) {
        super(builder);
    }

    public String execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return deleteJson();
    }

    public static final class Builder extends AbstractDataRequest.Builder<String, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder owner_id(final String owner_id) {
            assert (owner_id != null);
            assert (!owner_id.isEmpty());
            return setPathParameter("owner_id", owner_id);
        }

        public Builder playlist_id(final String playlist_id) {
            assert (playlist_id != null);
            assert (!playlist_id.isEmpty());
            return setPathParameter("playlist_id", playlist_id);
        }

        @Override
        public UnfollowPlaylistRequest build() {
            setPath("/v1/users/{owner_id}/playlists/{playlist_id}/followers");
            return new UnfollowPlaylistRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
