package me.miki.shindo.libs.spotify.requests.data.playlists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Playlist;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = GetPlaylistRequest.Builder.class)
public class GetPlaylistRequest extends AbstractDataRequest<Playlist> {
    private GetPlaylistRequest(final Builder builder) {
        super(builder);
    }
    public Playlist execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Playlist.JsonUtil().createModelObject(getJson());
    }
    public static final class Builder extends AbstractDataRequest.Builder<Playlist, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }
        public Builder playlist_id(final String playlist_id) {
            assert (playlist_id != null);
            assert (!playlist_id.isEmpty());
            return setPathParameter("playlist_id", playlist_id);
        }
        public Builder fields(final String fields) {
            assert (fields != null);
            assert (!fields.isEmpty());
            return setQueryParameter("fields", fields);
        }
        public Builder market(final CountryCode market) {
            assert (market != null);
            return setQueryParameter("market", market);
        }
        public Builder additionalTypes(final String additionalTypes) {
            assert (additionalTypes != null);
            assert (additionalTypes.matches("((^|,)(episode|track))+$"));
            return setQueryParameter("additional_types", additionalTypes);
        }
        @Override
        public GetPlaylistRequest build() {
            setPath("/v1/playlists/{playlist_id}");
            return new GetPlaylistRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
