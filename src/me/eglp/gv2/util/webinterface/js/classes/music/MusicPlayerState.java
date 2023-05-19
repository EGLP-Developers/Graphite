package me.eglp.gv2.util.webinterface.js.classes.music;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

public class MusicPlayerState implements WebinterfaceObject{

	@JavaScriptValue(getter = "getCurrentIndex")
	private int currentIndex;

	@JavaScriptValue(getter = "getVolume", setter = "setVolume")
	private int volume;

	@JavaScriptValue(getter = "getBassBoostLevel", setter = "setBassBoostLevel")
	private int bassBoostLevel;

	@JavaScriptValue(getter = "getPitch", setter = "setPitch")
	private double pitch;

	@JavaScriptValue(getter = "getSpeed", setter = "setSpeed")
	private double speed;

	@JavaScriptValue(getter = "getAudioChannel")
	private String audioChannel;

	@JavaScriptValue(getter = "getPosition", setter = "setPosition")
	private long position;

	@JavaScriptValue(getter = "isLooping", setter = "setLooping")
	private boolean looping;

	@JavaScriptValue(getter = "isPaused", setter = "setPaused")
	private boolean paused;

	@JavaScriptValue(getter = "isEndless", setter = "setEndless")
	private boolean endless;

	public MusicPlayerState(GraphiteGuild g) {
		this.currentIndex = g.getMusic().getQueue().getCurrentIndex();
		this.volume = g.getMusic().getVolume();
		this.bassBoostLevel = g.getMusic().getBassBoostLevel();
		this.pitch = g.getMusic().getPitch();
		this.speed = g.getMusic().getSpeed();
		this.audioChannel = g.getMusic().getChannel().getName();
		this.position = g.getMusic().getPosition();
		this.looping = g.getMusic().isLooping();
		this.endless = g.getMusic().isEndless();
		this.paused = g.getMusic().isPaused();
	}

	@JavaScriptFunction(calling = "playTrack", withGuild = true)
	public static void playTrack(@JavaScriptParameter(name = "rawArgs") String rawArgs) {};

	@JavaScriptFunction(calling = "stopPlaying", withGuild = true)
	public static void stopPlaying() {};

	@JavaScriptFunction(calling = "jumpTo", withGuild = true)
	public static void jumpTo(@JavaScriptParameter(name = "index") int index) {};

	@JavaScriptFunction(calling = "removeTrack", withGuild = true)
	public static void removeTrack(@JavaScriptParameter(name = "index") int index) {};

	@JavaScriptFunction(calling = "getCurrentPlayingTrack", returning = "track", withGuild = true)
	public static void getCurrentPlayingTrack() {};

	@JavaScriptFunction(calling = "getCurrentPlayerState", returning = "state", withGuild = true)
	public static void getCurrentPlayerState() {};

	@JavaScriptFunction(calling = "getFullQueue", returning = "queue", withGuild = true)
	public static void getFullQueue() {};

	@JavaScriptFunction(calling = "shuffleQueue", withGuild = true)
	public static void shuffleQueue() {};

	@JavaScriptFunction(calling = "getPlaylists", returning = "playlists", withGuild = true)
	public static void getPlaylists() {};

	@JavaScriptFunction(calling = "playPlaylist", withGuild = true)
	public static void playPlaylist(@JavaScriptParameter(name = "playlist_name") String id) {};

	@JavaScriptFunction(calling = "deletePlaylist", withGuild = true)
	public static void deletePlaylist(@JavaScriptParameter(name = "playlist_name") String id) {};

	@JavaScriptFunction(calling = "setVolume", withGuild = true)
	public static void setVolume(@JavaScriptParameter(name = "volume") int volume) {};

	@JavaScriptFunction(calling = "setBassBoostLevel", withGuild = true)
	public static void setBassBoostLevel(@JavaScriptParameter(name = "level") int level) {};

	@JavaScriptFunction(calling = "setPitch", withGuild = true)
	public static void setPitch(@JavaScriptParameter(name = "pitch") double pitch) {};

	@JavaScriptFunction(calling = "setSpeed", withGuild = true)
	public static void setSpeed(@JavaScriptParameter(name = "speed") double speed) {};

	@JavaScriptFunction(calling = "seek", withGuild = true)
	public static void seek(@JavaScriptParameter(name = "position") long level) {};

	@JavaScriptFunction(calling = "setPaused", withGuild = true)
	public static void setPaused(@JavaScriptParameter(name = "pause") boolean pause) {};

	@JavaScriptFunction(calling = "setLooping", withGuild = true)
	public static void setLooping(@JavaScriptParameter(name = "looping") boolean looping) {};

	@JavaScriptFunction(calling = "setEndless", withGuild = true)
	public static void setEndless(@JavaScriptParameter(name = "endless") boolean endless) {};

	@JavaScriptFunction(calling = "enableNightcore", withGuild = true)
	public static void enableNightcore() {};

	@JavaScriptFunction(calling = "resetPlaybackSettings", withGuild = true)
	public static void resetPlaybackSettings() {};

	@JavaScriptFunction(calling = "createPlaylist", returning = "playlist", withGuild = true)
	public static void createPlaylist() {};

	@JavaScriptFunction(calling = "renamePlaylist", withGuild = true)
	public static void renamePlaylist(@JavaScriptParameter(name = "name") String name, @JavaScriptParameter(name = "new_name") String newName) {};

}
