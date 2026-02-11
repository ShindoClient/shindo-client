package me.miki.shindo.libs.spotify.requests.data.shows;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.ShowSimplified;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetSeveralShowsRequest.Builder.class)
public class GetSeveralShowsRequest extends AbstractDataRequest<ShowSimplified[]> {
    private GetSeveralShowsRequest(final Builder builder) {
        super(builder);
    }

    @Override
    public ShowSimplified[] execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new ShowSimplified.JsonUtil().createModelObjectArray(getJson(), "shows");
    }

    public static final class Builder extends AbstractDataRequest.Builder<ShowSimplified[], Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public GetSeveralShowsRequest.Builder ids(final String ids) {
            assert (ids != null);
            assert (ids.split(",").length <= 50);
            return setQueryParameter("ids", ids);
        }

        public GetSeveralShowsRequest.Builder market(final CountryCode market) {
            assert (market != null);
            return setQueryParameter("market", market);
        }

        @Override
        public GetSeveralShowsRequest build() {
            setPath("/v1/shows");
            return new GetSeveralShowsRequest(this);
        }

        @Override
        protected GetSeveralShowsRequest.Builder self() {
            return this;
        }
    }
}
