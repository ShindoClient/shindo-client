package me.miki.shindo.libs.spotify.requests.data.follow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = CheckUsersFollowPlaylistRequest.Builder.class)
public class CheckUsersFollowPlaylistRequest extends AbstractDataRequest<Boolean[]> {
    private CheckUsersFollowPlaylistRequest(final Builder builder) {
        super(builder);
    }

    public Boolean[] execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Gson().fromJson(JsonParser.parseString(getJson()).getAsJsonArray(), Boolean[].class);
    }

    public static final class Builder extends AbstractDataRequest.Builder<Boolean[], Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        @Deprecated()
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

        public Builder ids(final String ids) {
            assert (ids != null);
            assert (ids.split(",").length <= 5);
            return setQueryParameter("ids", ids);
        }

        @Override
        public CheckUsersFollowPlaylistRequest build() {
            setPath("/v1/playlists/{playlist_id}/followers/contains");
            return new CheckUsersFollowPlaylistRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
