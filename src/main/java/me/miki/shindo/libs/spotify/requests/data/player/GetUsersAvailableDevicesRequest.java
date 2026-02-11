package me.miki.shindo.libs.spotify.requests.data.player;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.miscellaneous.Device;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetUsersAvailableDevicesRequest.Builder.class)
public class GetUsersAvailableDevicesRequest extends AbstractDataRequest<Device[]> {
    private GetUsersAvailableDevicesRequest(final Builder builder) {
        super(builder);
    }

    public Device[] execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Device.JsonUtil().createModelObjectArray(getJson(), "devices");
    }

    public static final class Builder extends AbstractDataRequest.Builder<Device[], Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        @Override
        public GetUsersAvailableDevicesRequest build() {
            setPath("/v1/me/player/devices");
            return new GetUsersAvailableDevicesRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
