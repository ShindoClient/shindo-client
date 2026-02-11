package me.miki.shindo.libs.spotify.requests.data.search.simplified;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.AlbumSimplified;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = SearchAlbumsRequest.Builder.class)
public class SearchAlbumsRequest extends AbstractDataRequest<Paging<AlbumSimplified>> {
    private SearchAlbumsRequest(final Builder builder) {
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

        public Builder includeExternal(String includeExternal) {
            assert (includeExternal != null);
            assert (includeExternal.matches("audio"));
            return setQueryParameter("include_external", includeExternal);
        }

        @Override
        public SearchAlbumsRequest build() {
            setPath("/v1/search");
            setQueryParameter("type", "album");
            return new SearchAlbumsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
