package me.miki.shindo.libs.spotify.requests.data.search.simplified;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.model_objects.specification.Track;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = SearchTracksRequest.Builder.class)
public class SearchTracksRequest extends AbstractDataRequest<Paging<Track>> {
    private SearchTracksRequest(final Builder builder) {
        super(builder);
    }

    public Paging<Track> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Track.JsonUtil().createModelObjectPaging(getJson(), "tracks");
    }

    public static final class Builder extends AbstractDataPagingRequest.Builder<Track, Builder> {
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
        public SearchTracksRequest build() {
            setPath("/v1/search");
            setQueryParameter("type", "track");
            return new SearchTracksRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
