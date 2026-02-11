package me.miki.shindo.libs.spotify.requests.data.player;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = AddItemToUsersPlaybackQueueRequest.Builder.class)
public class AddItemToUsersPlaybackQueueRequest extends AbstractDataRequest<String> {
    private AddItemToUsersPlaybackQueueRequest(final Builder builder) {
        super(builder);
    }

    @Override
    public String execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return postJson();
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

        public Builder uri(final String uri) {
            assert (uri != null);
            assert (!uri.isEmpty());
            return setQueryParameter("uri", uri);
        }

        @Override
        public AddItemToUsersPlaybackQueueRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/me/player/queue");
            return new AddItemToUsersPlaybackQueueRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

}
