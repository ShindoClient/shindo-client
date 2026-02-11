package me.miki.shindo.libs.spotify.requests.data.tracks;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.miscellaneous.AudioAnalysis;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetAudioAnalysisForTrackRequest.Builder.class)
public class GetAudioAnalysisForTrackRequest extends AbstractDataRequest<AudioAnalysis> {
    private GetAudioAnalysisForTrackRequest(final Builder builder) {
        super(builder);
    }

    public AudioAnalysis execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new AudioAnalysis.JsonUtil().createModelObject(getJson());
    }

    public static final class Builder extends AbstractDataRequest.Builder<AudioAnalysis, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder id(final String id) {
            assert (id != null);
            assert (!id.isEmpty());
            return setPathParameter("id", id);
        }

        @Override
        public GetAudioAnalysisForTrackRequest build() {
            setPath("/v1/audio-analysis/{id}");
            return new GetAudioAnalysisForTrackRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
