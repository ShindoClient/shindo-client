package me.miki.shindo.libs.spotify.requests.data.browse;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.i18n.LanguageCode;
import me.miki.shindo.libs.spotify.SpotifyApi;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.special.FeaturedPlaylists;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.Date;

@JsonDeserialize(builder = GetListOfFeaturedPlaylistsRequest.Builder.class)
public class GetListOfFeaturedPlaylistsRequest extends AbstractDataRequest<FeaturedPlaylists> {
    private GetListOfFeaturedPlaylistsRequest(final Builder builder) {
        super(builder);
    }

    public FeaturedPlaylists execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new FeaturedPlaylists.JsonUtil().createModelObject(getJson());
    }

    public static final class Builder extends AbstractDataRequest.Builder<FeaturedPlaylists, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder locale(final String locale) {
            assert (locale != null);
            assert (locale.contains("_"));
            String[] localeParts = locale.split("_");
            assert (localeParts.length == 2);
            assert (LanguageCode.getByCode(localeParts[0]) != null);
            assert (CountryCode.getByCode(localeParts[1]) != null);
            return setQueryParameter("locale", locale);
        }

        public Builder country(final CountryCode country) {
            assert (country != null);
            return setQueryParameter("country", country);
        }

        public Builder timestamp(final Date timestamp) {
            assert (timestamp != null);
            return setQueryParameter("timestamp", SpotifyApi.formatDefaultDate(timestamp));
        }

        public Builder limit(final Integer limit) {
            assert (1 <= limit && limit <= 50);
            return setQueryParameter("limit", limit);
        }

        public Builder offset(final Integer offset) {
            assert (offset >= 0);
            return setQueryParameter("offset", offset);
        }

        @Override
        public GetListOfFeaturedPlaylistsRequest build() {
            setPath("/v1/browse/featured-playlists");
            return new GetListOfFeaturedPlaylistsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
