package me.miki.shindo.libs.spotify.requests.data.users_profile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.User;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = GetCurrentUsersProfileRequest.Builder.class)
public class GetCurrentUsersProfileRequest extends AbstractDataRequest<User> {
    private GetCurrentUsersProfileRequest(final Builder builder) {
        super(builder);
    }
    public User execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new User.JsonUtil().createModelObject(getJson());
    }
    public static final class Builder extends AbstractDataRequest.Builder<User, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }
        @Override
        public GetCurrentUsersProfileRequest build() {
            setPath("/v1/me");
            return new GetCurrentUsersProfileRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
