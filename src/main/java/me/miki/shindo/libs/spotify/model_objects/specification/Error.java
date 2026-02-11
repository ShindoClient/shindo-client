package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
@JsonDeserialize(builder = Error.Builder.class)
public class Error extends AbstractModelObject {
    private final Integer status;
    private final String message;

    private Error(final Builder builder) {
        super(builder);

        this.status = builder.status;
        this.message = builder.message;
    }
    public Integer getStatus() {
        return status;
    }
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Error(status=" + status + ", message=" + message + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Integer status;
        private String message;
        public Builder setStatus(Integer status) {
            this.status = status;
            return this;
        }
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        @Override
        public Error build() {
            return new Error(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<Error> {
        public Error createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Error.Builder()
                    .setStatus(
                            hasAndNotNull(jsonObject, "status")
                                    ? jsonObject.get("status").getAsInt()
                                    : null)
                    .setMessage(
                            hasAndNotNull(jsonObject, "message")
                                    ? jsonObject.get("message").getAsString()
                                    : null)
                    .build();
        }
    }
}
