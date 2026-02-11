package me.miki.shindo.libs.spotify.requests.data.library;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = SaveEpisodesForCurrentUserRequest.Builder.class)
public class SaveEpisodesForCurrentUserRequest extends AbstractDataRequest<String> {
    private SaveEpisodesForCurrentUserRequest(final SaveEpisodesForCurrentUserRequest.Builder builder) {
        super(builder);
    }

    @Override
    public String execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return putJson();
    }

    public static final class Builder extends AbstractDataRequest.Builder<String, SaveEpisodesForCurrentUserRequest.Builder> {
        public Builder(String accessToken) {
            super(accessToken);
        }

        public SaveEpisodesForCurrentUserRequest.Builder ids(final String ids) {
            assert (ids != null);
            assert (ids.split(",").length <= 50);
            return setQueryParameter("ids", ids);
        }

        public SaveEpisodesForCurrentUserRequest.Builder ids(final JsonArray ids) {
            assert (ids != null);
            assert (!ids.isJsonNull());
            assert (ids.size() <= 50);
            return setBodyParameter("ids", ids);
        }

        @Override
        public SaveEpisodesForCurrentUserRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/me/episodes");
            return new SaveEpisodesForCurrentUserRequest(this);
        }

        @Override
        protected SaveEpisodesForCurrentUserRequest.Builder self() {
            return this;
        }
    }

}
