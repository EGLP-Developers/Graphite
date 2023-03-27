package me.eglp.gv2.util.webinterface.js.classes.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.lang.GraphiteTimeParser;
import me.eglp.gv2.util.music.GraphiteTrack;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

public class MusicTrack implements WebinterfaceObject{

	@JavaScriptValue(getter = "getIdentifier")
	private String identifier;

	@JavaScriptValue(getter = "getTitle")
	private String title;

	@JavaScriptValue(getter = "getAuthor")
	private String author;

	@JavaScriptValue(getter = "getFriendlyDuration")
	private String friendlyDuration;

	@JavaScriptValue(getter = "getDuration")
	private long duration;

	@JavaScriptValue(getter = "getURI")
	private String uri;

	@JavaScriptValue(getter = "getType")
	private String type;

	public MusicTrack(GraphiteGuild g, GraphiteTrack t) {
		AudioTrackInfo info = t.getLavaTrack().getInfo();

		this.identifier = info.identifier;
		this.title = info.title;
		this.author = info.author;
		this.friendlyDuration = GraphiteTimeParser.getTimestamp(info.length);
		this.duration = info.length;
		this.uri = info.uri;
		this.type = t.getType() == null ? null : t.getType().name();
	}

}
