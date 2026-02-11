package me.miki.shindo.libs.spotify.requests.data.library;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = RemoveAlbumsForCurrentUserRequest.Builder.class)
public class RemoveAlbumsForCurrentUserRequest extends AbstractDataRequest<String> {
    private RemoveAlbumsForCurrentUserRequest(final Builder builder) {
        super(builder);
    }

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

        public Builder ids(final JsonArray ids) {
            assert (ids != null);
            assert (!ids.isJsonNull());
            assert (ids.size() <= 50);
            return setBodyParameter("ids", ids);
        }

        @Override
        public RemoveAlbumsForCurrentUserRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/me/albums");
            return new RemoveAlbumsForCurrentUserRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
