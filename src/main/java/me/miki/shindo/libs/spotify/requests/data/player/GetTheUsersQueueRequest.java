package me.miki.shindo.libs.spotify.requests.data.player;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.special.PlaybackQueue;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetTheUsersQueueRequest.Builder.class)
public class GetTheUsersQueueRequest extends AbstractDataRequest<PlaybackQueue> {
    private GetTheUsersQueueRequest(final Builder builder) {
        super(builder);
    }

    @Override
    public PlaybackQueue execute() throws IOException, SpotifyWebApiException, ParseException {
        return new PlaybackQueue.JsonUtil().createModelObject(getJson());
    }

    public static final class Builder extends AbstractDataRequest.Builder<PlaybackQueue, GetTheUsersQueueRequest.Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        @Override
        public GetTheUsersQueueRequest build() {
            setPath("/v1/me/player/queue");
            return new GetTheUsersQueueRequest(this);
        }

        @Override
        protected GetTheUsersQueueRequest.Builder self() {
            return this;
        }
    }
}
