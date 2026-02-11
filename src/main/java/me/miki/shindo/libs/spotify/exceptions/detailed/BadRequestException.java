package me.miki.shindo.libs.spotify.exceptions.detailed;

import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
public class BadRequestException extends SpotifyWebApiException {

    public BadRequestException() {
        super();
    }

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

}
