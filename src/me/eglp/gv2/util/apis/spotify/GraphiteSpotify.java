package me.eglp.gv2.util.apis.spotify;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.ParseException;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteSetupException;
import me.mrletsplay.mrcore.misc.FriendlyException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Track;

public class GraphiteSpotify {
	
	public static final Pattern
		TRACK_LINK_PATTERN = Pattern.compile("http(s)?://open\\.spotify\\.com/track/(?<id>[a-zA-Z0-9]+)(\\?.+)?"),
		ALBUM_LINK_PATTERN = Pattern.compile("http(s)?://open\\.spotify\\.com/album/(?<id>[a-zA-Z0-9]+)(\\?.+)?"),
		PLAYLIST_LINK_PATTERN = Pattern.compile("http(s)?://open\\.spotify\\.com/playlist/(?<id>[a-zA-Z0-9]+)(\\?.+)?");
	
	private SpotifyApi spotify;
	
	public GraphiteSpotify() {
		
		try {
			spotify = SpotifyApi.builder()
					.setClientId(Graphite.getMainBotInfo().getSpotify().getClientID())
					.setClientSecret(Graphite.getMainBotInfo().getSpotify().getClientSecret())
					.build();
			refreshCredentials();
		}catch(Exception e) {
			throw new GraphiteSetupException("Failed to create Spotify API, check credentials", e);
		}
		
		Graphite.getScheduler().scheduleAtFixedRate("spotify-refresh", () -> {
			refreshCredentials();
		}, 3000 * 1000);
	}
	
	public String getTrackISRC(String trackID) {
		try {
			return spotify.getTrack(trackID).build().execute().getExternalIds().getExternalIds().get("isrc");
		} catch (ParseException | SpotifyWebApiException | IOException e) {
			return null;
		}
	}
	
	public List<String> getAlbumISRC(String albumID) {
		try {
			return Arrays.stream(spotify.getSeveralTracks(Arrays.stream(spotify.getAlbum(albumID).build().execute().getTracks().getItems())
					.map(t -> t.getId())
					.toArray(String[]::new)).build().execute())
					.map(t -> t.getExternalIds().getExternalIds().get("isrc"))
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		} catch (ParseException | SpotifyWebApiException | IOException e) {
			return null;
		}
	}
	
	public List<String> getPlaylistISRC(String playlistID) {
		try {
			return Arrays.stream(spotify.getPlaylist(playlistID).build().execute().getTracks().getItems())
					.filter(t -> !t.getIsLocal() && t.getTrack() instanceof Track)
					.map(t -> ((Track) t.getTrack()).getExternalIds().getExternalIds().get("isrc"))
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		} catch (ParseException | SpotifyWebApiException | IOException e) {
			return null;
		}
	}
	
	private void refreshCredentials() {
		try {
			ClientCredentials creds = spotify.clientCredentials().build().execute();
			spotify.setAccessToken(creds.getAccessToken());
		} catch (ParseException | SpotifyWebApiException | IOException e) {
			throw new FriendlyException(e);
		}
	}

}
