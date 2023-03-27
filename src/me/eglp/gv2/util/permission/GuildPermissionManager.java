package me.eglp.gv2.util.permission;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteMember;
import me.eglp.gv2.util.base.guild.GraphiteRole;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.mysql.SQLTable;

@SQLTable(
	name = "guilds_permissions",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"PermissibleType varchar(255) NOT NULL",
		"PermissibleId varchar(255) DEFAULT NULL",
		"Permission text NOT NULL"
	},
	guildReference = "GuildId"
)
public class GuildPermissionManager {
	
	private GraphiteGuild guild;
	private EveryonePermissions everyone = new EveryonePermissions(this);
	
	public GuildPermissionManager(GraphiteGuild guild) {
		this.guild = guild;
	}
	
	public GraphiteGuild getGuild() {
		return guild;
	}
	
	public List<MemberPermissions> getMemberPermissions() {
		return Graphite.getMySQL().queryArray(String.class, "SELECT PermissibleId FROM guilds_permissions WHERE GuildId = ? AND PermissibleType = ?", guild.getID(), MemberPermissions.PERMISSIBLE_TYPE).orElse(Collections.emptyList()).stream()
				.map(id -> {
					GraphiteMember m = guild.getMember(id);
					if(m == null) discardMember(id);
					return m;
				})
				.filter(Objects::nonNull)
				.map(m -> new MemberPermissions(this, m))
				.collect(Collectors.toList());
	}
	
	public List<RolePermissions> getRolePermissions() {
		return Graphite.getMySQL().queryArray(String.class, "SELECT PermissibleId FROM guilds_permissions WHERE GuildId = ? AND PermissibleType = ?", guild.getID(), MemberPermissions.PERMISSIBLE_TYPE).orElse(Collections.emptyList()).stream()
			.map(id -> {
				GraphiteRole r = guild.getRoleByID(id);
				if(r == null) discardRole(id);
				return r;
			})
			.filter(Objects::nonNull)
			.map(m -> new RolePermissions(this, m))
			.collect(Collectors.toList());
	}
	
	public EveryonePermissions getEveryonePermissions() {
		return everyone;
	}
	
	public MemberPermissions getPermissions(GraphiteUser user) {
		GraphiteMember member = guild.getMember(user);
		if(member == null) return null;
		return getPermissions(member);
	}
	
	public MemberPermissions getPermissions(GraphiteMember member) {
		if(!member.getGuild().equals(guild)) throw new IllegalArgumentException("Not from the same guild");
		return new MemberPermissions(this, member);
	}
	
	public RolePermissions getPermissions(GraphiteRole role) {
		if(!role.getGuild().equals(guild)) throw new IllegalArgumentException("Not from the same guild");
		return new RolePermissions(this, role);
	}
	
	public void discardRole(String roleID) {
		Graphite.getMySQL().query("DELETE FROM guilds_permissions WHERE GuildId = ? AND PermissibleType = ? AND PermissibleId = ?", guild.getID(), RolePermissions.PERMISSIBLE_TYPE, roleID);
	}
	
	public void discardMember(String memberID) {
		Graphite.getMySQL().query("DELETE FROM guilds_permissions WHERE GuildId = ? AND PermissibleType = ? AND PermissibleId = ?", guild.getID(), MemberPermissions.PERMISSIBLE_TYPE, memberID);
	}
	
	public void discardEverything() {
		Graphite.getMySQL().query("DELETE FROM guilds_permissions WHERE GuildId = ?", guild.getID());
	}
	
	public boolean hasPermission(GraphiteMember member, Permission permission) {
		return getEveryonePermissions().hasPermission(permission) ||
				getPermissions(member).hasPermission(permission) ||
				member.getRoles().stream().anyMatch(r -> getPermissions(r).hasPermission(permission));
	}
	
	public boolean hasPermission(GraphiteMember member, String permission) {
		return hasPermission(member, new Permission(permission));
	}
	
	public boolean hasPermission(GraphiteUser user, Permission permission) {
		GraphiteMember member = guild.getMember(user);
		if(member == null) return false;
		return hasPermission(member, permission);
	}
	
	public boolean hasPermission(GraphiteUser user, String permission) {
		return hasPermission(user, new Permission(permission));
	}
	
}
