package me.miki.shindo.libs.spotify.requests.data.personalization.simplified;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Artist;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = GetUsersTopArtistsRequest.Builder.class)
public class GetUsersTopArtistsRequest extends AbstractDataRequest<Paging<Artist>> {
    private GetUsersTopArtistsRequest(final Builder builder) {
        super(builder);
    }
    public Paging<Artist> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Artist.JsonUtil().createModelObjectPaging(getJson());
    }
    public static final class Builder extends AbstractDataPagingRequest.Builder<Artist, Builder> {
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
        public GetUsersTopArtistsRequest build() {
            setPath("/v1/me/top/artists");
            return new GetUsersTopArtistsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
