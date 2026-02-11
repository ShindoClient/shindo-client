package me.miki.shindo.libs.spotify.model_objects.credentials.error;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.util.Objects;
@JsonDeserialize(builder = AuthenticationError.Builder.class)
public class AuthenticationError extends AbstractModelObject {
    private final String error;
    private final String error_description;

    private AuthenticationError(final Builder builder) {
        super(builder);

        this.error = builder.error;
        this.error_description = builder.error_description;
    }
    public String getError() {
        return error;
    }
    public String getError_description() {
        return error_description;
    }

    @Override
    public String toString() {
        return "AuthenticationError(error=" + error + ", error_description=" + error_description + ")";
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
        AuthenticationError that = (AuthenticationError) o;
        return Objects.equals(error, that.error) && Objects.equals(error_description, that.error_description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error, error_description);
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private String error;
        private String error_description;
        public Builder setError(String error) {
            this.error = error;
            return this;
        }
        public Builder setError_description(String error_description) {
            this.error_description = error_description;
            return this;
        }

        @Override
        public AuthenticationError build() {
            return new AuthenticationError(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<AuthenticationError> {
        public AuthenticationError createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new AuthenticationError.Builder()
                    .setError(
                            hasAndNotNull(jsonObject, "error")
                                    ? jsonObject.get("error").getAsString()
                                    : null)
                    .setError_description(
                            hasAndNotNull(jsonObject, "error_description")
                                    ? jsonObject.get("error_description").getAsString()
                                    : null)
                    .build();
        }
    }
}
