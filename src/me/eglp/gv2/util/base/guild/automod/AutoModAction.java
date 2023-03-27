package me.eglp.gv2.util.base.guild.automod;

import me.eglp.gv2.util.webinterface.js.JavaScriptEnum;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;

@JavaScriptEnum
public enum AutoModAction implements WebinterfaceObject, JSONPrimitiveStringConvertible {	

	DISABLED("Disabled", false, false),
	DELETE("Delete", true, false),
	WARN("Warn", false, true),
	DELETE_AND_WARN("Delete & Warn", true, true);
	
	@JavaScriptValue(getter = "getFriendlyName")
	private String friendlyName;
	
	private boolean
		delete,
		warn;

	private AutoModAction(String friendlyName, boolean delete, boolean warn) {
		this.friendlyName = friendlyName;
		this.delete = delete;
		this.warn = warn;
	}

	public boolean isDelete() {
		return delete;
	}

	public boolean isWarn() {
		return warn;
	}
	
	public String getFriendlyName() {
		return friendlyName;
	}

	@Override
	public String toJSONPrimitive() {
		return name();
	}
	
	public static AutoModAction decodePrimitive(Object value) {
		return valueOf((String) value);
	}
	
}
