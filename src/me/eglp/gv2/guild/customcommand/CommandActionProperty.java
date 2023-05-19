package me.eglp.gv2.guild.customcommand;

import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

public class CommandActionProperty implements WebinterfaceObject {

	@JavaScriptValue(getter = "getName")
	private String name;

	@JavaScriptValue(getter = "getFriendlyName")
	private String friendlyName;

	@JavaScriptValue(getter = "getType")
	private CommandParameterType type;

	@JavaScriptConstructor
	private CommandActionProperty() {}

	public CommandActionProperty(String name, String friendlyName, CommandParameterType type) {
		this.name = name;
		this.friendlyName = friendlyName;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public CommandParameterType getType() {
		return type;
	}

}
