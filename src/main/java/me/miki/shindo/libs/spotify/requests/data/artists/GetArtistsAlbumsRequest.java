package me.miki.shindo.libs.spotify.requests.data.artists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.AlbumSimplified;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetArtistsAlbumsRequest.Builder.class)
public class GetArtistsAlbumsRequest extends AbstractDataRequest<Paging<AlbumSimplified>> {
    private GetArtistsAlbumsRequest(final Builder builder) {
        super(builder);
    }

    public Paging<AlbumSimplified> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new AlbumSimplified.JsonUtil().createModelObjectPaging(getJson());
    }

    public static final class Builder extends AbstractDataPagingRequest.Builder<AlbumSimplified, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder id(final String id) {
            assert (id != null);
            assert (!id.isEmpty());
            return setPathParameter("id", id);
        }

        @Deprecated
        public Builder album_type(final String album_type) {
            assert (album_type != null);
            assert (album_type.matches("((^|,)(single|album|appears_on|compilation))+$"));
            return setQueryParameter("album_type", album_type);
        }

        public Builder include_groups(final String include_groups) {
            assert (include_groups != null);
            assert (include_groups.matches("((^|,)(single|album|appears_on|compilation))+$"));
            return setQueryParameter("include_groups", include_groups);
        }

        public Builder market(final CountryCode market) {
            assert (market != null);
            return setQueryParameter("market", market);
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

        @Override
        public GetArtistsAlbumsRequest build() {
            setPath("/v1/artists/{id}/albums");
            return new GetArtistsAlbumsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
