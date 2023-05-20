package me.eglp.gv2.util.permission;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.main.Graphite;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class MemberPermissions implements DiscardablePermissible {

	public static final String PERMISSIBLE_TYPE = "member";

	private GuildPermissionManager permissionManager;
	private GraphiteMember member;

	public MemberPermissions(GuildPermissionManager permissionManager, GraphiteMember member) {
		this.permissionManager = permissionManager;
		this.member = member;
	}

	@Override
	public GuildPermissionManager getPermissionManager() {
		return permissionManager;
	}

	@Override
	public void addPermission(Permission permission) {
		if(getPermissions().contains(permission)) return;
		Graphite.getMySQL().query("INSERT IGNORE INTO guilds_permissions(GuildId, PermissibleType, PermissibleId, Permission) VALUES(?, ?, ?, ?)", permissionManager.getGuild().getID(), PERMISSIBLE_TYPE, member.getID(), permission.getPermission());
	}

	@Override
	public void removePermission(Permission permission) {
		Graphite.getMySQL().query("DELETE FROM guilds_permissions WHERE GuildId = ? AND PermissibleType = ? AND PermissibleId = ? AND Permission = ?", permissionManager.getGuild().getID(), PERMISSIBLE_TYPE, member.getID(), permission.getPermission());
	}

	@Override
	public boolean hasPermission(Permission permission) {
		return member.isOwner() || member.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR) || DiscardablePermissible.super.hasPermission(permission);
	}

	@Override
	public void setPermissions(List<Permission> permissions) {
		Graphite.getMySQL().query("DELETE FROM guilds_permissions WHERE GuildId = ? AND PermissibleType = ? AND PermissibleId = ?", permissionManager.getGuild().getID(), PERMISSIBLE_TYPE, member.getID());
		Graphite.getMySQL().run(con -> {
			try(PreparedStatement p = con.prepareStatement("INSERT INTO guilds_permissions(GuildId, PermissibleType, PermissibleId, Permission) VALUES(?,?,?,?)")) {
				for(Permission perm : permissions) {
					p.setString(1, permissionManager.getGuild().getID());
					p.setString(2, PERMISSIBLE_TYPE);
					p.setString(3, member.getID());
					p.setString(4, perm.getPermission());
					p.addBatch();
				}
				p.executeBatch();
			}
		});
	}

	@Override
	public List<Permission> getPermissions() {
		return Graphite.getMySQL().queryArray(String.class, "SELECT Permission FROM guilds_permissions WHERE GuildId = ? AND PermissibleType = ? AND PermissibleId = ?", permissionManager.getGuild().getID(), PERMISSIBLE_TYPE, member.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load member permissions from MySQL", e)).stream()
				.map(Permission::new)
				.collect(Collectors.toList());
	}

	public GraphiteMember getMember() {
		return member;
	}

	@Override
	public void discard() {
		permissionManager.discardMember(member.getID());
	}

}
