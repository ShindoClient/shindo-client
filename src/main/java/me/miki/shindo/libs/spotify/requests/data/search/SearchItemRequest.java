package me.miki.shindo.libs.spotify.requests.data.search;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.special.SearchResult;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = SearchItemRequest.Builder.class)
public class SearchItemRequest extends AbstractDataRequest<SearchResult> {
    private SearchItemRequest(final Builder builder) {
        super(builder);
    }

    public SearchResult execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new SearchResult.JsonUtil().createModelObject(getJson());
    }

    public static final class Builder extends AbstractDataRequest.Builder<SearchResult, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder q(final String q) {
            assert (q != null);
            assert (!q.isEmpty());
            return setQueryParameter("q", q);
        }

        public Builder type(final String type) {
            assert (type != null);
            assert (type.matches("((^|,)(album|artist|episode|playlist|show|track))+$"));
            return setQueryParameter("type", type);
        }

        public Builder market(final CountryCode market) {
            assert (market != null);
            return setQueryParameter("market", market);
        }

        public Builder limit(final Integer limit) {
            assert (limit != null);
            assert (1 <= limit && limit <= 50);
            return setQueryParameter("limit", limit);
        }

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
        public SearchItemRequest build() {
            setPath("/v1/search");
            return new SearchItemRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
