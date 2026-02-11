package me.miki.shindo.libs.spotify.requests.data.browse;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.AlbumSimplified;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetListOfNewReleasesRequest.Builder.class)
public class GetListOfNewReleasesRequest extends AbstractDataRequest<Paging<AlbumSimplified>> {
    private GetListOfNewReleasesRequest(final Builder builder) {
        super(builder);
    }

    public Paging<AlbumSimplified> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new AlbumSimplified.JsonUtil().createModelObjectPaging(getJson(), "albums");
    }

    public static final class Builder extends AbstractDataPagingRequest.Builder<AlbumSimplified, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder country(final CountryCode country) {
            assert (country != null);
            return setQueryParameter("country", country);
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

        @Override
        public GetListOfNewReleasesRequest build() {
            setPath("/v1/browse/new-releases");
            return new GetListOfNewReleasesRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
