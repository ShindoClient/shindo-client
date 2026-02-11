package me.miki.shindo.libs.spotify.requests.data.playlists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.model_objects.specification.PlaylistSimplified;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = GetListOfCurrentUsersPlaylistsRequest.Builder.class)
public class GetListOfCurrentUsersPlaylistsRequest extends AbstractDataRequest<Paging<PlaylistSimplified>> {
    private GetListOfCurrentUsersPlaylistsRequest(final Builder builder) {
        super(builder);
    }
    public Paging<PlaylistSimplified> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new PlaylistSimplified.JsonUtil().createModelObjectPaging(getJson());
    }
    public static final class Builder extends AbstractDataPagingRequest.Builder<PlaylistSimplified, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }
        @Override
        public Builder limit(final Integer limit) {
            assert (1 <= limit && limit <= 50);
            return setQueryParameter("limit", limit);
        }
        @Override
        public Builder offset(final Integer offset) {
            assert (0 <= offset && offset <= 100000);
            return setQueryParameter("offset", offset);
        }
        @Override
        public GetListOfCurrentUsersPlaylistsRequest build() {
            setPath("/v1/me/playlists");
            return new GetListOfCurrentUsersPlaylistsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
