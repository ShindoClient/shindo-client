package me.miki.shindo.libs.spotify.model_objects.miscellaneous;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
@JsonDeserialize(builder = AudioAnalysisMeta.Builder.class)
public class AudioAnalysisMeta extends AbstractModelObject {
    private final String analyzerVersion;
    private final String platform;
    private final String detailedStatus;
    private final Integer statusCode;
    private final Long timestamp;
    private final Float analysisTime;
    private final String inputProcess;

    private AudioAnalysisMeta(final Builder builder) {
        super(builder);

        this.analyzerVersion = builder.analyzerVersion;
        this.platform = builder.platform;
        this.detailedStatus = builder.detailedStatus;
        this.statusCode = builder.statusCode;
        this.timestamp = builder.timestamp;
        this.analysisTime = builder.analysisTime;
        this.inputProcess = builder.inputProcess;
    }
    public String getAnalyzerVersion() {
        return analyzerVersion;
    }
    public String getPlatform() {
        return platform;
    }
    public String getDetailedStatus() {
        return detailedStatus;
    }
    public Integer getStatusCode() {
        return statusCode;
    }
    public Long getTimestamp() {
        return timestamp;
    }
    public Float getAnalysisTime() {
        return analysisTime;
    }
    public String getInputProcess() {
        return inputProcess;
    }

    @Override
    public String toString() {
        return "AudioAnalysisMeta(analyzerVersion=" + analyzerVersion + ", platform=" + platform + ", detailedStatus="
                + detailedStatus + ", statusCode=" + statusCode + ", timestamp=" + timestamp + ", analysisTime=" + analysisTime
                + ", inputProcess=" + inputProcess + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private String analyzerVersion;
        private String platform;
        private String detailedStatus;
        private Integer statusCode;
        private Long timestamp;
        private Float analysisTime;
        private String inputProcess;
        public Builder setAnalyzerVersion(String analyzerVersion) {
            this.analyzerVersion = analyzerVersion;
            return this;
        }
        public Builder setPlatform(String platform) {
            this.platform = platform;
            return this;
        }
        public Builder setDetailedStatus(String detailedStatus) {
            this.detailedStatus = detailedStatus;
            return this;
        }
        public Builder setStatusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }
        public Builder setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        public Builder setAnalysisTime(Float analysisTime) {
            this.analysisTime = analysisTime;
            return this;
        }
        public Builder setInputProcess(String inputProcess) {
            this.inputProcess = inputProcess;
            return this;
        }

        @Override
        public AudioAnalysisMeta build() {
            return new AudioAnalysisMeta(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<AudioAnalysisMeta> {
        public AudioAnalysisMeta createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new AudioAnalysisMeta.Builder()
                    .setAnalysisTime(
                            hasAndNotNull(jsonObject, "analysis_time")
                                    ? jsonObject.get("analysis_time").getAsFloat()
                                    : null)
                    .setAnalyzerVersion(
                            hasAndNotNull(jsonObject, "analyzer_version")
                                    ? jsonObject.get("analyzer_version").getAsString()
                                    : null)
                    .setDetailedStatus(
                            hasAndNotNull(jsonObject, "detailed_status")
                                    ? jsonObject.get("detailed_status").getAsString()
                                    : null)
                    .setInputProcess(
                            hasAndNotNull(jsonObject, "input_process")
                                    ? jsonObject.get("input_process").getAsString()
                                    : null)
                    .setPlatform(
                            hasAndNotNull(jsonObject, "platform")
                                    ? jsonObject.get("platform").getAsString()
                                    : null)
                    .setStatusCode(
                            hasAndNotNull(jsonObject, "status_code")
                                    ? jsonObject.get("status_code").getAsInt()
                                    : null)
                    .setTimestamp(
                            hasAndNotNull(jsonObject, "timestamp")
                                    ? jsonObject.get("timestamp").getAsLong()
                                    : null)
                    .build();
        }
    }
}
