package me.eglp.gv2.util.apis.twitch;

import me.eglp.gv2.util.webinterface.js.JavaScriptEnum;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;

@JavaScriptEnum
public enum TwitchAnnouncementParameter implements WebinterfaceObject, JSONPrimitiveStringConvertible {

	SHOW_GAME("Show game", true),
	SHOW_PROFILE_ICON("Show profile icon", false),
	SHOW_VIEWERS("Show viewers", false),
	SHOW_TITLE("Show title", true),
	SHOW_STREAMER_LINK("Show streamer link", true),
	SHOW_STREAM_PREVIEW("Show stream preview", false),
	SHOW_STARTED_AT("Show started at", false),
	REMOVE_ON_END("Remove message on stream end", false);

	@JavaScriptValue(getter = "getFriendlyName")
	private String friendyName;

	@JavaScriptValue(getter = "show")
	private boolean def;

	private TwitchAnnouncementParameter(String friendlyName, boolean def) {
		this.friendyName = friendlyName;
		this.def = def;
	}

	public String getFriendyName() {
		return friendyName;
	}

	public boolean show() {
		return def;
	}

	@Override
	public String toJSONPrimitive() {
		return name();
	}

	public static TwitchAnnouncementParameter decodePrimitive(String value) {
		return valueOf(value);
	}

}
