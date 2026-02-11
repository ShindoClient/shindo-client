package me.miki.shindo.libs.spotify.requests.data.artists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Track;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetArtistsTopTracksRequest.Builder.class)
public class GetArtistsTopTracksRequest extends AbstractDataRequest<Track[]> {
    private GetArtistsTopTracksRequest(final Builder builder) {
        super(builder);
    }

    public Track[] execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Track.JsonUtil().createModelObjectArray(getJson(), "tracks");
    }

    public static final class Builder extends AbstractDataRequest.Builder<Track[], Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder id(final String id) {
            assert (id != null);
            assert (!id.isEmpty());
            return setPathParameter("id", id);
        }

        public Builder country(final CountryCode country) {
            assert (country != null);
            return setQueryParameter("country", country);
        }

        @Override
        public GetArtistsTopTracksRequest build() {
            setPath("/v1/artists/{id}/top-tracks");
            return new GetArtistsTopTracksRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
