package me.miki.shindo.libs.spotify.requests.data.library;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = CheckUsersSavedShowsRequest.Builder.class)
public class CheckUsersSavedShowsRequest extends AbstractDataRequest<Boolean[]> {
    private CheckUsersSavedShowsRequest(final Builder builder) {
        super(builder);
    }

    @Override
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

        public CheckUsersSavedShowsRequest.Builder ids(final String ids) {
            assert (ids != null);
            assert (ids.split(",").length <= 50);
            return setQueryParameter("ids", ids);
        }

        @Override
        public CheckUsersSavedShowsRequest build() {
            setPath("/v1/me/shows/contains");
            return new CheckUsersSavedShowsRequest(this);
        }

        @Override
        protected CheckUsersSavedShowsRequest.Builder self() {
            return this;
        }
    }
}
