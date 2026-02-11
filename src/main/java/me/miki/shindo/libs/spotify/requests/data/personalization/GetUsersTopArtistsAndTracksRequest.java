package me.miki.shindo.libs.spotify.requests.data.personalization;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
import me.miki.shindo.libs.spotify.model_objects.specification.Artist;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.model_objects.specification.Track;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import me.miki.shindo.libs.spotify.requests.data.personalization.interfaces.IArtistTrackModelObject;
import me.miki.shindo.libs.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;
import me.miki.shindo.libs.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = GetUsersTopArtistsAndTracksRequest.Builder.class)
public class GetUsersTopArtistsAndTracksRequest<T extends IArtistTrackModelObject> extends AbstractDataRequest<Paging<T>> {

    private final AbstractModelObject.JsonUtil<T> tClass;
    private GetUsersTopArtistsAndTracksRequest(final Builder<T> builder, final AbstractModelObject.JsonUtil<T> tClass) {
        super(builder);
        this.tClass = tClass;
    }
    public Paging<T> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return tClass.createModelObjectPaging(getJson());
    }
    public static final class Builder<T extends IArtistTrackModelObject> extends AbstractDataPagingRequest.Builder<T, Builder<T>> {

        private AbstractModelObject.JsonUtil<T> tClass;
        public Builder(final String accessToken) {
            super(accessToken);
        }
        @SuppressWarnings("unchecked")
        public Builder<T> type(final ModelObjectType type) {
            assert (type != null);
            assert (type.getType().equals("artists") || type.getType().equals("tracks"));

            switch (type.getType()) {
                case "artists":
                    tClass = (AbstractModelObject.JsonUtil<T>) new Artist.JsonUtil();
                    break;
                case "tracks":
                    tClass = (AbstractModelObject.JsonUtil<T>) new Track.JsonUtil();
                    break;
            }

            return setPathParameter("type", type.getType());
        }
        @Override
        public Builder<T> limit(final Integer limit) {
            assert (limit != null);
            assert (1 <= limit && limit <= 50);
            return setQueryParameter("limit", limit);
        }
        @Override
        public Builder<T> offset(final Integer offset) {
            assert (offset >= 0);
            return setQueryParameter("offset", offset);
        }
        public Builder<T> time_range(final String time_range) {
            assert (time_range != null);
            assert (time_range.equals("long_term") || time_range.equals("medium_term") || time_range.equals("short_term"));
            return setQueryParameter("time_range", time_range);
        }
        @Override
        public GetUsersTopArtistsAndTracksRequest<T> build() {
            setPath("/v1/me/top/{type}");
            return new GetUsersTopArtistsAndTracksRequest<>(this, tClass);
        }

        @Override
        protected Builder<T> self() {
            return this;
        }
    }
}
