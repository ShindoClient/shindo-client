package me.miki.shindo.libs.spotify.exceptions;

import org.apache.hc.core5.http.HttpException;
public class SpotifyWebApiException extends HttpException {

    public SpotifyWebApiException() {
        super();
    }

    public SpotifyWebApiException(String message) {
        super(message);
    }

    public SpotifyWebApiException(String message, Throwable cause) {
        super(message, cause);
    }

}
