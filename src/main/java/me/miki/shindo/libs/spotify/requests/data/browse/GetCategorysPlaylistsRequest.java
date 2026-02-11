package me.miki.shindo.libs.spotify.requests.data.browse;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.model_objects.specification.PlaylistSimplified;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetCategorysPlaylistsRequest.Builder.class)
public class GetCategorysPlaylistsRequest extends AbstractDataRequest<Paging<PlaylistSimplified>> {
    private GetCategorysPlaylistsRequest(final Builder builder) {
        super(builder);
    }

    public Paging<PlaylistSimplified> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new PlaylistSimplified.JsonUtil().createModelObjectPaging(getJson(), "playlists");
    }

    public static final class Builder extends AbstractDataPagingRequest.Builder<PlaylistSimplified, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder category_id(final String category_id) {
            assert (category_id != null);
            assert (category_id.matches("^[a-z]+$"));
            return setPathParameter("category_id", category_id);
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
        public GetCategorysPlaylistsRequest build() {
            setPath("/v1/browse/categories/{category_id}/playlists");
            return new GetCategorysPlaylistsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
