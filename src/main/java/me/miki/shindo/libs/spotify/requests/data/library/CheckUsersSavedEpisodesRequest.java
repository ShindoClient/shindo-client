package me.miki.shindo.libs.spotify.requests.data.library;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = CheckUsersSavedEpisodesRequest.Builder.class)
public class CheckUsersSavedEpisodesRequest extends AbstractDataRequest<Boolean[]> {
    private CheckUsersSavedEpisodesRequest(final CheckUsersSavedEpisodesRequest.Builder builder) {
        super(builder);
    }

    @Override
    public Boolean[] execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Gson().fromJson(JsonParser.parseString(getJson()).getAsJsonArray(), Boolean[].class);
    }

    public static final class Builder extends AbstractDataRequest.Builder<Boolean[], CheckUsersSavedEpisodesRequest.Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public CheckUsersSavedEpisodesRequest.Builder ids(final String ids) {
            assert (ids != null);
            assert (ids.split(",").length <= 50);
            return setQueryParameter("ids", ids);
        }

        @Override
        public CheckUsersSavedEpisodesRequest build() {
            setPath("/v1/me/episodes/contains");
            return new CheckUsersSavedEpisodesRequest(this);
        }

        @Override
        protected CheckUsersSavedEpisodesRequest.Builder self() {
            return this;
        }
    }
}
