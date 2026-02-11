package me.miki.shindo.libs.spotify.requests.data.player;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = SetRepeatModeOnUsersPlaybackRequest.Builder.class)
public class SetRepeatModeOnUsersPlaybackRequest extends AbstractDataRequest<String> {
    private SetRepeatModeOnUsersPlaybackRequest(final Builder builder) {
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

        public Builder state(final String state) {
            assert (state != null);
            assert (state.equals("track") || state.equals("context") || state.equals("off"));
            return setQueryParameter("state", state);
        }

        public Builder device_id(final String device_id) {
            assert (device_id != null);
            assert (!device_id.isEmpty());
            return setQueryParameter("device_id", device_id);
        }

        @Override
        public SetRepeatModeOnUsersPlaybackRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/me/player/repeat");
            return new SetRepeatModeOnUsersPlaybackRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
