package me.eglp.gv2.guild.recorder.recording;

import java.util.List;

import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONListType;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class RecordingEvent implements JSONConvertible, WebinterfaceObject {

	@JSONValue
	@JavaScriptValue(getter = "getFrameIndex")
	private int frameIndex;

	@JSONValue
	private RecordingEventType type;

	@JSONValue
	@JSONListType(JSONType.STRING)
	@JavaScriptValue(getter = "getUsers")
	private List<String> users;

	@JSONConstructor
	private RecordingEvent() {}

	public RecordingEvent(int frameIndex, RecordingEventType type, List<String> users) {
		this.frameIndex = frameIndex;
		this.type = type;
		this.users = users;
	}

	public int getFrameIndex() {
		return frameIndex;
	}

	@JavaScriptGetter(name = "getType", returning = "type")
	public RecordingEventType getType() {
		return type;
	}

	public List<String> getUsers() {
		return users;
	}

	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("type", type.name());
	}

}
