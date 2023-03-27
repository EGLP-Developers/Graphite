package me.eglp.gv2.util.webinterface.js.classes;

import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

public class Support implements WebinterfaceObject{
	
	@JavaScriptFunction(calling = "getSupportQueue", returning = "support_queue", withGuild = true)
	public static void getSupportQueue() {};
	
	@JavaScriptFunction(calling = "setSupportQueue", withGuild = true)
	public static void setSupportQueue(@JavaScriptParameter(name = "channel") String channel) {};
	
	@JavaScriptFunction(calling = "unsetSupportQueue", withGuild = true)
	public static void unsetSupportQueue() {};

}
