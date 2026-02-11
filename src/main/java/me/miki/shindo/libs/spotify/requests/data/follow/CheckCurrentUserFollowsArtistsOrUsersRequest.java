package me.miki.shindo.libs.spotify.requests.data.follow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = CheckCurrentUserFollowsArtistsOrUsersRequest.Builder.class)
public class CheckCurrentUserFollowsArtistsOrUsersRequest extends AbstractDataRequest<Boolean[]> {
    private CheckCurrentUserFollowsArtistsOrUsersRequest(final Builder builder) {
        super(builder);
    }

    public Boolean[] execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Gson().fromJson(JsonParser.parseString(getJson()).getAsJsonArray(), Boolean[].class);
    }

    public static final class Builder extends AbstractDataRequest.Builder<Boolean[], Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder type(final ModelObjectType type) {
            assert (type != null);
            assert (type.getType().equals("artist") || type.getType().equals("user"));
            return setQueryParameter("type", type);
        }

        public Builder ids(final String ids) {
            assert (ids != null);
            assert (ids.split(",").length <= 50);
            return setQueryParameter("ids", ids);
        }

        @Override
        public CheckCurrentUserFollowsArtistsOrUsersRequest build() {
            setPath("/v1/me/following/contains");
            return new CheckCurrentUserFollowsArtistsOrUsersRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
