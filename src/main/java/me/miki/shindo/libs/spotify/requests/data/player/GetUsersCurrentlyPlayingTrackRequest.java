package me.miki.shindo.libs.spotify.requests.data.player;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetUsersCurrentlyPlayingTrackRequest.Builder.class)
public class GetUsersCurrentlyPlayingTrackRequest extends AbstractDataRequest<CurrentlyPlaying> {
    private GetUsersCurrentlyPlayingTrackRequest(final Builder builder) {
        super(builder);
    }

    public CurrentlyPlaying execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new CurrentlyPlaying.JsonUtil().createModelObject(getJson());
    }

    public static final class Builder extends AbstractDataRequest.Builder<CurrentlyPlaying, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder market(final CountryCode market) {
            assert (market != null);
            return setQueryParameter("market", market);
        }

        public GetUsersCurrentlyPlayingTrackRequest.Builder additionalTypes(final String additionalTypes) {
            assert (additionalTypes != null);
            assert (additionalTypes.matches("((^|,)(episode|track))+$"));
            return setQueryParameter("additional_types", additionalTypes);
        }

        @Override
        public GetUsersCurrentlyPlayingTrackRequest build() {
            setPath("/v1/me/player/currently-playing");
            return new GetUsersCurrentlyPlayingTrackRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
