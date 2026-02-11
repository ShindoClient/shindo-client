package me.miki.shindo.libs.spotify.requests.data.personalization.simplified;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.model_objects.specification.Track;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = GetUsersTopTracksRequest.Builder.class)
public class GetUsersTopTracksRequest extends AbstractDataRequest<Paging<Track>> {
    private GetUsersTopTracksRequest(final Builder builder) {
        super(builder);
    }
    public Paging<Track> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Track.JsonUtil().createModelObjectPaging(getJson());
    }
    public static final class Builder extends AbstractDataPagingRequest.Builder<Track, Builder> {
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
        public Builder offset(final Integer offset) {
            assert (offset >= 0);
            return setQueryParameter("offset", offset);
        }
        public Builder time_range(final String time_range) {
            assert (time_range != null);
            assert (time_range.equals("long_term") || time_range.equals("medium_term") || time_range.equals("short_term"));
            return setQueryParameter("time_range", time_range);
        }
        @Override
        public GetUsersTopTracksRequest build() {
            setPath("/v1/me/top/tracks");
            return new GetUsersTopTracksRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
