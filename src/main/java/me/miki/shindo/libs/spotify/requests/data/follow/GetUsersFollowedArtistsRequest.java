package me.miki.shindo.libs.spotify.requests.data.follow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Artist;
import me.miki.shindo.libs.spotify.model_objects.specification.PagingCursorbased;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingCursorbasedRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetUsersFollowedArtistsRequest.Builder.class)
public class GetUsersFollowedArtistsRequest extends AbstractDataRequest<PagingCursorbased<Artist>> {
    private GetUsersFollowedArtistsRequest(final Builder builder) {
        super(builder);
    }

    public PagingCursorbased<Artist> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Artist.JsonUtil().createModelObjectPagingCursorbased(getJson(), "artists");
    }

    public static final class Builder extends AbstractDataPagingCursorbasedRequest.Builder<Artist, String, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder type(final ModelObjectType type) {
            assert (type != null);
            assert (type.getType().equals("artist"));
            return setQueryParameter("type", type);
        }

        @Override
        public Builder limit(final Integer limit) {
            assert (limit != null);
            assert (1 <= limit && limit <= 50);
            return setQueryParameter("limit", limit);
        }

        @Override
        public Builder after(final String after) {
            assert (after != null);
            return setQueryParameter("after", after);
        }

        @Override
        public GetUsersFollowedArtistsRequest build() {
            setPath("/v1/me/following");
            return new GetUsersFollowedArtistsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
