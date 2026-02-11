package me.miki.shindo.libs.spotify.requests.data.artists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Artist;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetArtistsRelatedArtistsRequest.Builder.class)
public class GetArtistsRelatedArtistsRequest extends AbstractDataRequest<Artist[]> {
    private GetArtistsRelatedArtistsRequest(final Builder builder) {
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

        public Builder id(final String id) {
            assert (id != null);
            assert (!id.isEmpty());
            return setPathParameter("id", id);
        }

        @Override
        public GetArtistsRelatedArtistsRequest build() {
            setPath("/v1/artists/{id}/related-artists");
            return new GetArtistsRelatedArtistsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
