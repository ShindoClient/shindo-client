package me.miki.shindo.libs.spotify.requests.data.tracks;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Track;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetSeveralTracksRequest.Builder.class)
public class GetSeveralTracksRequest extends AbstractDataRequest<Track[]> {
    private GetSeveralTracksRequest(final Builder builder) {
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

        public Builder ids(final String ids) {
            assert (ids != null);
            assert (ids.split(",").length <= 50);
            return setQueryParameter("ids", ids);
        }

        public Builder market(final CountryCode market) {
            assert (market != null);
            return setQueryParameter("market", market);
        }

        @Override
        public GetSeveralTracksRequest build() {
            setPath("/v1/tracks");
            return new GetSeveralTracksRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
