package me.miki.shindo.libs.spotify.requests.authorization;

import me.miki.shindo.libs.spotify.Base64;
import me.miki.shindo.libs.spotify.requests.AbstractRequest;

public abstract class AbstractAuthorizationRequest<T> extends AbstractRequest<T> {
    protected AbstractAuthorizationRequest(final Builder<T, ?> builder) {
        super(builder);
    }

    public static abstract class Builder<T, BT extends Builder<T, ?>> extends AbstractRequest.Builder<T, BT> {
        protected Builder(final String clientId, final String clientSecret) {
            super();

            assert (clientId != null);
            assert (clientSecret != null);
            assert (!clientId.isEmpty());
            assert (!clientSecret.isEmpty());

            setHeader("Authorization", "Basic " + Base64.encode((clientId + ":" + clientSecret).getBytes()));
        }
    }
}
