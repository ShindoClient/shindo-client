package me.miki.shindo.libs.spotify.requests.data.episodes;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Episode;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetEpisodeRequest.Builder.class)
public class GetEpisodeRequest extends AbstractDataRequest<Episode> {
    private GetEpisodeRequest(final Builder builder) {
        super(builder);
    }

    @Override
    public Episode execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Episode.JsonUtil().createModelObject(getJson());
    }

    public static final class Builder extends AbstractDataRequest.Builder<Episode, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder id(final String id) {
            assert (id != null);
            assert (!id.isEmpty());
            return setPathParameter("id", id);
        }

        public Builder market(final CountryCode market) {
            assert (market != null);
            return setQueryParameter("market", market);
        }

        @Override
        public GetEpisodeRequest build() {
            setPath("/v1/episodes/{id}");
            return new GetEpisodeRequest(this);
        }

        @Override
        protected GetEpisodeRequest.Builder self() {
            return this;
        }
    }
}
