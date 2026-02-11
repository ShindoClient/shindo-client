package me.miki.shindo.libs.spotify.requests.data.library;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = RemoveUsersSavedShowsRequest.Builder.class)
public class RemoveUsersSavedShowsRequest extends AbstractDataRequest<String> {
    private RemoveUsersSavedShowsRequest(final Builder builder) {
        super(builder);
    }

    @Override
    public String execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return deleteJson();
    }

    public static final class Builder extends AbstractDataRequest.Builder<String, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder ids(final String ids) {
            assert (ids != null);
            assert (ids.split(",").length <= 50);
            return setQueryParameter("ids", ids);
        }

        public Builder market(final CountryCode market) {
            assert (market != null);
            return setQueryParameter("market", market);
        }

        public Builder ids(final JsonArray ids) {
            assert (ids != null);
            assert (!ids.isJsonNull());
            assert (ids.size() <= 50);
            return setBodyParameter("ids", ids);
        }

        @Override
        public RemoveUsersSavedShowsRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/me/shows");
            return new RemoveUsersSavedShowsRequest(this);
        }

        @Override
        protected RemoveUsersSavedShowsRequest.Builder self() {
            return this;
        }
    }
}
