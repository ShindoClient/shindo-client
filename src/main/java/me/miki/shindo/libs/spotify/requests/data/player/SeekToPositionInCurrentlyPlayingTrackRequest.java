package me.miki.shindo.libs.spotify.requests.data.player;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = SeekToPositionInCurrentlyPlayingTrackRequest.Builder.class)
public class SeekToPositionInCurrentlyPlayingTrackRequest extends AbstractDataRequest<String> {
    private SeekToPositionInCurrentlyPlayingTrackRequest(final Builder builder) {
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

        public Builder position_ms(final Integer position_ms) {
            assert (position_ms != null);
            assert (position_ms >= 0);
            return setQueryParameter("position_ms", position_ms);
        }

        public Builder device_id(final String device_id) {
            assert (device_id != null);
            assert (!device_id.isEmpty());
            return setQueryParameter("device_id", device_id);
        }

        @Override
        public SeekToPositionInCurrentlyPlayingTrackRequest build() {
            setContentType(ContentType.APPLICATION_JSON);
            setPath("/v1/me/player/seek");
            return new SeekToPositionInCurrentlyPlayingTrackRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
