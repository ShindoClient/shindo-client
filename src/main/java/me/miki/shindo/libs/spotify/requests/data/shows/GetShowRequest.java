package me.miki.shindo.libs.spotify.requests.data.shows;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Show;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetShowRequest.Builder.class)
public class GetShowRequest extends AbstractDataRequest<Show> {
    private GetShowRequest(final Builder builder) {
        super(builder);
    }

    @Override
    public Show execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Show.JsonUtil().createModelObject(getJson());
    }

    public static final class Builder extends AbstractDataRequest.Builder<Show, Builder> {
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
        public GetShowRequest build() {
            setPath("/v1/shows/{id}");
            return new GetShowRequest(this);
        }

        @Override
        protected GetShowRequest.Builder self() {
            return this;
        }
    }
}
