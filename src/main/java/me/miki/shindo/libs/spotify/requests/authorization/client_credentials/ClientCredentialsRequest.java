package me.miki.shindo.libs.spotify.requests.authorization.client_credentials;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.SpotifyApi;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.credentials.ClientCredentials;
import me.miki.shindo.libs.spotify.requests.authorization.AbstractAuthorizationRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = ClientCredentialsRequest.Builder.class)
public class ClientCredentialsRequest extends AbstractAuthorizationRequest<ClientCredentials> {

    public ClientCredentialsRequest(Builder builder) {
        super(builder);
    }
    public ClientCredentials execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new ClientCredentials.JsonUtil().createModelObject(postJson());
    }
    public static final class Builder extends AbstractAuthorizationRequest.Builder<ClientCredentials, Builder> {

        public Builder(final String clientId, final String clientSecret) {
            super(clientId, clientSecret);
        }
        public Builder grant_type(final String grant_type) {
            assert (grant_type != null);
            assert (grant_type.equals("client_credentials"));
            return setBodyParameter("grant_type", grant_type);
        }
        public ClientCredentialsRequest build() {
            setContentType(ContentType.APPLICATION_FORM_URLENCODED);
            setHost(SpotifyApi.DEFAULT_AUTHENTICATION_HOST);
            setPort(SpotifyApi.DEFAULT_AUTHENTICATION_PORT);
            setScheme(SpotifyApi.DEFAULT_AUTHENTICATION_SCHEME);
            setPath("/api/token");

            return new ClientCredentialsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
