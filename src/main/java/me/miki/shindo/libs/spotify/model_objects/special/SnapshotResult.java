package me.miki.shindo.libs.spotify.model_objects.special;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
@JsonDeserialize(builder = SnapshotResult.Builder.class)
public class SnapshotResult extends AbstractModelObject {
    private final String snapshotId;

    private SnapshotResult(final Builder builder) {
        super(builder);

        this.snapshotId = builder.snapshotId;
    }
    public String getSnapshotId() {
        return snapshotId;
    }

    @Override
    public String toString() {
        return "SnapshotResult(snapshotId=" + snapshotId + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        public String snapshotId;

        public Builder setSnapshotId(String snapshotId) {
            this.snapshotId = snapshotId;
            return this;
        }

        @Override
        public SnapshotResult build() {
            return new SnapshotResult(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<SnapshotResult> {
        public SnapshotResult createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new SnapshotResult.Builder()
                    .setSnapshotId(
                            hasAndNotNull(jsonObject, "snapshot_id")
                                    ? jsonObject.get("snapshot_id").getAsString()
                                    : null)
                    .build();
        }
    }
}
