package me.miki.shindo.libs.spotify.requests.data.search.simplified.special;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.special.AlbumSimplifiedSpecial;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = SearchAlbumsSpecialRequest.Builder.class)
public class SearchAlbumsSpecialRequest extends AbstractDataRequest<Paging<AlbumSimplifiedSpecial>> {
    private SearchAlbumsSpecialRequest(final Builder builder) {
        super(builder);
    }

    public Paging<AlbumSimplifiedSpecial> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new AlbumSimplifiedSpecial.JsonUtil().createModelObjectPaging(getJson(), "albums");
    }

    public static final class Builder extends AbstractDataPagingRequest.Builder<AlbumSimplifiedSpecial, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder q(final String q) {
            assert (q != null);
            assert (!q.isEmpty());
            return setQueryParameter("q", q);
        }

        public Builder market(final CountryCode market) {
            assert (market != null);
            return setQueryParameter("market", market);
        }

        @Override
        public Builder limit(final Integer limit) {
            assert (limit != null);
            assert (1 <= limit && limit <= 50);
            return setQueryParameter("limit", limit);
        }

        @Override
        public Builder offset(final Integer offset) {
            assert (offset != null);
            assert (0 <= offset && offset <= 100000);
            return setQueryParameter("offset", offset);
        }

        @Override
        public SearchAlbumsSpecialRequest build() {
            setPath("/v1/search");
            setQueryParameter("type", "album");
            return new SearchAlbumsSpecialRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
