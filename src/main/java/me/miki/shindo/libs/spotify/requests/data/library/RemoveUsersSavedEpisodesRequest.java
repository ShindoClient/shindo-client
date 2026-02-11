package me.miki.shindo.libs.spotify.requests.data.library;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = RemoveUsersSavedEpisodesRequest.Builder.class)
public class RemoveUsersSavedEpisodesRequest extends AbstractDataRequest<String> {
    private RemoveUsersSavedEpisodesRequest(final RemoveUsersSavedEpisodesRequest.Builder builder) {
        super(builder);
    }

    @Override
    public String execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return deleteJson();
    }

    public static final class Builder extends AbstractDataRequest.Builder<String, RemoveUsersSavedEpisodesRequest.Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public RemoveUsersSavedEpisodesRequest.Builder ids(final String ids) {
            assert (ids != null);
            assert (ids.split(",").length <= 50);
            return setQueryParameter("ids", ids);
        }

        public RemoveUsersSavedEpisodesRequest.Builder ids(final JsonArray ids) {
            assert (ids != null);
            assert (!ids.isJsonNull());
            assert (ids.size() <= 50);
            return setBodyParameter("ids", ids);
        }

        @Override
        public RemoveUsersSavedEpisodesRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/me/episodes");
            return new RemoveUsersSavedEpisodesRequest(this);
        }

        @Override
        protected RemoveUsersSavedEpisodesRequest.Builder self() {
            return this;
        }
    }
}
