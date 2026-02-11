package me.miki.shindo.libs.spotify.requests.data.library;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = SaveShowsForCurrentUserRequest.Builder.class)
public class SaveShowsForCurrentUserRequest extends AbstractDataRequest<String> {
    private SaveShowsForCurrentUserRequest(final Builder builder) {
        super(builder);
    }

    @Override
    public String execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return putJson();
    }

    public static final class Builder extends AbstractDataRequest.Builder<String, Builder> {
        public Builder(String accessToken) {
            super(accessToken);
        }

        public Builder ids(final String ids) {
            assert (ids != null);
            assert (ids.split(",").length <= 50);
            return setQueryParameter("ids", ids);
        }

        public Builder ids(final JsonArray ids) {
            assert (ids != null);
            assert (!ids.isJsonNull());
            assert (ids.size() <= 50);
            return setBodyParameter("ids", ids);
        }

        @Override
        public SaveShowsForCurrentUserRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/me/shows");
            return new SaveShowsForCurrentUserRequest(this);
        }

        @Override
        protected SaveShowsForCurrentUserRequest.Builder self() {
            return this;
        }
    }
}
