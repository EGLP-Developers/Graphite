package me.eglp.gv2.guild.recorder.recording;

import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;

public enum RecordingEventType implements JSONPrimitiveStringConvertible {

	UPDATE_USERS,
	UPDATE_TALKING_USERS,
	;

	@Override
	public String toJSONPrimitive() {
		return name();
	}

	public static RecordingEventType decodePrimitive(String obj) {
		return valueOf(obj);
	}

}
