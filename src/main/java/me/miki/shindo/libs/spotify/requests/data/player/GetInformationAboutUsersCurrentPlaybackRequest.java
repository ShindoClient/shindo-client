package me.miki.shindo.libs.spotify.requests.data.player;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetInformationAboutUsersCurrentPlaybackRequest.Builder.class)
public class GetInformationAboutUsersCurrentPlaybackRequest extends AbstractDataRequest<CurrentlyPlayingContext> {
    private GetInformationAboutUsersCurrentPlaybackRequest(final Builder builder) {
        super(builder);
    }

    public CurrentlyPlayingContext execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new CurrentlyPlayingContext.JsonUtil().createModelObject(getJson());
    }

    public static final class Builder extends AbstractDataRequest.Builder<CurrentlyPlayingContext, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
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
        public GetInformationAboutUsersCurrentPlaybackRequest build() {
            setPath("/v1/me/player");
            return new GetInformationAboutUsersCurrentPlaybackRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
