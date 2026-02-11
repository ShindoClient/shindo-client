package me.miki.shindo.libs.spotify.requests.data.browse.miscellaneous;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.List;

@JsonDeserialize(builder = GetAvailableGenreSeedsRequest.Builder.class)
public class GetAvailableGenreSeedsRequest extends AbstractDataRequest<String[]> {
    private GetAvailableGenreSeedsRequest(final Builder builder) {
        super(builder);
    }

    public String[] execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        List<String> genres = new Gson().fromJson(
                JsonParser
                        .parseString(getJson())
                        .getAsJsonObject()
                        .get("genres")
                        .getAsJsonArray(),
                new TypeToken<List<String>>() {
                }.getType()
        );

        return genres.toArray(new String[0]);
    }

    public static final class Builder extends AbstractDataRequest.Builder<String[], Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        @Override
        public GetAvailableGenreSeedsRequest build() {
            setPath("/v1/recommendations/available-genre-seeds");
            return new GetAvailableGenreSeedsRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
