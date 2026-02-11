package me.miki.shindo.libs.spotify;

import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.URI;
public interface IHttpManager {
    String get(URI uri, Header[] headers) throws
            IOException,
            SpotifyWebApiException,
            ParseException;
    String post(URI uri, Header[] headers, HttpEntity body) throws
            IOException,
            SpotifyWebApiException,
            ParseException;
    String put(URI uri, Header[] headers, HttpEntity body) throws
            IOException,
            SpotifyWebApiException,
            ParseException;
    String delete(URI uri, Header[] headers, HttpEntity body) throws
            IOException,
            SpotifyWebApiException,
            ParseException;

}
