package me.miki.shindo.libs.spotify.requests.data.tracks;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Track;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetTrackRequest.Builder.class)
public class GetTrackRequest extends AbstractDataRequest<Track> {
    private GetTrackRequest(final Builder builder) {
        super(builder);
    }

    public Track execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Track.JsonUtil().createModelObject(getJson());
    }

    public static final class Builder extends AbstractDataRequest.Builder<Track, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder id(final String id) {
            assert (id != null);
            assert (!id.isEmpty());
            return setPathParameter("id", id);
        }

        public Builder market(final CountryCode market) {
            assert (market != null);
            return setQueryParameter("market", market);
        }

        @Override
        public GetTrackRequest build() {
            setPath("/v1/tracks/{id}");
            return new GetTrackRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
