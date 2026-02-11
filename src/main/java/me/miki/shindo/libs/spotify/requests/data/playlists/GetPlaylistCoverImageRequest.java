package me.miki.shindo.libs.spotify.requests.data.playlists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Image;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
@JsonDeserialize(builder = GetPlaylistCoverImageRequest.Builder.class)
public class GetPlaylistCoverImageRequest extends AbstractDataRequest<Image[]> {
    private GetPlaylistCoverImageRequest(final Builder builder) {
        super(builder);
    }
    public Image[] execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Image.JsonUtil().createModelObjectArray(getJson());
    }
    public static final class Builder extends AbstractDataRequest.Builder<Image[], Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }
        public Builder playlist_id(final String playlist_id) {
            assert (playlist_id != null);
            assert (!playlist_id.isEmpty());
            return setPathParameter("playlist_id", playlist_id);
        }
        @Override
        public GetPlaylistCoverImageRequest build() {
            setPath("/v1/playlists/{playlist_id}/images");
            return new GetPlaylistCoverImageRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
