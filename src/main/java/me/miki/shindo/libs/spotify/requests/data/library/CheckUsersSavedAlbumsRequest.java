package me.miki.shindo.libs.spotify.requests.data.library;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = CheckUsersSavedAlbumsRequest.Builder.class)
public class CheckUsersSavedAlbumsRequest extends AbstractDataRequest<Boolean[]> {
    private CheckUsersSavedAlbumsRequest(final Builder builder) {
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
        public CheckUsersSavedAlbumsRequest build() {
            setPath("/v1/me/albums/contains");
            return new CheckUsersSavedAlbumsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
