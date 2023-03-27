package me.eglp.gv2.util.webinterface.js.classes;

import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

public class Module implements WebinterfaceObject {

	@JavaScriptValue(getter = "getID")
	private String id;
	
	@JavaScriptValue(getter = "getName")
	private String name;
	
	@JavaScriptValue(getter = "getAvailableCommands")
	private String commands;
	
	@JavaScriptValue(getter = "isEnabled")
	private boolean enabled;
	
	public Module(String id, String name, String commands, boolean enabled) {
		this.id = id;
		this.name = name;
		this.commands = commands;
		this.enabled = enabled;
	}
	
	@JavaScriptFunction(calling = "enableModule", withGuild = true)
	public static void enableModule(@JavaScriptParameter(name = "module_id") String id) {};
	
	@JavaScriptFunction(calling = "disableModule", withGuild = true)
	public static void disableModule(@JavaScriptParameter(name = "module_id") String id) {};

	@JavaScriptFunction(calling = "getModules", returning = "modules", withGuild = true)
	public static void getModules() {};

}
