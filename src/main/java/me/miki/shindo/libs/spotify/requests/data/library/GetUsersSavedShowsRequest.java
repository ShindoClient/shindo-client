package me.miki.shindo.libs.spotify.requests.data.library;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.model_objects.specification.SavedShow;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetUsersSavedShowsRequest.Builder.class)
public class GetUsersSavedShowsRequest extends AbstractDataRequest<Paging<SavedShow>> {
    private GetUsersSavedShowsRequest(Builder builder) {
        super(builder);
    }

    @Override
    public Paging<SavedShow> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new SavedShow.JsonUtil().createModelObjectPaging(getJson());
    }

    public static final class Builder extends AbstractDataPagingRequest.Builder<SavedShow, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        @Override
        public Builder limit(final Integer limit) {
            assert (1 <= limit && limit <= 50);
            return setQueryParameter("limit", limit);
        }

        @Override
        public Builder offset(final Integer offset) {
            assert (offset >= 0);
            return setQueryParameter("offset", offset);
        }

        @Override
        public GetUsersSavedShowsRequest build() {
            setPath("/v1/me/shows");
            return new GetUsersSavedShowsRequest(this);
        }

        @Override
        protected GetUsersSavedShowsRequest.Builder self() {
            return this;
        }
    }
}
