package me.miki.shindo.libs.spotify.requests.data.albums;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.model_objects.specification.TrackSimplified;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetAlbumsTracksRequest.Builder.class)
public class GetAlbumsTracksRequest extends AbstractDataRequest<Paging<TrackSimplified>> {
    private GetAlbumsTracksRequest(final Builder builder) {
        super(builder);
    }

    public Paging<TrackSimplified> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new TrackSimplified.JsonUtil().createModelObjectPaging(getJson());
    }

    public static final class Builder extends AbstractDataPagingRequest.Builder<TrackSimplified, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder id(final String id) {
            assert (id != null);
            assert (!id.isEmpty());
            return setPathParameter("id", id);
        }

        @Override
        public Builder limit(final Integer limit) {
            assert (1 <= limit && limit <= 50);
            return setQueryParameter("limit", limit);
        }

        @Override
        public Builder offset(final Integer offset) {
            assert (offset >= 0);
            return setQueryParameter("offset", offset);
        }

        public Builder market(final CountryCode market) {
            assert (market != null);
            return setQueryParameter("market", market);
        }

        @Override
        public GetAlbumsTracksRequest build() {
            setPath("/v1/albums/{id}/tracks");
            return new GetAlbumsTracksRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
