package me.miki.shindo.libs.spotify.exceptions.detailed;

import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
public class UnauthorizedException extends SpotifyWebApiException {

    public UnauthorizedException() {
        super();
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

}
