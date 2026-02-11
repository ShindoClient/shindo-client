package me.miki.shindo.libs.spotify.requests.data.playlists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Playlist;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = CreatePlaylistRequest.Builder.class)
public class CreatePlaylistRequest extends AbstractDataRequest<Playlist> {
    private CreatePlaylistRequest(final Builder builder) {
        super(builder);
    }
    public Playlist execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Playlist.JsonUtil().createModelObject(postJson());
    }
    public static final class Builder extends AbstractDataRequest.Builder<Playlist, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }
        public Builder user_id(final String user_id) {
            assert (user_id != null);
            assert (!user_id.isEmpty());
            return setPathParameter("user_id", user_id);
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
        public CreatePlaylistRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/users/{user_id}/playlists");
            return new CreatePlaylistRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
