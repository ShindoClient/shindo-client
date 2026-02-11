package me.miki.shindo.libs.spotify.requests.data.library;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = CheckUsersSavedTracksRequest.Builder.class)
public class CheckUsersSavedTracksRequest extends AbstractDataRequest<Boolean[]> {
    private CheckUsersSavedTracksRequest(final Builder builder) {
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

        public Builder ids(final String ids) {
            assert (ids != null);
            assert (ids.split(",").length <= 50);
            return setQueryParameter("ids", ids);
        }

        @Override
        public CheckUsersSavedTracksRequest build() {
            setPath("/v1/me/tracks/contains");
            return new CheckUsersSavedTracksRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
