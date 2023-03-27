package me.eglp.gv2.util.scripting.object;

import org.mozilla.javascript.Scriptable;

import me.eglp.gv2.util.permission.Permissible;
import me.eglp.gv2.util.scripting.GraphiteScript;

public class JSPermissible {
	
	private Permissible permissible;
	
	public JSPermissible(Permissible permissible) {
		this.permissible = permissible;
	}
	
	/**
	 * Adds the permission to the list of permissions
	 * @param permission The permission to add
	 */
	public void addPermission(String permission) {
		permissible.addPermission(permission);
	}
	
	/**
	 * Removes a permission from the list of permissions
	 * @param permission The permission to remove
	 */
	public void removePermission(String permission) {
		permissible.removePermission(permission);
	}
	
	/**
	 * Returns whether the permission or a higher permission is contained in the list of permissions
	 * (e.g if {@code test.*} is present it will still return true for {@code test.test})
	 * @param permission The permission to check
	 * @return Whether the permission or a higher permission is present
	 */
	public boolean hasPermission(String permission) {
		return permissible.hasPermission(permission);
	}
	
	/**
	 * Returns whether the exact permission is present in the list of permissions
	 * (e.g. even if {@code test.*} is present it will return false for {@code test.test})
	 * @param permission The permission to check
	 * @return Whether the permission is present
	 */
	public boolean hasPermissionExactly(String permission) {
		return permissible.hasPermissionExactly(permission);
	}
	
	/**
	 * Returns a list of all the (raw) permissions.<br>
	 * Changing this list will not affect the permissions this object has
	 * @return A list of all the (raw) permissions
	 */
	public Scriptable getRawPermissions() {
		return GraphiteScript.createJSArray(permissible.getRawPermissions().stream().toArray(String[]::new));
	}
	
	@Override
	public String toString() {
		return "[JS Permissible]";
	}

}
