package me.eglp.gv2.util.webinterface.js.classes;

import java.util.List;

import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

@JavaScriptClass(name = "Permission")
public class JSPermission implements WebinterfaceObject{
	
	@JavaScriptValue(getter = "getPermission")
	private String permission;
	
	@JavaScriptValue(getter = "getAvailableCommands")
	private List<String> availableCommands;
	
	public JSPermission(String permission, List<String> availableCommands) {
		this.permission = permission;
		this.availableCommands = availableCommands;
	}
	
	public String getPermission() {
		return permission;
	}
	
	public List<String> getAvailableCommands() {
		return availableCommands;
	}
	
	@JavaScriptFunction(calling = "getPermissionGroups", returning = "permissionGroups", withGuild = true)
	public static void getPermissionGroups() {};
	
	@JavaScriptFunction(calling = "getMemberPermissions", returning = "permissions", withGuild = true)
	public static void getMemberPermissions(@JavaScriptParameter(name = "memberID") String memberID) {};
	
	@JavaScriptFunction(calling = "setMemberPermission", withGuild = true)
	public static void setMemberPermission(@JavaScriptParameter(name = "memberID") String memberID, @JavaScriptParameter(name = "permission") String permission, @JavaScriptParameter(name = "allow") boolean allow) {};
	
	@JavaScriptFunction(calling = "getRolePermissions", returning = "permissions", withGuild = true)
	public static void getRolePermissions(@JavaScriptParameter(name = "roleID") String roleID) {};
	
	@JavaScriptFunction(calling = "setRolePermission", withGuild = true)
	public static void setRolePermission(@JavaScriptParameter(name = "roleID") String roleID, @JavaScriptParameter(name = "permission") String permission, @JavaScriptParameter(name = "allow") boolean allow) {};
	
	@JavaScriptFunction(calling = "getEveryonePermissions", returning = "permissions", withGuild = true)
	public static void getEveryonePermissions() {};
	
	@JavaScriptFunction(calling = "setEveryonePermission", withGuild = true)
	public static void setEveryonePermission(@JavaScriptParameter(name = "permission") String permission, @JavaScriptParameter(name = "allow") boolean allow) {};

}
