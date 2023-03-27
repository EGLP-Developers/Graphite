package me.eglp.gv2.util.permission;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteRole;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class RolePermissions implements DiscardablePermissible {
	
	public static final String PERMISSIBLE_TYPE = "role";

	private GuildPermissionManager permissionManager;
	private GraphiteRole role;
	
	public RolePermissions(GuildPermissionManager permissionManager, GraphiteRole role) {
		this.permissionManager = permissionManager;
		this.role = role;
	}
	
	@Override
	public GuildPermissionManager getPermissionManager() {
		return permissionManager;
	}
	
	@Override
	public void addPermission(Permission permission) {
		if(getPermissions().contains(permission)) return;
		Graphite.getMySQL().query("INSERT IGNORE INTO guilds_permissions(GuildId, PermissibleType, PermissibleId, Permission) VALUES(?, ?, ?, ?)", permissionManager.getGuild().getID(), PERMISSIBLE_TYPE, role.getID(), permission.getPermission());
	}

	@Override
	public void removePermission(Permission permission) {
		Graphite.getMySQL().query("DELETE FROM guilds_permissions WHERE GuildId = ? AND PermissibleType = ? AND PermissibleId = ? AND Permission = ?", permissionManager.getGuild().getID(), PERMISSIBLE_TYPE, role.getID(), permission.getPermission());
	}
	
	public List<Permission> getPermissions() {
		return Graphite.getMySQL().queryArray(String.class, "SELECT Permission FROM guilds_permissions WHERE GuildId = ? AND PermissibleType = ? AND PermissibleId = ?", permissionManager.getGuild().getID(), PERMISSIBLE_TYPE, role.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load role permissions from MySQL", e)).stream()
				.map(Permission::new)
				.collect(Collectors.toList());
	}
	
	public GraphiteRole getRole() {
		return role;
	}

	@Override
	public void discard() {
		permissionManager.discardRole(role.getID());
	}

	@Override
	public void setPermissions(List<Permission> permissions) {
		Graphite.getMySQL().query("DELETE FROM guilds_permissions WHERE GuildId = ? AND PermissibleType = ? AND PermissibleId = ?", permissionManager.getGuild().getID(), PERMISSIBLE_TYPE, role.getID());
		Graphite.getMySQL().run(con -> {
			try(PreparedStatement p = con.prepareStatement("INSERT INTO guilds_permissions(GuildId, PermissibleType, PermissibleId, Permission) VALUES(?,?,?,?)")) {
				for(Permission perm : permissions) {
					p.setString(1, permissionManager.getGuild().getID());
					p.setString(2, PERMISSIBLE_TYPE);
					p.setString(3, role.getID());
					p.setString(4, perm.getPermission());
					p.addBatch();
				}
				p.executeBatch();
			}
		});
	}
	
}
