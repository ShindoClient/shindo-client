package me.miki.shindo.libs.spotify.model_objects.miscellaneous;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
@JsonDeserialize(builder = Restrictions.Builder.class)
public class Restrictions extends AbstractModelObject {
    private final String reason;

    private Restrictions(final Builder builder) {
        super(builder);

        this.reason = builder.reason;
    }
    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "Restrictions(reason=" + reason + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private String reason;
        public Builder setReason(String reason) {
            this.reason = reason;
            return this;
        }

        @Override
        public Restrictions build() {
            return new Restrictions(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<Restrictions> {
        public Restrictions createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Restrictions.Builder()
                    .setReason(
                            hasAndNotNull(jsonObject, "reason")
                                    ? jsonObject.get("reason").getAsString()
                                    : null)
                    .build();
        }
    }
}
