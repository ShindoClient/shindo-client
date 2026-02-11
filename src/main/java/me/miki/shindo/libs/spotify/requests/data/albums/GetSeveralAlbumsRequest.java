package me.miki.shindo.libs.spotify.requests.data.albums;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Album;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetSeveralAlbumsRequest.Builder.class)
public class GetSeveralAlbumsRequest extends AbstractDataRequest<Album[]> {
    private GetSeveralAlbumsRequest(final Builder builder) {
        super(builder);
    }

    public Album[] execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Album.JsonUtil().createModelObjectArray(getJson(), "albums");
    }

    public static final class Builder extends AbstractDataRequest.Builder<Album[], Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder ids(final String ids) {
            assert (ids != null);
            assert (ids.split(",").length <= 20);
            return setQueryParameter("ids", ids);
        }

        public Builder market(final CountryCode market) {
            assert (market != null);
            return setQueryParameter("market", market);
        }

        @Override
        public GetSeveralAlbumsRequest build() {
            setPath("/v1/albums");
            return new GetSeveralAlbumsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
