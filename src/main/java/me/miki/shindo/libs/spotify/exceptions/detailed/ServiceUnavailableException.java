package me.miki.shindo.libs.spotify.exceptions.detailed;

import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
public class ServiceUnavailableException extends SpotifyWebApiException {

    public ServiceUnavailableException() {
        super();
    }

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

}
