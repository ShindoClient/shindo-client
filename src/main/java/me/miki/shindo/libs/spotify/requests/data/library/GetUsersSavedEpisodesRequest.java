package me.miki.shindo.libs.spotify.requests.data.library;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.miscellaneous.SavedEpisode;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetUsersSavedEpisodesRequest.Builder.class)
public class GetUsersSavedEpisodesRequest extends AbstractDataRequest<Paging<SavedEpisode>> {
    private GetUsersSavedEpisodesRequest(GetUsersSavedEpisodesRequest.Builder builder) {
        super(builder);
    }

    @Override
    public Paging<SavedEpisode> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new SavedEpisode.JsonUtil().createModelObjectPaging(getJson());
    }

    public static final class Builder extends AbstractDataPagingRequest.Builder<SavedEpisode, GetUsersSavedEpisodesRequest.Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        @Override
        public GetUsersSavedEpisodesRequest.Builder limit(final Integer limit) {
            assert (1 <= limit && limit <= 50);
            return setQueryParameter("limit", limit);
        }

        @Override
        public GetUsersSavedEpisodesRequest.Builder offset(final Integer offset) {
            assert (offset >= 0);
            return setQueryParameter("offset", offset);
        }

        public GetUsersSavedEpisodesRequest.Builder market(final CountryCode market) {
            assert (market != null);
            return setQueryParameter("market", market);
        }

        @Override
        public GetUsersSavedEpisodesRequest build() {
            setPath("/v1/me/episodes");
            return new GetUsersSavedEpisodesRequest(this);
        }

        @Override
        protected GetUsersSavedEpisodesRequest.Builder self() {
            return this;
        }
    }
}
