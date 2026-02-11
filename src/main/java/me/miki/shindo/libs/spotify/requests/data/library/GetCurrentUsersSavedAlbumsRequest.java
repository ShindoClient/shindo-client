package me.miki.shindo.libs.spotify.requests.data.library;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.model_objects.specification.SavedAlbum;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetCurrentUsersSavedAlbumsRequest.Builder.class)
public class GetCurrentUsersSavedAlbumsRequest extends AbstractDataRequest<Paging<SavedAlbum>> {
    private GetCurrentUsersSavedAlbumsRequest(final Builder builder) {
        super(builder);
    }

    public Paging<SavedAlbum> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new SavedAlbum.JsonUtil().createModelObjectPaging(getJson());
    }

    public static final class Builder extends AbstractDataPagingRequest.Builder<SavedAlbum, Builder> {
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
            assert (offset >= 0);
            return setQueryParameter("offset", offset);
        }

        public Builder market(final CountryCode market) {
            assert (market != null);
            return setQueryParameter("market", market);
        }

        @Override
        public GetCurrentUsersSavedAlbumsRequest build() {
            setPath("/v1/me/albums");
            return new GetCurrentUsersSavedAlbumsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
