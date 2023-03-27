package me.eglp.gv2.util.webinterface.js.classes;

import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

public class Bot implements WebinterfaceObject {
	
	@JavaScriptFunction(calling = "getAvailableBots", returning = "bots", withGuild = true)
	public static void getAvailableBots() {};
	
	@JavaScriptFunction(calling = "restart", withGuild = true)
	public static void restart() {};
	
	@JavaScriptFunction(calling = "shutdown", withGuild = true)
	public static void shutdown() {};
	
	@JavaScriptFunction(calling = "isAboveUserRoles", returning = "highest_role", withGuild = true)
	public static void isAboveUserRoles() {};
	
	@JavaScriptFunction(calling = "isOnServer", returning = "isOnServer", withGuild = false)
	public static void isOnServer(@JavaScriptParameter(name = "guild") String guildID) {};
	
	@JavaScriptFunction(calling = "getLoggedInUsers", returning = "users", withGuild = true)
	public static void getLoggedInUsers() {};
	
	@JavaScriptFunction(calling = "kickWIUser", withGuild = true)
	public static void kickWIUser(@JavaScriptParameter(name = "user_id") String userID) {};
	
}
