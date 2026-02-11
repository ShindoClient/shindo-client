package me.miki.shindo.libs.spotify.requests.data.playlists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.model_objects.specification.PlaylistTrack;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = GetPlaylistsItemsRequest.Builder.class)
public class GetPlaylistsItemsRequest extends AbstractDataRequest<Paging<PlaylistTrack>> {
    private GetPlaylistsItemsRequest(final Builder builder) {
        super(builder);
    }
    public Paging<PlaylistTrack> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new PlaylistTrack.JsonUtil().createModelObjectPaging(getJson());
    }
    public static final class Builder extends AbstractDataPagingRequest.Builder<PlaylistTrack, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }
        public Builder playlist_id(final String playlist_id) {
            assert (playlist_id != null);
            assert (!playlist_id.isEmpty());
            return setPathParameter("playlist_id", playlist_id);
        }
        public Builder fields(final String fields) {
            assert (fields != null);
            assert (!fields.isEmpty());
            return setQueryParameter("fields", fields);
        }
        @Override
        public Builder limit(final Integer limit) {
            assert (1 <= limit && limit <= 100);
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
        public Builder additionalTypes(final String additionalTypes) {
            assert (additionalTypes != null);
            assert (additionalTypes.matches("((^|,)(episode|track))+$"));
            return setQueryParameter("additional_types", additionalTypes);
        }
        @Override
        public GetPlaylistsItemsRequest build() {
            setPath("/v1/playlists/{playlist_id}/tracks");
            return new GetPlaylistsItemsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
