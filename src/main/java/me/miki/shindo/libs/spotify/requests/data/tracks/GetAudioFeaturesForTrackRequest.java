package me.miki.shindo.libs.spotify.requests.data.tracks;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.AudioFeatures;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetAudioFeaturesForTrackRequest.Builder.class)
public class GetAudioFeaturesForTrackRequest extends AbstractDataRequest<AudioFeatures> {
    private GetAudioFeaturesForTrackRequest(final Builder builder) {
        super(builder);
    }

    public AudioFeatures execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new AudioFeatures.JsonUtil().createModelObject(getJson());
    }

    public static final class Builder extends AbstractDataRequest.Builder<AudioFeatures, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder id(final String id) {
            assert (id != null);
            assert (!id.isEmpty());
            return setPathParameter("id", id);
        }

        @Override
        public GetAudioFeaturesForTrackRequest build() {
            setPath("/v1/audio-features/{id}");
            return new GetAudioFeaturesForTrackRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
