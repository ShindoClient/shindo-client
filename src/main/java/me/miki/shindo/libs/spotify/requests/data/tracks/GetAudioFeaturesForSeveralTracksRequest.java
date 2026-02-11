package me.miki.shindo.libs.spotify.requests.data.tracks;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.AudioFeatures;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetAudioFeaturesForSeveralTracksRequest.Builder.class)
public class GetAudioFeaturesForSeveralTracksRequest extends AbstractDataRequest<AudioFeatures[]> {
    private GetAudioFeaturesForSeveralTracksRequest(final Builder builder) {
        super(builder);
    }

    public AudioFeatures[] execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new AudioFeatures.JsonUtil().createModelObjectArray(getJson(), "audio_features");
    }

    public static final class Builder extends AbstractDataRequest.Builder<AudioFeatures[], Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder ids(final String ids) {
            assert (ids != null);
            assert (ids.split(",").length <= 100);
            return setQueryParameter("ids", ids);
        }

        @Override
        public GetAudioFeaturesForSeveralTracksRequest build() {
            setPath("/v1/audio-features");
            return new GetAudioFeaturesForSeveralTracksRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
