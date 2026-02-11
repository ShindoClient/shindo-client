package me.miki.shindo.libs.spotify.requests.data.shows;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.EpisodeSimplified;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetShowsEpisodesRequest.Builder.class)
public class GetShowsEpisodesRequest extends AbstractDataRequest<Paging<EpisodeSimplified>> {
    private GetShowsEpisodesRequest(Builder builder) {
        super(builder);
    }

    @Override
    public Paging<EpisodeSimplified> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new EpisodeSimplified.JsonUtil().createModelObjectPaging(getJson());
    }

    public static final class Builder extends AbstractDataPagingRequest.Builder<EpisodeSimplified, Builder> {
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
        public GetShowsEpisodesRequest build() {
            setPath("/v1/shows/{id}/episodes");
            return new GetShowsEpisodesRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
