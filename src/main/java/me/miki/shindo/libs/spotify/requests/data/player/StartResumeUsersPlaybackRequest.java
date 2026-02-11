package me.miki.shindo.libs.spotify.requests.data.player;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = StartResumeUsersPlaybackRequest.Builder.class)
public class StartResumeUsersPlaybackRequest extends AbstractDataRequest<String> {
    private StartResumeUsersPlaybackRequest(final Builder builder) {
        super(builder);
    }

    public String execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return putJson();
    }

    public static final class Builder extends AbstractDataRequest.Builder<String, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder device_id(final String device_id) {
            assert (device_id != null);
            assert (!device_id.isEmpty());
            return setQueryParameter("device_id", device_id);
        }

        public Builder context_uri(final String context_uri) {
            assert (context_uri != null);
            assert (!context_uri.isEmpty());
            return setBodyParameter("context_uri", context_uri);
        }

        public Builder uris(final JsonArray uris) {
            assert (uris != null);
            assert (!uris.isJsonNull());
            return setBodyParameter("uris", uris);
        }

        public Builder offset(final JsonObject offset) {
            assert (offset != null);
            assert (!offset.isJsonNull());
            return setBodyParameter("offset", offset);
        }

        public Builder position_ms(final Integer position_ms) {
            assert (position_ms != null);
            assert (position_ms >= 0);
            return setBodyParameter("position_ms", position_ms);
        }

        @Override
        public StartResumeUsersPlaybackRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/me/player/play");
            return new StartResumeUsersPlaybackRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
