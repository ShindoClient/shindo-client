package me.miki.shindo.libs.spotify.requests.data.playlists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
@JsonDeserialize(builder = UploadCustomPlaylistCoverImageRequest.Builder.class)
public class UploadCustomPlaylistCoverImageRequest extends AbstractDataRequest<String> {
    private UploadCustomPlaylistCoverImageRequest(final Builder builder) {
        super(builder);
    }
    public String execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return putJson();
    }
    public static final class Builder extends AbstractDataRequest.Builder<String, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }
        public Builder playlist_id(final String playlist_id) {
            assert (playlist_id != null);
            assert (!playlist_id.isEmpty());
            return setPathParameter("playlist_id", playlist_id);
        }
        public Builder image_data(final String image_data) {
            assert (image_data != null);
            assert (!image_data.isEmpty());
            assert (image_data.getBytes().length <= 256000);
            return setBody(new StringEntity(image_data, ContentType.IMAGE_JPEG));
        }
        @Override
        public UploadCustomPlaylistCoverImageRequest build() {
            setContentType(ContentType.IMAGE_JPEG);
            setPath("/v1/playlists/{playlist_id}/images");
            return new UploadCustomPlaylistCoverImageRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
