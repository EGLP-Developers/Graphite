package me.eglp.gv2.util.webinterface.handlers.music;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.commands.music.CommandMusicPlaylist;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.music.GuildMusic;
import me.eglp.gv2.guild.music.GuildTrackManager;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.user.EasterEgg;
import me.eglp.gv2.util.music.GraphitePlaylist;
import me.eglp.gv2.util.music.GraphiteTrack;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.eglp.gv2.util.webinterface.js.classes.music.MusicPlayerState;
import me.eglp.gv2.util.webinterface.js.classes.music.MusicPlaylist;
import me.eglp.gv2.util.webinterface.js.classes.music.MusicTrack;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class MusicRequestHandler {

	@WebinterfaceHandler(requestMethod = "playTrack", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse playTrack(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GraphiteMember m = g.getMember(event.getUser().getDiscordUser());
		if(m.getCurrentAudioChannel() == null) {
			return WebinterfaceResponse.error("You must be in an audio channel");
		}

		String link = event.getRequestData().getString("rawArgs").trim();
		if(!(link.startsWith("https://") || link.startsWith("http://"))) {
			link = "ytsearch: " + link;
		}

		GuildMusic.loadTrack(link).thenAccept(result -> {
			if(result.getResults().isEmpty()) return;
			if(result.isPlaylist()) {
				result.getResults().forEach(t -> g.getMusic().queue(t));
			}else {
				g.getMusic().queue(result.getResults().get(0));
			}
			g.getMusic().join(m.getCurrentAudioChannel());
		});

		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "stopPlaying", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse stopPlaying(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!g.getMusic().isPlaying()) {
			return WebinterfaceResponse.success();
		}
		g.getMusic().awaitStop();
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "jumpTo", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse jumpTo(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!g.getMusic().isPlaying()) {
			return WebinterfaceResponse.success();
		}
		int i = event.getRequestData().getInt("index");
		g.getMusic().jump(i);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "removeTrack", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse removeTrack(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!g.getMusic().isPlaying()) {
			return WebinterfaceResponse.success();
		}
		int i = event.getRequestData().getInt("index");
		g.getMusic().removeAbsolute(i);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "getCurrentPlayerState", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse getCurrentPlayerState(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();

		JSONObject o = new JSONObject();
		o.put("state", g.getMusic().isPlaying() ? new MusicPlayerState(g).toWebinterfaceObject() : null);
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "getCurrentPlayingTrack", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse getCurrentPlayingTrack(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();

		JSONObject o = new JSONObject();
		o.put("track", g.getMusic().isPlaying() ? new MusicTrack(g, g.getMusic().getPlayingTrack()).toWebinterfaceObject() : null);
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "getFullQueue", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse getFullQueue(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		List<GraphiteTrack> queue = new ArrayList<>(g.getMusic().getQueue().getFullQueue());

		JSONObject o = new JSONObject();
		o.put("queue", new JSONArray(queue.stream().map(t -> new MusicTrack(g, t).toWebinterfaceObject()).collect(Collectors.toList())));

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "shuffleQueue", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse shuffleQueue(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!g.getMusic().isPlaying()) {
			return WebinterfaceResponse.success();
		}
		g.getMusic().shuffleQueue();
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "getPlaylists", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse getPlaylists(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		List<GraphitePlaylist> pl = g.getMusic().getPlaylists();

		JSONArray arr = new JSONArray();

		for(GraphitePlaylist playlist : pl) {
			JSONArray tracks = new JSONArray(playlist.getTracks().stream().map(t -> new MusicTrack(g, t)).collect(Collectors.toList()));
			arr.add(new MusicPlaylist(playlist.getName(), tracks).toWebinterfaceObject());
		}

		JSONObject o = new JSONObject();
		o.put("playlists", arr);

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "playPlaylist", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse playPlaylist(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GraphiteMember m = g.getMember(event.getUser().getDiscordUser());
		String name = event.getRequestData().getString("playlist_name");
		GraphitePlaylist pl = g.getMusic().getPlaylistByName(name);
		if(pl == null) {
			return WebinterfaceResponse.error("Playlist doesn't exist");
		}

		if(m.getCurrentAudioChannel() == null) {
			return WebinterfaceResponse.error("You must be in an audio channel");
		}

		pl.getTracks().forEach(t -> g.getMusic().queue(t));
		g.getMusic().join(m.getCurrentAudioChannel());
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "deletePlaylist", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse deletePlaylist(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String name = event.getRequestData().getString("playlist_name");
		GraphitePlaylist pl = g.getMusic().getPlaylistByName(name);
		if(pl == null) {
			return WebinterfaceResponse.error("Playlist doesn't exist");
		}
		g.getMusic().deletePlaylist(pl);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "setVolume", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse setVolume(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!g.getMusic().isPlaying()) {
			return WebinterfaceResponse.success();
		}

		int val = event.getRequestData().getInt("volume");
		if(val < 0 || val > 100 && val != 300) return WebinterfaceResponse.error("Invalid volume");

		if(val == 300 && !event.getUser().getDiscordUser().getConfig().hasFoundEasterEgg(EasterEgg.MUSIC_VOLUME_EARRAPE)) {
			event.getUser().getDiscordUser().getConfig().addEasterEgg(EasterEgg.MUSIC_VOLUME_EARRAPE, true);
		}

		event.getSelectedGuild().getMusic().setVolume(val);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "setBassBoostLevel", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse setBassBoostLevel(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!g.getMusic().isPlaying()) {
			return WebinterfaceResponse.success();
		}

		int val = event.getRequestData().getInt("level");
		if(val < 0 || val > GuildTrackManager.BASS_BOOST_LEVELS.length) {
			return WebinterfaceResponse.error("Level too high/low");
		}

		event.getSelectedGuild().getMusic().setBassBoostLevel(val);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "setPitch", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse setPitch(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!g.getMusic().isPlaying()) {
			return WebinterfaceResponse.success();
		}

		double val = event.getRequestData().getDouble("pitch");
		if(val < GuildTrackManager.MIN_PITCH || val > GuildTrackManager.MAX_PITCH) {
			return WebinterfaceResponse.error("Pitch too high/low");
		}

		event.getSelectedGuild().getMusic().setPitch(val);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "setSpeed", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse setSpeed(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!g.getMusic().isPlaying()) {
			return WebinterfaceResponse.success();
		}

		double val = event.getRequestData().getDouble("speed");
		if(val < GuildTrackManager.MIN_SPEED || val > GuildTrackManager.MAX_SPEED) {
			return WebinterfaceResponse.error("Speed too high/low");
		}

		event.getSelectedGuild().getMusic().setSpeed(val);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "enableNightcore", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse enableNightcore(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!g.getMusic().isPlaying()) {
			return WebinterfaceResponse.success();
		}

		event.getSelectedGuild().getMusic().setSpeedAndPitch(1.25, 1.25);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "resetPlaybackSettings", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse resetPlaybackSettings(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!g.getMusic().isPlaying()) {
			return WebinterfaceResponse.success();
		}

		event.getSelectedGuild().getMusic().reset();
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "seek", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse seek(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!g.getMusic().isPlaying()) {
			return WebinterfaceResponse.success();
		}

		long val = event.getRequestData().getLong("position");
		g.getMusic().seek(val);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "setPaused", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse setPaused(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!g.getMusic().isPlaying()) {
			return WebinterfaceResponse.success();
		}

		boolean pause = event.getRequestData().getBoolean("pause");
		g.getMusic().setPaused(pause);

		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "setLooping", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse setLooping(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!g.getMusic().isPlaying()) {
			return WebinterfaceResponse.success();
		}

		boolean looping = event.getRequestData().getBoolean("looping");
		g.getMusic().setLooping(looping);

		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "setEndless", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse setEndless(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!g.getMusic().isPlaying()) {
			return WebinterfaceResponse.success();
		}

		boolean endless = event.getRequestData().getBoolean("endless");
		g.getMusic().setEndless(endless);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "createPlaylist", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse createPlaylist(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();

		List<GraphiteTrack> full = g.getMusic().getQueue().getFullQueue();
		if(!g.getMusic().isPlaying() || full.isEmpty()) {
			return WebinterfaceResponse.error("Nothing that can be dumped");
		}

		List<GraphiteTrack> saveableTracks = full.stream()
				.filter(GraphiteTrack::canBeSaved)
				.collect(Collectors.toList());
		GraphitePlaylist pl = g.getMusic().createPlaylist(saveableTracks, null);

		JSONArray tracks = new JSONArray();
		for(GraphiteTrack track : pl.getTracks()) {
			tracks.add(new MusicTrack(g, track));
		}

		JSONObject o = new JSONObject();
		o.put("playlist", new MusicPlaylist(pl.getName(), tracks).toWebinterfaceObject());

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "renamePlaylist", requireBot = true, requireGuild = true, requireFeatures = GraphiteFeature.MUSIC)
	public static WebinterfaceResponse renamePlaylist(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();

		String name = event.getRequestData().getString("name");
		String newName = event.getRequestData().getString("new_name");

		GraphitePlaylist playlist = g.getMusic().getPlaylistByName(name);

		if(playlist == null) {
			return WebinterfaceResponse.error("Playlist doesn't exist");
		}

		if(!CommandMusicPlaylist.PLAYLIST_NAME_PATTERN.matcher(newName).matches()) {
			return WebinterfaceResponse.error("New playlist name doesn't match our requirements");
		}

		playlist.setName(newName);

		return WebinterfaceResponse.success();
	}

}
