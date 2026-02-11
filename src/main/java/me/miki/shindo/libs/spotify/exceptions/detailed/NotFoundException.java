package me.miki.shindo.libs.spotify.exceptions.detailed;

import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
public class NotFoundException extends SpotifyWebApiException {

    public NotFoundException() {
        super();
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
