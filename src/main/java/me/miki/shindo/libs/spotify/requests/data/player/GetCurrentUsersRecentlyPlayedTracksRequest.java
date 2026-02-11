package me.miki.shindo.libs.spotify.requests.data.player;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.SpotifyApi;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.PagingCursorbased;
import me.miki.shindo.libs.spotify.model_objects.specification.PlayHistory;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingCursorbasedRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.Date;

@JsonDeserialize(builder = GetCurrentUsersRecentlyPlayedTracksRequest.Builder.class)
public class GetCurrentUsersRecentlyPlayedTracksRequest extends AbstractDataRequest<PagingCursorbased<PlayHistory>> {
    private GetCurrentUsersRecentlyPlayedTracksRequest(final Builder builder) {
        super(builder);
    }

    public PagingCursorbased<PlayHistory> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new PlayHistory.JsonUtil().createModelObjectPagingCursorbased(getJson());
    }

    public static final class Builder extends AbstractDataPagingCursorbasedRequest.Builder<PlayHistory, Date, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        @Override
        public Builder limit(final Integer limit) {
            assert (limit != null);
            assert (1 <= limit && limit <= 50);
            return setQueryParameter("limit", limit);
        }

        @Override
        public Builder after(final Date after) {
            assert (after != null);
            return setQueryParameter("after", SpotifyApi.formatDefaultDate(after));
        }

        public Builder before(final Date before) {
            assert (before != null);
            return setQueryParameter("before", SpotifyApi.formatDefaultDate(before));
        }

        @Override
        public GetCurrentUsersRecentlyPlayedTracksRequest build() {
            setPath("/v1/me/player/recently-played");
            return new GetCurrentUsersRecentlyPlayedTracksRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
