package me.miki.shindo.libs.spotify.requests.data.player;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = TransferUsersPlaybackRequest.Builder.class)
public class TransferUsersPlaybackRequest extends AbstractDataRequest<String> {
    private TransferUsersPlaybackRequest(final Builder builder) {
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

        public Builder device_ids(final JsonArray device_ids) {
            assert (device_ids != null);
            assert (!device_ids.isJsonNull());
            assert (device_ids.size() == 1);
            return setBodyParameter("device_ids", device_ids);
        }

        public Builder play(final Boolean play) {
            return setBodyParameter("play", play);
        }

        @Override
        public TransferUsersPlaybackRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/me/player");
            return new TransferUsersPlaybackRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
