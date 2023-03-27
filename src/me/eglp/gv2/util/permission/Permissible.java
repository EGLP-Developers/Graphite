package me.eglp.gv2.util.permission;

import java.util.List;
import java.util.stream.Collectors;

public interface Permissible {

	public GuildPermissionManager getPermissionManager();
	
	public void addPermission(Permission permission);
	
	public default void addPermission(String permission) {
		addPermission(new Permission(permission));
	}
	
	public void removePermission(Permission permission);
	
	public default void removePermission(String permission) {
		removePermission(new Permission(permission));
	}
	
	public default boolean hasPermission(Permission permission) {
		return getPermissions().stream().anyMatch(p -> p.includes(permission));
	}

	public default boolean hasPermission(String permission) {
		return hasPermission(new Permission(permission));
	}
	
	public default boolean hasPermissionExactly(Permission permission) {
		return getPermissions().contains(permission);
	}
	
	public default boolean hasPermissionExactly(String permission) {
		return getPermissions().contains(new Permission(permission));
	}
	
	public default List<String> getRawPermissions() {
		return getPermissions().stream().map(Permission::getPermission).collect(Collectors.toList());
	}
	
	public void setPermissions(List<Permission> permissions);
	
	public List<Permission> getPermissions();
	
}
