package me.miki.shindo.libs.spotify.model_objects.credentials;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.util.Objects;
@JsonDeserialize(builder = AuthorizationCodeCredentials.Builder.class)
public class AuthorizationCodeCredentials extends AbstractModelObject {
    private final String accessToken;
    private final String tokenType;
    private final String scope;
    private final Integer expiresIn;
    private final String refreshToken;

    private AuthorizationCodeCredentials(final Builder builder) {
        super(builder);

        this.accessToken = builder.accessToken;
        this.tokenType = builder.tokenType;
        this.scope = builder.scope;
        this.expiresIn = builder.expiresIn;
        this.refreshToken = builder.refreshToken;
    }
    public String getAccessToken() {
        return accessToken;
    }
    public String getTokenType() {
        return tokenType;
    }
    public String getScope() {
        return scope;
    }
    public Integer getExpiresIn() {
        return expiresIn;
    }
    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public String toString() {
        return "AuthorizationCodeCredentials(accessToken=" + accessToken + ", tokenType=" + tokenType + ", scope=" + scope
                + ", expiresIn=" + expiresIn + ", refreshToken=" + refreshToken + ")";
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
        AuthorizationCodeCredentials that = (AuthorizationCodeCredentials) o;
        return Objects.equals(accessToken, that.accessToken) && Objects.equals(tokenType, that.tokenType) &&
                Objects.equals(scope, that.scope) && Objects.equals(expiresIn, that.expiresIn) &&
                Objects.equals(refreshToken, that.refreshToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, tokenType, scope, expiresIn, refreshToken);
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private String accessToken;
        private String tokenType;
        private String scope;
        private Integer expiresIn;
        private String refreshToken;
        public Builder setAccessToken(final String accessToken) {
            this.accessToken = accessToken;
            return this;
        }
        public Builder setTokenType(final String tokenType) {
            this.tokenType = tokenType;
            return this;
        }
        public Builder setScope(final String scope) {
            this.scope = scope;
            return this;
        }
        public Builder setExpiresIn(final Integer expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }
        public Builder setRefreshToken(final String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        @Override
        public AuthorizationCodeCredentials build() {
            return new AuthorizationCodeCredentials(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<AuthorizationCodeCredentials> {
        public AuthorizationCodeCredentials createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new AuthorizationCodeCredentials.Builder()
                    .setAccessToken(
                            hasAndNotNull(jsonObject, "access_token")
                                    ? jsonObject.get("access_token").getAsString()
                                    : null)
                    .setTokenType(
                            hasAndNotNull(jsonObject, "token_type")
                                    ? jsonObject.get("token_type").getAsString()
                                    : null)
                    .setScope(
                            hasAndNotNull(jsonObject, "scope")
                                    ? jsonObject.get("scope").getAsString()
                                    : null)
                    .setExpiresIn(
                            hasAndNotNull(jsonObject, "expires_in")
                                    ? jsonObject.get("expires_in").getAsInt()
                                    : null)
                    .setRefreshToken(
                            hasAndNotNull(jsonObject, "refresh_token")
                                    ? jsonObject.get("refresh_token").getAsString()
                                    : null)
                    .build();
        }
    }
}
