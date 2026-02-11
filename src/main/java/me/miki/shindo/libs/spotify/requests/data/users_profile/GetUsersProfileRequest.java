package me.miki.shindo.libs.spotify.requests.data.users_profile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.User;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = GetUsersProfileRequest.Builder.class)
public class GetUsersProfileRequest extends AbstractDataRequest<User> {
    private GetUsersProfileRequest(final Builder builder) {
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
        public Builder user_id(final String user_id) {
            assert (user_id != null);
            assert (!user_id.isEmpty());
            return setPathParameter("user_id", user_id);
        }
        @Override
        public GetUsersProfileRequest build() {
            setPath("/v1/users/{user_id}");
            return new GetUsersProfileRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
