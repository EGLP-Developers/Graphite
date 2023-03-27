package me.eglp.gv2.util.webinterface.js.classes;

import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

public class Moderator implements WebinterfaceObject{
	
	@JavaScriptFunction(calling = "getModLogChannel", returning = "channel", withGuild = true)
	public static void getModLogChannel() {};
	
	@JavaScriptFunction(calling = "setModLogChannel", withGuild = true)
	public static void setModLogChannel(@JavaScriptParameter(name = "id") String id) {};
	
	@JavaScriptFunction(calling = "unsetModLogChannel", withGuild = true)
	public static void unsetModLogChannel() {};
	
	@JavaScriptFunction(calling = "getModeratorRoles", returning = "moderator", withGuild = true)
	public static void getModeratorRoles() {};
	
	@JavaScriptFunction(calling = "addModeratorRole", withGuild = true)
	public static void addModeratorRole(@JavaScriptParameter(name = "id") String id) {};
	
	@JavaScriptFunction(calling = "removeModeratorRole", withGuild = true)
	public static void removeModeratorRole(@JavaScriptParameter(name = "id") String id) {};

}
