package me.miki.shindo.libs.spotify.requests.data.artists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Artist;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetSeveralArtistsRequest.Builder.class)
public class GetSeveralArtistsRequest extends AbstractDataRequest<Artist[]> {
    private GetSeveralArtistsRequest(final Builder builder) {
        super(builder);
    }

    public Artist[] execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Artist.JsonUtil().createModelObjectArray(getJson(), "artists");
    }

    public static final class Builder extends AbstractDataRequest.Builder<Artist[], Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder ids(final String ids) {
            assert (ids != null);
            assert (ids.split(",").length <= 50);
            return setQueryParameter("ids", ids);
        }

        @Override
        public GetSeveralArtistsRequest build() {
            setPath("/v1/artists");
            return new GetSeveralArtistsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
