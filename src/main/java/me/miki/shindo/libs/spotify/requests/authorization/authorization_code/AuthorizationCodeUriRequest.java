package me.miki.shindo.libs.spotify.requests.authorization.authorization_code;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.SpotifyApi;
import me.miki.shindo.libs.spotify.enums.AuthorizationScope;
import me.miki.shindo.libs.spotify.requests.AbstractRequest;

import java.net.URI;
@JsonDeserialize(builder = AuthorizationCodeUriRequest.Builder.class)
public class AuthorizationCodeUriRequest extends AbstractRequest<URI> {

    private AuthorizationCodeUriRequest(Builder builder) {
        super(builder);
    }
    public URI execute() {
        return this.getUri();
    }
    public static final class Builder extends AbstractRequest.Builder<URI, Builder> {

        public Builder() {
            super();
        }
        public Builder client_id(final String client_id) {
            assert (client_id != null);
            assert (!client_id.isEmpty());
            return setQueryParameter("client_id", client_id);
        }
        public Builder response_type(final String response_type) {
            assert (response_type != null);
            assert (response_type.equals("code"));
            return setQueryParameter("response_type", response_type);
        }
        public Builder redirect_uri(final URI redirect_uri) {
            assert (redirect_uri != null);
            return setQueryParameter("redirect_uri", redirect_uri.toString());
        }
        public Builder code_challenge_method(String code_challenge_method) {
            assert (code_challenge_method != null);
            assert (code_challenge_method.equals("S256"));
            return setQueryParameter("code_challenge_method", code_challenge_method);
        }
        public Builder code_challenge(String code_challenge) {
            assert (code_challenge != null);
            assert (!code_challenge.isEmpty());
            return setQueryParameter("code_challenge", code_challenge);
        }
        public Builder state(final String state) {
            assert (state != null);
            assert (!state.isEmpty());
            return setQueryParameter("state", state);
        }
        public Builder scope(final String scope) {
            assert (scope != null);
            assert (!scope.isEmpty());
            return setQueryParameter("scope", scope);
        }

        public Builder scope(final AuthorizationScope... scopes) {
            StringBuilder finalScopes = new StringBuilder();

            for (AuthorizationScope scope : scopes) {
                finalScopes.append(scope.GetScope()).append(" ");
            }

            return scope(finalScopes.toString().trim());
        }
        public Builder show_dialog(final boolean show_dialog) {
            return setQueryParameter("show_dialog", show_dialog);
        }
        public AuthorizationCodeUriRequest build() {
            setHost(SpotifyApi.DEFAULT_AUTHENTICATION_HOST);
            setPort(SpotifyApi.DEFAULT_AUTHENTICATION_PORT);
            setScheme(SpotifyApi.DEFAULT_AUTHENTICATION_SCHEME);
            setPath("/authorize");

            return new AuthorizationCodeUriRequest(this);
        }

        @Override
        protected AuthorizationCodeUriRequest.Builder self() {
            return this;
        }
    }
}
