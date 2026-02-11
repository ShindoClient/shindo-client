package me.miki.shindo.libs.spotify;

import com.google.gson.JsonArray;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.model_objects.specification.Artist;
import me.miki.shindo.libs.spotify.model_objects.specification.Track;
import me.miki.shindo.libs.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import me.miki.shindo.libs.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import me.miki.shindo.libs.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import me.miki.shindo.libs.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERefreshRequest;
import me.miki.shindo.libs.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERequest;
import me.miki.shindo.libs.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import me.miki.shindo.libs.spotify.requests.data.albums.GetAlbumRequest;
import me.miki.shindo.libs.spotify.requests.data.albums.GetAlbumsTracksRequest;
import me.miki.shindo.libs.spotify.requests.data.albums.GetSeveralAlbumsRequest;
import me.miki.shindo.libs.spotify.requests.data.artists.*;
import me.miki.shindo.libs.spotify.requests.data.browse.*;
import me.miki.shindo.libs.spotify.requests.data.browse.miscellaneous.GetAvailableGenreSeedsRequest;
import me.miki.shindo.libs.spotify.requests.data.episodes.GetEpisodeRequest;
import me.miki.shindo.libs.spotify.requests.data.episodes.GetSeveralEpisodesRequest;
import me.miki.shindo.libs.spotify.requests.data.follow.*;
import me.miki.shindo.libs.spotify.requests.data.follow.legacy.FollowPlaylistRequest;
import me.miki.shindo.libs.spotify.requests.data.follow.legacy.UnfollowPlaylistRequest;
import me.miki.shindo.libs.spotify.requests.data.library.*;
import me.miki.shindo.libs.spotify.requests.data.personalization.GetUsersTopArtistsAndTracksRequest;
import me.miki.shindo.libs.spotify.requests.data.personalization.interfaces.IArtistTrackModelObject;
import me.miki.shindo.libs.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;
import me.miki.shindo.libs.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;
import me.miki.shindo.libs.spotify.requests.data.player.*;
import me.miki.shindo.libs.spotify.requests.data.playlists.*;
import me.miki.shindo.libs.spotify.requests.data.search.SearchItemRequest;
import me.miki.shindo.libs.spotify.requests.data.search.simplified.*;
import me.miki.shindo.libs.spotify.requests.data.search.simplified.special.SearchAlbumsSpecialRequest;
import me.miki.shindo.libs.spotify.requests.data.shows.GetSeveralShowsRequest;
import me.miki.shindo.libs.spotify.requests.data.shows.GetShowRequest;
import me.miki.shindo.libs.spotify.requests.data.shows.GetShowsEpisodesRequest;
import me.miki.shindo.libs.spotify.requests.data.tracks.*;
import me.miki.shindo.libs.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import me.miki.shindo.libs.spotify.requests.data.users_profile.GetUsersProfileRequest;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;
public class SpotifyApi {
    public static final String DEFAULT_AUTHENTICATION_HOST = "accounts.spotify.com";
    public static final int DEFAULT_AUTHENTICATION_PORT = 443;
    public static final String DEFAULT_AUTHENTICATION_SCHEME = "https";
    public static final String DEFAULT_HOST = "api.spotify.com";
    public static final IHttpManager DEFAULT_HTTP_MANAGER = new SpotifyHttpManager.Builder().build();
    public static final int DEFAULT_PORT = 443;
    public static final String DEFAULT_SCHEME = "https";

    public static final Logger LOGGER = Logger.getLogger(SpotifyApi.class.getName());
    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = ThreadLocal.withInitial(() -> makeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", "GMT"));

    private final IHttpManager httpManager;
    private final String scheme;
    private final String host;
    private final Integer port;
    private final String proxyUrl;
    private final Integer proxyPort;
    private final Integer proxyUsername;
    private final Integer proxyPassword;
    private final String clientId;
    private final String clientSecret;
    private final URI redirectUri;
    private String accessToken;
    private String refreshToken;

    private SpotifyApi(Builder builder) {
        assert (builder.httpManager != null);

        this.httpManager = builder.httpManager;
        this.scheme = builder.scheme;
        this.host = builder.host;
        this.port = builder.port;
        this.proxyUrl = builder.proxyUrl;
        this.proxyPort = builder.proxyPort;
        this.proxyUsername = builder.proxyUsername;
        this.proxyPassword = builder.proxyPassword;
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.redirectUri = builder.redirectUri;
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
    }
    public static Builder builder() {
        return new Builder();
    }
    public static String concat(String[] parts, char character) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String part : parts) {
            stringBuilder.append(part).append(character);
        }

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        return stringBuilder.toString();
    }
    public static Date parseDefaultDate(String date) throws ParseException {
        return SIMPLE_DATE_FORMAT.get().parse(date);
    }
    public static String formatDefaultDate(Date date) {
        return SIMPLE_DATE_FORMAT.get().format(date);
    }

    public static SimpleDateFormat makeSimpleDateFormat(String pattern, String id) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(id));

        return simpleDateFormat;
    }
    public IHttpManager getHttpManager() {
        return httpManager;
    }
    public String getScheme() {
        return scheme;
    }
    public String getHost() {
        return host;
    }
    public Integer getPort() {
        return port;
    }
    public String getProxyUrl() {
        return proxyUrl;
    }
    public Integer getProxyPort() {
        return proxyPort;
    }
    public Integer getProxyUsername() {
        return proxyUsername;
    }
    public Integer getProxyPassword() {
        return proxyPassword;
    }
    public String getClientId() {
        return clientId;
    }
    public String getClientSecret() {
        return clientSecret;
    }
    public URI getRedirectURI() {
        return redirectUri;
    }
    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public AuthorizationCodeRefreshRequest.Builder authorizationCodeRefresh(String client_id, String client_secret, String refresh_token) {
        return new AuthorizationCodeRefreshRequest.Builder(client_id, client_secret)
                .setDefaults(httpManager, scheme, host, port)
                .grant_type("refresh_token")
                .refresh_token(refresh_token);
    }
    public AuthorizationCodeRefreshRequest.Builder authorizationCodeRefresh() {
        return new AuthorizationCodeRefreshRequest.Builder(clientId, clientSecret)
                .setDefaults(httpManager, scheme, host, port)
                .grant_type("refresh_token")
                .refresh_token(refreshToken);
    }
    public AuthorizationCodePKCERefreshRequest.Builder authorizationCodePKCERefresh(String client_id, String refresh_token) {
        return new AuthorizationCodePKCERefreshRequest.Builder()
                .setDefaults(httpManager, scheme, host, port)
                .client_id(client_id)
                .grant_type("refresh_token")
                .refresh_token(refresh_token);
    }
    public AuthorizationCodePKCERefreshRequest.Builder authorizationCodePKCERefresh() {
        return new AuthorizationCodePKCERefreshRequest.Builder()
                .setDefaults(httpManager, scheme, host, port)
                .client_id(clientId)
                .grant_type("refresh_token")
                .refresh_token(refreshToken);
    }
    public AuthorizationCodeRequest.Builder authorizationCode(String client_id, String client_secret, String code, URI redirect_uri) {
        return new AuthorizationCodeRequest.Builder(client_id, client_secret)
                .setDefaults(httpManager, scheme, host, port)
                .grant_type("authorization_code")
                .code(code)
                .redirect_uri(redirect_uri);
    }
    public AuthorizationCodeRequest.Builder authorizationCode(String code) {
        return new AuthorizationCodeRequest.Builder(clientId, clientSecret)
                .setDefaults(httpManager, scheme, host, port)
                .grant_type("authorization_code")
                .code(code)
                .redirect_uri(redirectUri);
    }
    public AuthorizationCodePKCERequest.Builder authorizationCodePKCE(String client_id, String code, String code_verifier, URI redirect_uri) {
        return new AuthorizationCodePKCERequest.Builder()
                .setDefaults(httpManager, scheme, host, port)
                .client_id(client_id)
                .code_verifier(code_verifier)
                .grant_type("authorization_code")
                .code(code)
                .redirect_uri(redirect_uri);
    }
    public AuthorizationCodePKCERequest.Builder authorizationCodePKCE(String code, String code_verifier) {
        return new AuthorizationCodePKCERequest.Builder()
                .setDefaults(httpManager, scheme, host, port)
                .client_id(clientId)
                .code_verifier(code_verifier)
                .grant_type("authorization_code")
                .code(code)
                .redirect_uri(redirectUri);
    }
    public AuthorizationCodeUriRequest.Builder authorizationCodeUri(String client_id, URI redirect_uri) {
        return new AuthorizationCodeUriRequest.Builder()
                .setDefaults(httpManager, scheme, host, port)
                .client_id(client_id)
                .response_type("code")
                .redirect_uri(redirect_uri);
    }
    public AuthorizationCodeUriRequest.Builder authorizationCodeUri() {
        return new AuthorizationCodeUriRequest.Builder()
                .setDefaults(httpManager, scheme, host, port)
                .client_id(clientId)
                .response_type("code")
                .redirect_uri(redirectUri);
    }
    public AuthorizationCodeUriRequest.Builder authorizationCodePKCEUri(String client_id, String code_challenge, URI redirect_uri) {
        return new AuthorizationCodeUriRequest.Builder()
                .setDefaults(httpManager, scheme, host, port)
                .client_id(client_id)
                .response_type("code")
                .code_challenge_method("S256")
                .code_challenge(code_challenge)
                .redirect_uri(redirect_uri);
    }
    public AuthorizationCodeUriRequest.Builder authorizationCodePKCEUri(String code_challenge) {
        return new AuthorizationCodeUriRequest.Builder()
                .setDefaults(httpManager, scheme, host, port)
                .client_id(clientId)
                .response_type("code")
                .code_challenge_method("S256")
                .code_challenge(code_challenge)
                .redirect_uri(redirectUri);
    }
    public ClientCredentialsRequest.Builder clientCredentials() {
        return new ClientCredentialsRequest.Builder(clientId, clientSecret)
                .setDefaults(httpManager, scheme, host, port)
                .grant_type("client_credentials");
    }
    public GetAlbumRequest.Builder getAlbum(String id) {
        return new GetAlbumRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .id(id);
    }
    public GetAlbumsTracksRequest.Builder getAlbumsTracks(String id) {
        return new GetAlbumsTracksRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .id(id);
    }
    public GetSeveralAlbumsRequest.Builder getSeveralAlbums(String... ids) {
        return new GetSeveralAlbumsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public GetArtistRequest.Builder getArtist(String id) {
        return new GetArtistRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .id(id);
    }
    public GetArtistsAlbumsRequest.Builder getArtistsAlbums(String id) {
        return new GetArtistsAlbumsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .id(id);
    }
    public GetArtistsTopTracksRequest.Builder getArtistsTopTracks(String id, CountryCode country) {
        return new GetArtistsTopTracksRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .id(id)
                .country(country);
    }
    public GetArtistsRelatedArtistsRequest.Builder getArtistsRelatedArtists(String id) {
        return new GetArtistsRelatedArtistsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .id(id);
    }
    public GetSeveralArtistsRequest.Builder getSeveralArtists(String... ids) {
        return new GetSeveralArtistsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public GetCategoryRequest.Builder getCategory(String category_id) {
        return new GetCategoryRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .category_id(category_id);
    }
    public GetCategorysPlaylistsRequest.Builder getCategorysPlaylists(String category_id) {
        return new GetCategorysPlaylistsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .category_id(category_id);
    }
    public GetListOfCategoriesRequest.Builder getListOfCategories() {
        return new GetListOfCategoriesRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public GetListOfFeaturedPlaylistsRequest.Builder getListOfFeaturedPlaylists() {
        return new GetListOfFeaturedPlaylistsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public GetListOfNewReleasesRequest.Builder getListOfNewReleases() {
        return new GetListOfNewReleasesRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public GetRecommendationsRequest.Builder getRecommendations() {
        return new GetRecommendationsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public GetAvailableGenreSeedsRequest.Builder getAvailableGenreSeeds() {
        return new GetAvailableGenreSeedsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public GetEpisodeRequest.Builder getEpisode(String id) {
        return new GetEpisodeRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .id(id);
    }
    public GetSeveralEpisodesRequest.Builder getSeveralEpisodes(String... ids) {
        return new GetSeveralEpisodesRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public CheckCurrentUserFollowsArtistsOrUsersRequest.Builder checkCurrentUserFollowsArtistsOrUsers(
            ModelObjectType type, String[] ids) {
        return new CheckCurrentUserFollowsArtistsOrUsersRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .type(type)
                .ids(concat(ids, ','));
    }
    @Deprecated()
    public CheckUsersFollowPlaylistRequest.Builder checkUsersFollowPlaylist(
            String owner_id, String playlist_id, String[] ids) {
        return new CheckUsersFollowPlaylistRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .owner_id(owner_id)
                .playlist_id(playlist_id)
                .ids(concat(ids, ','));
    }
    public CheckUsersFollowPlaylistRequest.Builder checkUsersFollowPlaylist(
            String playlist_id, String[] ids) {
        return new CheckUsersFollowPlaylistRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .playlist_id(playlist_id)
                .ids(concat(ids, ','));
    }
    public FollowArtistsOrUsersRequest.Builder followArtistsOrUsers(ModelObjectType type, String[] ids) {
        return new FollowArtistsOrUsersRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .type(type)
                .ids(concat(ids, ','));
    }
    public FollowArtistsOrUsersRequest.Builder followArtistsOrUsers(ModelObjectType type, JsonArray ids) {
        return new FollowArtistsOrUsersRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .type(type)
                .ids(ids);
    }
    public FollowPlaylistRequest.Builder followPlaylist(String owner_id, String playlist_id, boolean public_) {
        return new FollowPlaylistRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .owner_id(owner_id)
                .playlist_id(playlist_id)
                .public_(public_);
    }
    public me.miki.shindo.libs.spotify.requests.data.follow.FollowPlaylistRequest.Builder followPlaylist(String playlist_id, boolean public_) {
        return new me.miki.shindo.libs.spotify.requests.data.follow.FollowPlaylistRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .playlist_id(playlist_id)
                .public_(public_);
    }
    public GetUsersFollowedArtistsRequest.Builder getUsersFollowedArtists(ModelObjectType type) {
        return new GetUsersFollowedArtistsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .type(type);
    }
    public UnfollowArtistsOrUsersRequest.Builder unfollowArtistsOrUsers(ModelObjectType type, String[] ids) {
        return new UnfollowArtistsOrUsersRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .type(type)
                .ids(concat(ids, ','));
    }
    public UnfollowArtistsOrUsersRequest.Builder unfollowArtistsOrUsers(ModelObjectType type, JsonArray ids) {
        return new UnfollowArtistsOrUsersRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .type(type)
                .ids(ids);
    }
    public UnfollowPlaylistRequest.Builder unfollowPlaylist(String owner_id, String playlist_id) {
        return new UnfollowPlaylistRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .owner_id(owner_id)
                .playlist_id(playlist_id);
    }
    public me.miki.shindo.libs.spotify.requests.data.follow.UnfollowPlaylistRequest.Builder unfollowPlaylist(String playlist_id) {
        return new me.miki.shindo.libs.spotify.requests.data.follow.UnfollowPlaylistRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .playlist_id(playlist_id);
    }
    public CheckUsersSavedAlbumsRequest.Builder checkUsersSavedAlbums(String... ids) {
        return new CheckUsersSavedAlbumsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public CheckUsersSavedShowsRequest.Builder checkUsersSavedShows(String... ids) {
        return new CheckUsersSavedShowsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public CheckUsersSavedEpisodesRequest.Builder checkUsersSavedEpisodes(String... ids) {
        return new CheckUsersSavedEpisodesRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public CheckUsersSavedTracksRequest.Builder checkUsersSavedTracks(String... ids) {
        return new CheckUsersSavedTracksRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public GetCurrentUsersSavedAlbumsRequest.Builder getCurrentUsersSavedAlbums() {
        return new GetCurrentUsersSavedAlbumsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public GetUsersSavedShowsRequest.Builder getUsersSavedShows() {
        return new GetUsersSavedShowsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public GetUsersSavedEpisodesRequest.Builder getUsersSavedEpisodes() {
        return new GetUsersSavedEpisodesRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public GetUsersSavedTracksRequest.Builder getUsersSavedTracks() {
        return new GetUsersSavedTracksRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public RemoveAlbumsForCurrentUserRequest.Builder removeAlbumsForCurrentUser(String... ids) {
        return new RemoveAlbumsForCurrentUserRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public RemoveAlbumsForCurrentUserRequest.Builder removeAlbumsForCurrentUser(JsonArray ids) {
        return new RemoveAlbumsForCurrentUserRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(ids);
    }
    public RemoveUsersSavedShowsRequest.Builder removeUsersSavedShows(String... ids) {
        return new RemoveUsersSavedShowsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public RemoveUsersSavedShowsRequest.Builder removeUsersSavedShows(JsonArray ids) {
        return new RemoveUsersSavedShowsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(ids);
    }
    public RemoveUsersSavedEpisodesRequest.Builder removeUsersSavedEpisodes(String... ids) {
        return new RemoveUsersSavedEpisodesRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public RemoveUsersSavedEpisodesRequest.Builder removeUsersSavedEpisodes(JsonArray ids) {
        return new RemoveUsersSavedEpisodesRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(ids);
    }
    public RemoveUsersSavedTracksRequest.Builder removeUsersSavedTracks(String... ids) {
        return new RemoveUsersSavedTracksRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public RemoveUsersSavedTracksRequest.Builder removeUsersSavedTracks(JsonArray ids) {
        return new RemoveUsersSavedTracksRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(ids);
    }
    public SaveAlbumsForCurrentUserRequest.Builder saveAlbumsForCurrentUser(String... ids) {
        return new SaveAlbumsForCurrentUserRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public SaveAlbumsForCurrentUserRequest.Builder saveAlbumsForCurrentUser(JsonArray ids) {
        return new SaveAlbumsForCurrentUserRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(ids);
    }
    public SaveShowsForCurrentUserRequest.Builder saveShowsForCurrentUser(String... ids) {
        return new SaveShowsForCurrentUserRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public SaveShowsForCurrentUserRequest.Builder saveShowsForCurrentUser(JsonArray ids) {
        return new SaveShowsForCurrentUserRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(ids);
    }
    public SaveEpisodesForCurrentUserRequest.Builder saveEpisodesForCurrentUser(String... ids) {
        return new SaveEpisodesForCurrentUserRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public SaveEpisodesForCurrentUserRequest.Builder saveEpisodesForCurrentUser(JsonArray ids) {
        return new SaveEpisodesForCurrentUserRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(ids);
    }
    public SaveTracksForUserRequest.Builder saveTracksForUser(String... ids) {
        return new SaveTracksForUserRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public SaveTracksForUserRequest.Builder saveTracksForUser(JsonArray ids) {
        return new SaveTracksForUserRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(ids);
    }
    public <T extends IArtistTrackModelObject> GetUsersTopArtistsAndTracksRequest.Builder<T> getUsersTopArtistsAndTracks(ModelObjectType type) {
        return new GetUsersTopArtistsAndTracksRequest.Builder<T>(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .type(type);
    }
    public GetUsersTopArtistsRequest.Builder getUsersTopArtists() {
        return new GetUsersTopArtistsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public GetUsersTopTracksRequest.Builder getUsersTopTracks() {
        return new GetUsersTopTracksRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public GetInformationAboutUsersCurrentPlaybackRequest.Builder getInformationAboutUsersCurrentPlayback() {
        return new GetInformationAboutUsersCurrentPlaybackRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public GetCurrentUsersRecentlyPlayedTracksRequest.Builder getCurrentUsersRecentlyPlayedTracks() {
        return new GetCurrentUsersRecentlyPlayedTracksRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public GetUsersAvailableDevicesRequest.Builder getUsersAvailableDevices() {
        return new GetUsersAvailableDevicesRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public GetUsersCurrentlyPlayingTrackRequest.Builder getUsersCurrentlyPlayingTrack() {
        return new GetUsersCurrentlyPlayingTrackRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public PauseUsersPlaybackRequest.Builder pauseUsersPlayback() {
        return new PauseUsersPlaybackRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public SeekToPositionInCurrentlyPlayingTrackRequest.Builder seekToPositionInCurrentlyPlayingTrack(int position_ms) {
        return new SeekToPositionInCurrentlyPlayingTrackRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .position_ms(position_ms);
    }
    public SetRepeatModeOnUsersPlaybackRequest.Builder setRepeatModeOnUsersPlayback(String state) {
        return new SetRepeatModeOnUsersPlaybackRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .state(state);
    }
    public SetVolumeForUsersPlaybackRequest.Builder setVolumeForUsersPlayback(int volume_percent) {
        return new SetVolumeForUsersPlaybackRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .volume_percent(volume_percent);
    }
    public SkipUsersPlaybackToNextTrackRequest.Builder skipUsersPlaybackToNextTrack() {
        return new SkipUsersPlaybackToNextTrackRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public SkipUsersPlaybackToPreviousTrackRequest.Builder skipUsersPlaybackToPreviousTrack() {
        return new SkipUsersPlaybackToPreviousTrackRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public StartResumeUsersPlaybackRequest.Builder startResumeUsersPlayback() {
        return new StartResumeUsersPlaybackRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public ToggleShuffleForUsersPlaybackRequest.Builder toggleShuffleForUsersPlayback(boolean state) {
        return new ToggleShuffleForUsersPlaybackRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .state(state);
    }
    public TransferUsersPlaybackRequest.Builder transferUsersPlayback(JsonArray device_ids) {
        return new TransferUsersPlaybackRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .device_ids(device_ids);
    }
    public AddItemToUsersPlaybackQueueRequest.Builder addItemToUsersPlaybackQueue(String uri) {
        return new AddItemToUsersPlaybackQueueRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .uri(uri);
    }
    public GetTheUsersQueueRequest.Builder getTheUsersQueue() {
        return new GetTheUsersQueueRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public AddItemsToPlaylistRequest.Builder addItemsToPlaylist(String playlist_id, String[] uris) {
        return new AddItemsToPlaylistRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .playlist_id(playlist_id)
                .uris(concat(uris, ','));
    }
    public AddItemsToPlaylistRequest.Builder addItemsToPlaylist(String playlist_id, JsonArray uris) {
        return new AddItemsToPlaylistRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .playlist_id(playlist_id)
                .uris(uris);
    }
    public ChangePlaylistsDetailsRequest.Builder changePlaylistsDetails(String playlist_id) {
        return new ChangePlaylistsDetailsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .playlist_id(playlist_id);
    }
    public CreatePlaylistRequest.Builder createPlaylist(String user_id, String name) {
        return new CreatePlaylistRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .user_id(user_id)
                .name(name);
    }
    public GetListOfCurrentUsersPlaylistsRequest.Builder getListOfCurrentUsersPlaylists() {
        return new GetListOfCurrentUsersPlaylistsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public GetListOfUsersPlaylistsRequest.Builder getListOfUsersPlaylists(String user_id) {
        return new GetListOfUsersPlaylistsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .user_id(user_id);
    }
    public GetPlaylistRequest.Builder getPlaylist(String playlist_id) {
        return new GetPlaylistRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .playlist_id(playlist_id);
    }
    public GetPlaylistCoverImageRequest.Builder getPlaylistCoverImage(String playlist_id) {
        return new GetPlaylistCoverImageRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .playlist_id(playlist_id);
    }
    public GetPlaylistsItemsRequest.Builder getPlaylistsItems(String playlist_id) {
        return new GetPlaylistsItemsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .playlist_id(playlist_id);
    }
    public RemoveItemsFromPlaylistRequest.Builder removeItemsFromPlaylist(
            String playlist_id, JsonArray tracks) {
        return new RemoveItemsFromPlaylistRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .playlist_id(playlist_id)
                .tracks(tracks);
    }
    public ReorderPlaylistsItemsRequest.Builder reorderPlaylistsItems(String playlist_id, int range_start, int insert_before) {
        return new ReorderPlaylistsItemsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .playlist_id(playlist_id)
                .range_start(range_start)
                .insert_before(insert_before);
    }
    public ReplacePlaylistsItemsRequest.Builder replacePlaylistsItems(String playlist_id, String[] uris) {
        return new ReplacePlaylistsItemsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .playlist_id(playlist_id)
                .uris(concat(uris, ','));
    }
    public ReplacePlaylistsItemsRequest.Builder replacePlaylistsItems(String playlist_id, JsonArray uris) {
        return new ReplacePlaylistsItemsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .playlist_id(playlist_id)
                .uris(uris);
    }
    public UploadCustomPlaylistCoverImageRequest.Builder uploadCustomPlaylistCoverImage(String playlist_id) {
        return new UploadCustomPlaylistCoverImageRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .playlist_id(playlist_id);
    }
    public SearchItemRequest.Builder searchItem(String q, String type) {
        return new SearchItemRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .q(q)
                .type(type);
    }
    public SearchAlbumsRequest.Builder searchAlbums(String q) {
        return new SearchAlbumsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .q(q);
    }
    public SearchAlbumsSpecialRequest.Builder searchAlbumsSpecial(String q) {
        return new SearchAlbumsSpecialRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .q(q);
    }
    public SearchArtistsRequest.Builder searchArtists(String q) {
        return new SearchArtistsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .q(q);
    }
    public SearchEpisodesRequest.Builder searchEpisodes(String q) {
        return new SearchEpisodesRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .q(q);
    }
    public SearchPlaylistsRequest.Builder searchPlaylists(String q) {
        return new SearchPlaylistsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .q(q);
    }
    public SearchShowsRequest.Builder searchShows(String q) {
        return new SearchShowsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .q(q);
    }
    public SearchTracksRequest.Builder searchTracks(String q) {
        return new SearchTracksRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .q(q);
    }
    public GetShowRequest.Builder getShow(String id) {
        return new GetShowRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .id(id);
    }
    public GetSeveralShowsRequest.Builder getSeveralShows(String... ids) {
        return new GetSeveralShowsRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public GetShowsEpisodesRequest.Builder getShowEpisodes(String id) {
        return new GetShowsEpisodesRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .id(id);
    }
    public GetAudioAnalysisForTrackRequest.Builder getAudioAnalysisForTrack(String id) {
        return new GetAudioAnalysisForTrackRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .id(id);
    }
    public GetAudioFeaturesForTrackRequest.Builder getAudioFeaturesForTrack(String id) {
        return new GetAudioFeaturesForTrackRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .id(id);
    }
    public GetAudioFeaturesForSeveralTracksRequest.Builder getAudioFeaturesForSeveralTracks(String... ids) {
        return new GetAudioFeaturesForSeveralTracksRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public GetSeveralTracksRequest.Builder getSeveralTracks(String... ids) {
        return new GetSeveralTracksRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .ids(concat(ids, ','));
    }
    public GetTrackRequest.Builder getTrack(String id) {
        return new GetTrackRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .id(id);
    }
    public GetCurrentUsersProfileRequest.Builder getCurrentUsersProfile() {
        return new GetCurrentUsersProfileRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port);
    }
    public GetUsersProfileRequest.Builder getUsersProfile(String user_id) {
        return new GetUsersProfileRequest.Builder(accessToken)
                .setDefaults(httpManager, scheme, host, port)
                .user_id(user_id);
    }
    public static class Builder {

        private IHttpManager httpManager = DEFAULT_HTTP_MANAGER;
        private String scheme = DEFAULT_SCHEME;
        private String host = DEFAULT_HOST;
        private Integer port = DEFAULT_PORT;
        private String proxyUrl;
        private Integer proxyPort;
        private Integer proxyUsername;
        private Integer proxyPassword;
        private String clientId;
        private String clientSecret;
        private URI redirectUri;
        private String accessToken;
        private String refreshToken;
        public Builder setHttpManager(IHttpManager httpManager) {
            this.httpManager = httpManager;
            return this;
        }
        public Builder setScheme(String scheme) {
            this.scheme = scheme;
            return this;
        }
        public Builder setHost(String host) {
            this.host = host;
            return this;
        }
        public Builder setPort(Integer port) {
            this.port = port;
            return this;
        }
        public Builder setProxyUrl(String proxyUrl) {
            this.proxyUrl = proxyUrl;
            return this;
        }
        public Builder setProxyPort(Integer proxyPort) {
            this.proxyPort = proxyPort;
            return this;
        }
        public Builder setProxyUsername(Integer proxyUsername) {
            this.proxyUsername = proxyUsername;
            return this;
        }
        public Builder setProxyPassword(Integer proxyPassword) {
            this.proxyPassword = proxyPassword;
            return this;
        }
        public Builder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }
        public Builder setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }
        public Builder setRedirectUri(URI redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }
        public Builder setAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }
        public Builder setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }
        public SpotifyApi build() {
            return new SpotifyApi(this);
        }
    }
}

