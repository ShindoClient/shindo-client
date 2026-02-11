package me.miki.shindo.libs.spotify.requests.authorization.authorization_code.pkce;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.SpotifyApi;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import me.miki.shindo.libs.spotify.requests.AbstractRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = AuthorizationCodePKCERefreshRequest.Builder.class)
public class AuthorizationCodePKCERefreshRequest extends AbstractRequest<AuthorizationCodeCredentials> {

    private AuthorizationCodePKCERefreshRequest(Builder builder) {
        super(builder);
    }
    public AuthorizationCodeCredentials execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new AuthorizationCodeCredentials.JsonUtil().createModelObject(postJson());
    }
    public static final class Builder extends AbstractRequest.Builder<AuthorizationCodeCredentials, Builder> {

        public Builder() {
            super();
        }
        public Builder grant_type(final String grant_type) {
            assert (grant_type != null);
            assert (grant_type.equals("refresh_token"));
            return setBodyParameter("grant_type", grant_type);
        }
        public Builder refresh_token(final String refresh_token) {
            assert (refresh_token != null);
            assert (!refresh_token.isEmpty());
            return setBodyParameter("refresh_token", refresh_token);
        }
        public Builder client_id(final String client_id) {
            assert (client_id != null);
            assert (!client_id.isEmpty());
            return setBodyParameter("client_id", client_id);
        }
        public AuthorizationCodePKCERefreshRequest build() {
            setContentType(ContentType.APPLICATION_FORM_URLENCODED);
            setHost(SpotifyApi.DEFAULT_AUTHENTICATION_HOST);
            setPort(SpotifyApi.DEFAULT_AUTHENTICATION_PORT);
            setScheme(SpotifyApi.DEFAULT_AUTHENTICATION_SCHEME);
            setPath("/api/token");

            return new AuthorizationCodePKCERefreshRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
