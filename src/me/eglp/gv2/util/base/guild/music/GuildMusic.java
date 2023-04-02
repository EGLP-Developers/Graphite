package me.eglp.gv2.util.base.guild.music;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.multiplex.ContextHandle;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.apis.spotify.GraphiteSpotify;
import me.eglp.gv2.util.base.guild.GraphiteAudioChannel;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.music.GraphiteMusic;
import me.eglp.gv2.util.music.GraphiteTrack;
import me.mrletsplay.mrcore.json.JSONObject;

public class GuildMusic extends GraphiteMusic {
	
	private static final Pattern YOUTUBE_PATTERN = Pattern.compile("http(?:s)?://(?:www\\.)?(?:youtube\\.com/watch|youtu\\.be/).+?(?:&|\\?)t=(?<timestamp>\\d+)(?:\\&.+)?");
	
	// NONBETA: move setVolume() etc. to GuildTrackManager
	
	private GraphiteGuild guild;
	private Map<MultiplexBot, GuildTrackManager> trackManagers;
	
	public GuildMusic(GraphiteGuild guild) {
		super(guild);
		this.guild = guild;
		this.trackManagers = new HashMap<>();
	}
	
	public GuildTrackManager getTrackManager(MultiplexBot bot) {
		GuildTrackManager m = trackManagers.get(bot);
		if(m == null) {
			m = new GuildTrackManager(bot, guild);
			trackManagers.put(bot, m);
			Graphite.withBot(bot, () -> guild.getJDAGuild().getAudioManager().setSendingHandler(new GuildAudioSendHandler(bot, guild)));
		}
		return m;
	}
	
	private GuildTrackManager getTrackManager() {
		return getTrackManager(GraphiteMultiplex.getCurrentBot());
	}
	
	public GraphiteGuild getGuild() {
		return guild;
	}

	public GraphiteAudioChannel getChannel() {
		return getTrackManager().getChannel();
	}
	
	public void setVolume(int volume) {
		getTrackManager().getPlayer().setVolume(volume);
		
		Graphite.getWebinterface().sendRequestToGuildUsers("updatePlayerState", null, guild.getID(), GraphiteFeature.MUSIC);
	}
	
	public int getVolume() {
		return getTrackManager().getPlayer().getVolume();
	}

	public boolean isEarrape() {
		return getTrackManager().getPlayer().getVolume() > 100;
	}
	
	public void skip(int amount) {
		getTrackManager().skip(amount);
	}
	
	public void jump(int absoluteIndex) {
		getTrackManager().jump(absoluteIndex);
	}
	
	public void setPaused(boolean paused) {
		getTrackManager().getPlayer().setPaused(paused);
		
		Graphite.getWebinterface().sendRequestToGuildUsers("updatePlayerState", null, guild.getID(), GraphiteFeature.MUSIC);
	}
	
	public GraphiteTrack removeRelative(int idx) {
		return getTrackManager().removeRelative(idx);
	}
	
	public GraphiteTrack removeAbsolute(int idx) {
		return getTrackManager().removeAbsolute(idx);
	}

	public boolean isPaused() {
		return getTrackManager().getPlayer().isPaused();
	}
	
	public void join(GraphiteAudioChannel channel) {
		getTrackManager().setChannel(channel);
		getTrackManager().play();
	}
	
	public void queue(GraphiteTrack track) {
		getTrackManager().queueTrack(track);
	}
	
	public MusicQueue getQueue() {
		return getTrackManager().getQueue();
	}
	
	public void shuffleQueue() {
		getTrackManager().shuffleQueue();
		
		Graphite.getWebinterface().sendRequestToGuildUsers("updateQueue", null, guild.getID(), GraphiteFeature.MUSIC);
	}
	
	public void setLooping(boolean looping) {
		getTrackManager().setLooping(looping);
	}
	
	public boolean seek(long position) {
		AudioTrack tr = getTrackManager().getPlayer().getPlayingTrack();
		if(position < 0 || position > tr.getDuration()) {
			return false;
		}
		tr.setPosition(position);
		
		JSONObject d = new JSONObject();
		d.put("position", position);
		Graphite.getWebinterface().sendRequestToGuildUsers("updatePosition", d, guild.getID(), GraphiteFeature.MUSIC);
		return true;
	}
	
	public boolean fastForward(long duration) {
		AudioTrack tr = getTrackManager().getPlayer().getPlayingTrack();
		return seek(tr.getPosition() + duration);
	}
	
	public long getPosition() {
		AudioTrack tr = getTrackManager().getPlayer().getPlayingTrack();
		if(tr == null) return 0L;
		return tr.getPosition();
	}
	
	public boolean rewind(long duration) {
		AudioTrack tr = getTrackManager().getPlayer().getPlayingTrack();
		return seek(tr.getPosition() - duration);
	}

	public boolean isLooping() {
		return getTrackManager().isLooping();
	}
	
	public GraphiteTrack getPlayingTrack() {
		return getTrackManager().getPlayingTrack();
	}
	
	public boolean isPlaying() {
		return getPlayingTrack() != null;
	}
	
	public void setBassBoostLevel(int bassBoostLevel) {
		getTrackManager().setBassBoostLevel(bassBoostLevel);
	}

	public int getBassBoostLevel() {
		return getTrackManager().getBassBoostLevel();
	}
	
	public void setSpeed(double speed) {
		getTrackManager().setSpeed(speed);
	}
	
	public double getSpeed() {
		return getTrackManager().getSpeed();
	}
	
	public void setPitch(double pitch) {
		getTrackManager().setPitch(pitch);
	}
	
	public double getPitch() {
		return getTrackManager().getPitch();
	}
	
	public void setSpeedAndPitch(double speed, double pitch) {
		getTrackManager().setSpeedAndPitch(speed, pitch);
	}
	
	public void reset() {
		getTrackManager().reset();
	}
	
	public void stop() {
		getTrackManager().getPlayer().setPaused(false);
		getTrackManager().stop();
	}
	
	public void awaitStop() {
		stop();
		while(guild.getJDAGuild().getAudioManager().isConnected()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				GraphiteDebug.log(DebugCategory.MUSIC, e);
			}
		}
	}
	
	public void setEndless(boolean endless) {
		getTrackManager().setEndless(endless);
	}

	public boolean isEndless() {
		return getTrackManager().isEndless();
	}
	
	public static CompletableFuture<MusicSearchResult> loadTrack(String linkOrQuery) {
		CompletableFuture<MusicSearchResult> f = new CompletableFuture<>();
		ContextHandle h = GraphiteMultiplex.handle();
		
		GraphiteSpotify spotify = Graphite.getSpotify();
		if(spotify != null) {
			Matcher mAlbum = GraphiteSpotify.ALBUM_LINK_PATTERN.matcher(linkOrQuery);
			
			if(mAlbum.matches()) {
				List<String> isrc = spotify.getAlbumISRC(mAlbum.group("id"));
				
				if(isrc == null) {
					return CompletableFuture.failedFuture(new FriendlyException("Couldn't load album from Spotify", Severity.COMMON, null));
				}
				
				var futures = isrc.stream()
						.filter(Objects::nonNull)
						.map(i -> loadTrack("ytsearch: " + i))
						.collect(Collectors.toList());
				
				return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenApply(v -> {
					return new MusicSearchResult(futures.stream()
						.map(ft -> ft.join().getResults())
						.flatMap((List<GraphiteTrack> t) -> t.stream())
						.collect(Collectors.toList()), true);
				});
			}
			
			Matcher mPlaylist = GraphiteSpotify.PLAYLIST_LINK_PATTERN.matcher(linkOrQuery);
			
			if(mPlaylist.matches()) {
				List<String> isrc = spotify.getPlaylistISRC(mPlaylist.group("id"));
				
				if(isrc == null) {
					return CompletableFuture.failedFuture(new FriendlyException("Couldn't load playlist from Spotify", Severity.COMMON, null));
				}
				
				var futures = isrc.stream()
						.filter(Objects::nonNull)
						.map(i -> loadTrack("ytsearch: " + i))
						.collect(Collectors.toList());
				
				return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenApply(v -> {
					return new MusicSearchResult(futures.stream()
						.map(ft -> ft.join().getResults())
						.flatMap((List<GraphiteTrack> t) -> t.stream())
						.collect(Collectors.toList()), true);
				});
			}
			
			Matcher mTrack = GraphiteSpotify.TRACK_LINK_PATTERN.matcher(linkOrQuery);
			
			if(mTrack.matches()) {
				String isrc = spotify.getTrackISRC(mTrack.group("id"));
				
				if(isrc == null) {
					return CompletableFuture.failedFuture(new FriendlyException("Couldn't load track from Spotify", Severity.COMMON, null));
				}
				
				linkOrQuery = "ytsearch: " + isrc;
			}
		}
		
		final String fIdentifier = linkOrQuery;
		
		Graphite.getAudioPlayerManager().loadItem(linkOrQuery, new AudioLoadResultHandler() {
			
			@Override
			public void trackLoaded(AudioTrack track) {
				GraphiteTrack tr = new GraphiteTrack(track);
				if(track instanceof YoutubeAudioTrack) {
					Matcher m = YOUTUBE_PATTERN.matcher(fIdentifier);
					if(m.matches()) {
						Integer timestamp = Integer.parseInt(m.group("timestamp"));
						tr.setStartAt(timestamp);
					}
				}
				
				h.reset();
				f.complete(new MusicSearchResult(tr));
			}
			
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				h.reset();
				List<GraphiteTrack> trs = playlist.getTracks().stream().map(GraphiteTrack::new).collect(Collectors.toList());
				f.complete(new MusicSearchResult(trs, !fIdentifier.startsWith("ytsearch: ")));
			}
			
			@Override
			public void noMatches() {
				h.reset();
				f.complete(new MusicSearchResult());
			}
			
			@Override
			public void loadFailed(FriendlyException exception) {
				h.reset();
				f.completeExceptionally(exception);
			}
		});
		return f;
	}
	
}
