package me.eglp.gv2.util.permission;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class EveryonePermissions implements Permissible {
	
	public static final String PERMISSIBLE_TYPE = "everyone";

	private GuildPermissionManager permissionManager;
	
	public EveryonePermissions(GuildPermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	
	@Override
	public GuildPermissionManager getPermissionManager() {
		return permissionManager;
	}
	
	@Override
	public void addPermission(Permission permission) {
		if(getPermissions().contains(permission)) return;
		Graphite.getMySQL().query("INSERT IGNORE INTO guilds_permissions(GuildId, PermissibleType, PermissibleId, Permission) VALUES(?, ?, ?, ?)", permissionManager.getGuild().getID(), PERMISSIBLE_TYPE, "everyone", permission.getPermission());
	}

	@Override
	public void removePermission(Permission permission) {
		Graphite.getMySQL().query("DELETE FROM guilds_permissions WHERE GuildId = ? AND PermissibleType = ? AND Permission = ?", permissionManager.getGuild().getID(), PERMISSIBLE_TYPE, permission.getPermission());
	}
	
	@Override
	public List<Permission> getPermissions() {
		return Graphite.getMySQL().queryArray(String.class, "SELECT Permission FROM guilds_permissions WHERE GuildId = ? AND PermissibleType = ?", permissionManager.getGuild().getID(), PERMISSIBLE_TYPE)
				.orElseThrowOther(e -> new FriendlyException("Failed to load everyone permissions from MySQL", e)).stream()
				.map(Permission::new)
				.collect(Collectors.toList());
	}

	@Override
	public void setPermissions(List<Permission> permissions) {
		Graphite.getMySQL().query("DELETE FROM guilds_permissions WHERE GuildId = ? AND PermissibleType = ?", permissionManager.getGuild().getID(), PERMISSIBLE_TYPE);
		Graphite.getMySQL().run(con -> {
			try(PreparedStatement p = con.prepareStatement("INSERT INTO guilds_permissions(GuildId, PermissibleType, PermissibleId, Permission) VALUES(?,?,?,?)")) {
				for(Permission perm : permissions) {
					p.setString(1, permissionManager.getGuild().getID());
					p.setString(2, PERMISSIBLE_TYPE);
					p.setString(3, null);
					p.setString(4, perm.getPermission());
					p.addBatch();
				}
				p.executeBatch();
			}
		});
	}
	
}
