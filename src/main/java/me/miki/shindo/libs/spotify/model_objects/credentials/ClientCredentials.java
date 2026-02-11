package me.miki.shindo.libs.spotify.model_objects.credentials;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.util.Objects;
@JsonDeserialize(builder = ClientCredentials.Builder.class)
public class ClientCredentials extends AbstractModelObject {
    private final String accessToken;
    private final String tokenType;
    private final Integer expiresIn;

    private ClientCredentials(final Builder builder) {
        super(builder);

        this.accessToken = builder.accessToken;
        this.tokenType = builder.tokenType;
        this.expiresIn = builder.expiresIn;
    }
    public String getAccessToken() {
        return accessToken;
    }
    public String getTokenType() {
        return tokenType;
    }
    public Integer getExpiresIn() {
        return expiresIn;
    }

    @Override
    public String toString() {
        return "ClientCredentials(accessToken=" + accessToken + ", tokenType=" + tokenType + ", expiresIn=" + expiresIn
                + ")";
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
        ClientCredentials that = (ClientCredentials) o;
        return Objects.equals(accessToken, that.accessToken) && Objects.equals(tokenType, that.tokenType) &&
                Objects.equals(expiresIn, that.expiresIn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, tokenType, expiresIn);
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private String accessToken;
        private String tokenType;
        private Integer expiresIn;
        public Builder setAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }
        public Builder setTokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }
        public Builder setExpiresIn(Integer expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        @Override
        public ClientCredentials build() {
            return new ClientCredentials(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<ClientCredentials> {
        public ClientCredentials createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new ClientCredentials.Builder()
                    .setAccessToken(
                            hasAndNotNull(jsonObject, "access_token")
                                    ? jsonObject.get("access_token").getAsString()
                                    : null)
                    .setTokenType(
                            hasAndNotNull(jsonObject, "token_type")
                                    ? jsonObject.get("token_type").getAsString()
                                    : null)
                    .setExpiresIn(
                            hasAndNotNull(jsonObject, "expires_in")
                                    ? jsonObject.get("expires_in").getAsInt()
                                    : null)
                    .build();
        }
    }
}
