package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.enums.Modality;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
import me.miki.shindo.libs.spotify.model_objects.miscellaneous.AudioAnalysis;

import java.util.Objects;
@JsonDeserialize(builder = AudioFeatures.Builder.class)
public class AudioFeatures extends AbstractModelObject {
    private final Float acousticness;
    private final String analysisUrl;
    private final Float danceability;
    private final Integer durationMs;
    private final Float energy;
    private final String id;
    private final Float instrumentalness;
    private final Integer key;
    private final Float liveness;
    private final Float loudness;
    private final Modality mode;
    private final Float speechiness;
    private final Float tempo;
    private final Integer timeSignature;
    private final String trackHref;
    private final ModelObjectType type;
    private final String uri;
    private final Float valence;

    private AudioFeatures(final Builder builder) {
        super(builder);

        this.acousticness = builder.acousticness;
        this.analysisUrl = builder.analysisUrl;
        this.danceability = builder.danceability;
        this.durationMs = builder.durationMs;
        this.energy = builder.energy;
        this.id = builder.id;
        this.instrumentalness = builder.instrumentalness;
        this.key = builder.key;
        this.liveness = builder.liveness;
        this.loudness = builder.loudness;
        this.mode = builder.mode;
        this.speechiness = builder.speechiness;
        this.tempo = builder.tempo;
        this.timeSignature = builder.timeSignature;
        this.trackHref = builder.trackHref;
        this.type = builder.type;
        this.uri = builder.uri;
        this.valence = builder.valence;
    }
    public Float getAcousticness() {
        return acousticness;
    }
    public String getAnalysisUrl() {
        return analysisUrl;
    }
    public Float getDanceability() {
        return danceability;
    }
    public Integer getDurationMs() {
        return durationMs;
    }
    public Float getEnergy() {
        return energy;
    }
    public String getId() {
        return id;
    }
    public Float getInstrumentalness() {
        return instrumentalness;
    }
    public Integer getKey() {
        return key;
    }
    public Float getLiveness() {
        return liveness;
    }
    public Float getLoudness() {
        return loudness;
    }
    public Modality getMode() {
        return mode;
    }
    public Float getSpeechiness() {
        return speechiness;
    }
    public Float getTempo() {
        return tempo;
    }
    public Integer getTimeSignature() {
        return timeSignature;
    }
    public String getTrackHref() {
        return trackHref;
    }
    public ModelObjectType getType() {
        return type;
    }
    public String getUri() {
        return uri;
    }
    public Float getValence() {
        return valence;
    }

    @Override
    public String toString() {
        return "AudioFeatures(acousticness=" + acousticness + ", analysisUrl=" + analysisUrl + ", danceability="
                + danceability + ", durationMs=" + durationMs + ", energy=" + energy + ", id=" + id + ", instrumentalness="
                + instrumentalness + ", key=" + key + ", liveness=" + liveness + ", loudness=" + loudness + ", mode=" + mode
                + ", speechiness=" + speechiness + ", tempo=" + tempo + ", timeSignature=" + timeSignature + ", trackHref="
                + trackHref + ", type=" + type + ", uri=" + uri + ", valence=" + valence + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AudioFeatures that = (AudioFeatures) o;
        return Objects.equals(analysisUrl, that.analysisUrl) && Objects.equals(id, that.id) &&
                Objects.equals(key, that.key) && Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(analysisUrl, id, key, uri);
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Float acousticness;
        private String analysisUrl;
        private Float danceability;
        private Integer durationMs;
        private Float energy;
        private String id;
        private Float instrumentalness;
        private Integer key;
        private Float liveness;
        private Float loudness;
        private Modality mode;
        private Float speechiness;
        private Float tempo;
        private Integer timeSignature;
        private String trackHref;
        private ModelObjectType type;
        private String uri;
        private Float valence;
        public Builder setAcousticness(Float acousticness) {
            this.acousticness = acousticness;
            return this;
        }
        public Builder setAnalysisUrl(String analysisUrl) {
            this.analysisUrl = analysisUrl;
            return this;
        }
        public Builder setDanceability(Float danceability) {
            this.danceability = danceability;
            return this;
        }
        public Builder setDurationMs(Integer durationMs) {
            this.durationMs = durationMs;
            return this;
        }
        public Builder setEnergy(Float energy) {
            this.energy = energy;
            return this;
        }
        public Builder setId(String id) {
            this.id = id;
            return this;
        }
        public Builder setInstrumentalness(Float instrumentalness) {
            this.instrumentalness = instrumentalness;
            return this;
        }
        public Builder setKey(Integer key) {
            this.key = key;
            return this;
        }
        public Builder setLiveness(Float liveness) {
            this.liveness = liveness;
            return this;
        }
        public Builder setLoudness(Float loudness) {
            this.loudness = loudness;
            return this;
        }
        public Builder setMode(Modality mode) {
            this.mode = mode;
            return this;
        }
        public Builder setSpeechiness(Float speechiness) {
            this.speechiness = speechiness;
            return this;
        }
        public Builder setTempo(Float tempo) {
            this.tempo = tempo;
            return this;
        }
        public Builder setTimeSignature(Integer timeSignature) {
            this.timeSignature = timeSignature;
            return this;
        }
        public Builder setTrackHref(String trackHref) {
            this.trackHref = trackHref;
            return this;
        }
        public Builder setType(ModelObjectType type) {
            this.type = type;
            return this;
        }
        public Builder setUri(String uri) {
            this.uri = uri;
            return this;
        }
        public Builder setValence(Float valence) {
            this.valence = valence;
            return this;
        }

        @Override
        public AudioFeatures build() {
            return new AudioFeatures(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<AudioFeatures> {
        public AudioFeatures createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new AudioFeatures.Builder()
                    .setAcousticness(
                            hasAndNotNull(jsonObject, "acousticness")
                                    ? jsonObject.get("acousticness").getAsFloat()
                                    : null)
                    .setAnalysisUrl(
                            hasAndNotNull(jsonObject, "analysis_url")
                                    ? jsonObject.get("analysis_url").getAsString()
                                    : null)
                    .setDanceability(
                            hasAndNotNull(jsonObject, "danceability")
                                    ? jsonObject.get("danceability").getAsFloat()
                                    : null)
                    .setDurationMs(
                            hasAndNotNull(jsonObject, "duration_ms")
                                    ? jsonObject.get("duration_ms").getAsInt()
                                    : null)
                    .setEnergy(
                            hasAndNotNull(jsonObject, "energy")
                                    ? jsonObject.get("energy").getAsFloat()
                                    : null)
                    .setId(
                            hasAndNotNull(jsonObject, "id")
                                    ? jsonObject.get("id").getAsString()
                                    : null)
                    .setInstrumentalness(
                            hasAndNotNull(jsonObject, "instrumentalness")
                                    ? jsonObject.get("instrumentalness").getAsFloat()
                                    : null)
                    .setKey(
                            hasAndNotNull(jsonObject, "key")
                                    ? jsonObject.get("key").getAsInt()
                                    : null)
                    .setLiveness(
                            hasAndNotNull(jsonObject, "liveness")
                                    ? jsonObject.get("liveness").getAsFloat()
                                    : null)
                    .setLoudness(
                            hasAndNotNull(jsonObject, "loudness")
                                    ? jsonObject.get("loudness").getAsFloat()
                                    : null)
                    .setMode(
                            hasAndNotNull(jsonObject, "mode")
                                    ? Modality.keyOf(
                                    jsonObject.get("mode").getAsInt())
                                    : null)
                    .setSpeechiness(
                            hasAndNotNull(jsonObject, "speechiness")
                                    ? jsonObject.get("speechiness").getAsFloat()
                                    : null)
                    .setTempo(
                            hasAndNotNull(jsonObject, "tempo")
                                    ? jsonObject.get("tempo").getAsFloat()
                                    : null)
                    .setTimeSignature(
                            hasAndNotNull(jsonObject, "time_signature")
                                    ? jsonObject.get("time_signature").getAsInt()
                                    : null)
                    .setTrackHref(
                            hasAndNotNull(jsonObject, "track_href")
                                    ? jsonObject.get("track_href").getAsString()
                                    : null)
                    .setType(
                            hasAndNotNull(jsonObject, "type")
                                    ? ModelObjectType.keyOf(
                                    jsonObject.get("type").getAsString().toLowerCase())
                                    : null)
                    .setUri(
                            hasAndNotNull(jsonObject, "uri")
                                    ? jsonObject.get("uri").getAsString()
                                    : null)
                    .setValence(
                            hasAndNotNull(jsonObject, "valence")
                                    ? jsonObject.get("valence").getAsFloat()
                                    : null)
                    .build();
        }
    }
}
