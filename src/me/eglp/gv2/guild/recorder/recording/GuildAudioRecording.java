package me.eglp.gv2.guild.recorder.recording;

import java.util.List;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.util.base.GraphiteTemporary;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;

@JavaScriptClass(name = "AudioRecording")
public class GuildAudioRecording implements GraphiteTemporary, WebinterfaceObject {

	private GraphiteGuild guild;

	@JavaScriptValue(getter = "getName")
	private String name;

	@JavaScriptValue(getter = "getTimestamp")
	private long timestamp;

	@JavaScriptValue(getter = "getAudioLength")
	private long audioLength;

	@JavaScriptValue(getter = "getChannelName")
	private String channelName;

	@JavaScriptValue(getter = "getEvents")
	private List<RecordingEvent> events;

	public GuildAudioRecording(GraphiteGuild guild, String name, long timestamp, long audioLength, String channelName, List<RecordingEvent> events) {
		this.guild = guild;
		this.name = name;
		this.timestamp = timestamp;
		this.audioLength = audioLength;
		this.channelName = channelName;
		this.events = events;
	}

	public GraphiteGuild getGuild() {
		return guild;
	}

	public String getName() {
		return name;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public long getAudioLength() {
		return audioLength;
	}

	public String getChannelName() {
		return channelName;
	}

	public List<RecordingEvent> getEvents() {
		return events;
	}

	@JavaScriptGetter(name = "getSize", returning = "size")
	public long getSize() {
		return guild.getRecordingsConfig().getRecordingSize(name);
	}

	public byte[] loadAudioData() {
		return guild.getRecordingsConfig().getRecordingAudioData(name);
	}

	@Override
	public void remove() {
		guild.getRecordingsConfig().deleteRecording(name);
	}

	@Override
	public long getExpirationTime() {
		return timestamp + 48 * 60 * 60 * 1000;
	}

	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("size", getSize());
	}

	@JavaScriptFunction(calling = "getRecordings", returning = "recordings", withGuild = true)
	public static void getRecordings() {};

	@JavaScriptFunction(calling = "getRecording", returning = "recording", withGuild = true)
	public static void getRecording(@JavaScriptParameter(name = "recording_name") String name) {};

	@JavaScriptFunction(calling = "getRecordingAudio", returning = "audio", withGuild = true)
	public static void getRecordingAudio(@JavaScriptParameter(name = "recording_name") String name) {};

	@JavaScriptFunction(calling = "isRecording", returning = "is_recording", withGuild = true)
	public static void isRecording() {};

	@JavaScriptFunction(calling = "startRecording", withGuild = true)
	public static void startRecording() {};

	@JavaScriptFunction(calling = "stopRecording", withGuild = true)
	public static void stopRecording() {};

	@JavaScriptFunction(calling = "deleteRecording", withGuild = true)
	public static void deleteRecording(@JavaScriptParameter(name = "recording_name") String name) {};

	@JavaScriptFunction(calling = "deleteAllRecordings", withGuild = true)
	public static void deleteAllRecordings() {};

	@JavaScriptFunction(calling = "renameRecording", withGuild = true)
	public static void renameRecording(@JavaScriptParameter(name = "name") String name, @JavaScriptParameter(name = "new_name") String newName) {};

}
