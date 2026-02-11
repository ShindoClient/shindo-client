package me.miki.shindo.libs.spotify.requests.data.playlists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.model_objects.specification.PlaylistSimplified;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = GetListOfUsersPlaylistsRequest.Builder.class)
public class GetListOfUsersPlaylistsRequest extends AbstractDataRequest<Paging<PlaylistSimplified>> {
    private GetListOfUsersPlaylistsRequest(final Builder builder) {
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
        public Builder user_id(final String user_id) {
            assert (user_id != null);
            assert (!user_id.isEmpty());
            return setPathParameter("user_id", user_id);
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
        public GetListOfUsersPlaylistsRequest build() {
            setPath("/v1/users/{user_id}/playlists");
            return new GetListOfUsersPlaylistsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
