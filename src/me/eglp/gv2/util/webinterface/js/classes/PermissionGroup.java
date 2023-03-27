package me.eglp.gv2.util.webinterface.js.classes;

import java.util.List;

import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

public class PermissionGroup implements WebinterfaceObject {
	
	@JavaScriptValue(getter = "getName")
	private String name;
	
	@JavaScriptValue(getter = "getPermissions")
	private List<JSPermission> permissions;
	
	public PermissionGroup(String name, List<JSPermission> permissionGroups) {
		this.name = name; 
		this.permissions = permissionGroups;
	}
	
}
