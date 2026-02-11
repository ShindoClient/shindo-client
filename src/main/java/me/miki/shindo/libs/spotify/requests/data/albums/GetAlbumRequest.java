package me.miki.shindo.libs.spotify.requests.data.albums;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Album;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetAlbumRequest.Builder.class)
public class GetAlbumRequest extends AbstractDataRequest<Album> {
    private GetAlbumRequest(final Builder builder) {
        super(builder);
    }

    public Album execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Album.JsonUtil().createModelObject(getJson());
    }

    public static final class Builder extends AbstractDataRequest.Builder<Album, Builder> {
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
        public GetAlbumRequest build() {
            setPath("/v1/albums/{id}");
            return new GetAlbumRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
