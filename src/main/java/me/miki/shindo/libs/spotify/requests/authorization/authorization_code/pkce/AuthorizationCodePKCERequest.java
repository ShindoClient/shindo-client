package me.miki.shindo.libs.spotify.requests.authorization.authorization_code.pkce;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.SpotifyApi;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import me.miki.shindo.libs.spotify.requests.AbstractRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.URI;
@JsonDeserialize(builder = AuthorizationCodePKCERequest.Builder.class)
public class AuthorizationCodePKCERequest extends AbstractRequest<AuthorizationCodeCredentials> {

    private AuthorizationCodePKCERequest(Builder builder) {
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
        public Builder client_id(final String client_id) {
            assert (client_id != null);
            assert (!client_id.isEmpty());
            return setBodyParameter("client_id", client_id);
        }
        public Builder grant_type(final String grant_type) {
            assert (grant_type != null);
            assert (grant_type.equals("authorization_code"));
            return setBodyParameter("grant_type", grant_type);
        }
        public Builder code(final String code) {
            assert (code != null);
            assert (!code.isEmpty());
            return setBodyParameter("code", code);
        }
        public Builder redirect_uri(final URI redirect_uri) {
            assert (redirect_uri != null);
            return setBodyParameter("redirect_uri", redirect_uri.toString());
        }
        public Builder code_verifier(String code_verifier) {
            assert (code_verifier != null);
            assert (!code_verifier.isEmpty());
            return setBodyParameter("code_verifier", code_verifier);
        }
        public AuthorizationCodePKCERequest build() {
            setContentType(ContentType.APPLICATION_FORM_URLENCODED);
            setHost(SpotifyApi.DEFAULT_AUTHENTICATION_HOST);
            setPort(SpotifyApi.DEFAULT_AUTHENTICATION_PORT);
            setScheme(SpotifyApi.DEFAULT_AUTHENTICATION_SCHEME);
            setPath("/api/token");

            return new AuthorizationCodePKCERequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

}
