package me.miki.shindo.libs.spotify.requests.data.playlists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = ChangePlaylistsDetailsRequest.Builder.class)
public class ChangePlaylistsDetailsRequest extends AbstractDataRequest<String> {
    private ChangePlaylistsDetailsRequest(final Builder builder) {
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
        public Builder name(final String name) {
            assert (name != null);
            assert (!name.isEmpty());
            return setBodyParameter("name", name);
        }
        public Builder public_(final Boolean public_) {
            return setBodyParameter("public", public_);
        }
        public Builder collaborative(final Boolean collaborative) {
            return setBodyParameter("collaborative", collaborative);
        }
        public Builder description(final String description) {
            assert (description != null);
            assert (!description.isEmpty());
            return setBodyParameter("description", description);
        }
        @Override
        public ChangePlaylistsDetailsRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/playlists/{playlist_id}");
            return new ChangePlaylistsDetailsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
