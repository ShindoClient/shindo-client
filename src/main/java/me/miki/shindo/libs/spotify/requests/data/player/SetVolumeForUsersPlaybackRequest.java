package me.miki.shindo.libs.spotify.requests.data.player;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = SetVolumeForUsersPlaybackRequest.Builder.class)
public class SetVolumeForUsersPlaybackRequest extends AbstractDataRequest<String> {
    private SetVolumeForUsersPlaybackRequest(final Builder builder) {
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

        public Builder volume_percent(final Integer volume_percent) {
            assert (volume_percent != null);
            assert (0 <= volume_percent && volume_percent <= 100);
            return setQueryParameter("volume_percent", volume_percent);
        }

        public Builder device_id(final String device_id) {
            assert (device_id != null);
            assert (!device_id.isEmpty());
            return setQueryParameter("device_id", device_id);
        }

        @Override
        public SetVolumeForUsersPlaybackRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/me/player/volume");
            return new SetVolumeForUsersPlaybackRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
