package me.miki.shindo.libs.spotify.requests.data.artists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Artist;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetArtistRequest.Builder.class)
public class GetArtistRequest extends AbstractDataRequest<Artist> {
    private GetArtistRequest(final Builder builder) {
        super(builder);
    }

    public Artist execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Artist.JsonUtil().createModelObject(getJson());
    }

    public static final class Builder extends AbstractDataRequest.Builder<Artist, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder id(final String id) {
            assert (id != null);
            assert (!id.isEmpty());
            return setPathParameter("id", id);
        }

        @Override
        public GetArtistRequest build() {
            setPath("/v1/artists/{id}");
            return new GetArtistRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
