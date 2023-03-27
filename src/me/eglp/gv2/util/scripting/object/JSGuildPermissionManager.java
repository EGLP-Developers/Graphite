package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.util.permission.GuildPermissionManager;

public class JSGuildPermissionManager {
	
	private GuildPermissionManager manager;
	
	public JSGuildPermissionManager(GuildPermissionManager manager) {
		this.manager = manager;
	}
	
	/**
	 * Returns whether a member has the specified permission.<br>
	 * This will still return true if the member doesn't have the permission implicitly
	 * (e.g. via everyone permissions or role permissions)
	 * @param member The member to check for
	 * @param permission The permission to check
	 * @return Whether the member has the permission
	 * @see #getPermissions(JSMember)
	 * @see #getEveryonePermissions()
	 * @see #getPermissions(JSRole)
	 */
	public boolean hasPermission(JSMember member, String permission) {
		return manager.hasPermission(member.member, permission);
	}
	
	/**
	 * Returns the permissions for the specified member
	 * @param member The member to get the permissions for
	 * @return The permissions for the specified member
	 */
	public JSPermissible getPermissions(JSMember member) {
		return new JSPermissible(manager.getPermissions(member.member));
	}
	
	/**
	 * Returns the permissions for the specified role
	 * @param role The role to get the permissions for
	 * @return The permissions for the specified role
	 */
	public JSPermissible getPermissions(JSRole role) {
		return new JSPermissible(manager.getPermissions(role.role));
	}
	
	/**
	 * Returns the permissions for everyone on the guild
	 * @return The permissions for everyone on the guild
	 */
	public JSPermissible getEveryonePermissions() {
		return new JSPermissible(manager.getEveryonePermissions());
	}
	
	@Override
	public String toString() {
		return "[JS Guild Permission Manager]";
	}

}
