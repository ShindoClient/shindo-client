package me.miki.shindo.libs.spotify.exceptions.detailed;

import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
public class InternalServerErrorException extends SpotifyWebApiException {

    public InternalServerErrorException() {
        super();
    }

    public InternalServerErrorException(String message) {
        super(message);
    }

    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }

}
