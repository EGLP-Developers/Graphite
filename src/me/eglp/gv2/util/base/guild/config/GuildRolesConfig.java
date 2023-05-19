package me.eglp.gv2.util.base.guild.config;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteRole;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.misc.FriendlyException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

@SQLTable(
	name = "guilds_roles",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"RoleId varchar(255) NOT NULL",
		"`Type` varchar(255) NOT NULL",
		"PRIMARY KEY (GuildId, RoleId, Type)"
	},
	guildReference = "GuildId"
)
public class GuildRolesConfig {

	public static final String
		ROLE_TYPE_ACCESSIBLE = "accessible",
		ROLE_TYPE_AUTO = "auto",
		ROLE_TYPE_BOT = "bot",
		ROLE_TYPE_MODERATOR = "moderator",
		ROLE_TYPE_MUTED = "muted";

	public static final EnumSet<Permission> MUTED_ROLE_DENIED_PERMISSIONS = EnumSet.of(
		Permission.MESSAGE_SEND,
		Permission.CREATE_PUBLIC_THREADS,
		Permission.CREATE_PRIVATE_THREADS,
		Permission.MESSAGE_SEND_IN_THREADS);

	private GraphiteGuild guild;

	public GuildRolesConfig(GraphiteGuild guild) {
		this.guild = guild;
	}

	public void removeRole(String roleID) {
		Graphite.getMySQL().query("DELETE FROM guilds_roles WHERE GuildId = ? AND RoleId = ?", guild.getID(), roleID);
	}

	private void setRoles(String type, List<GraphiteRole> roles) {
		removeAllRoles(type);
		Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("INSERT INTO guilds_roles(GuildId, RoleId, `Type`) VALUES(?, ?, ?)")) {
				for(GraphiteRole r : roles) {
					s.setString(1, guild.getID());
					s.setString(2, r.getID());
					s.setString(3, type);
					s.addBatch();
				}

				s.executeBatch();
			}
		});
	}

	private void removeAllRoles(String type) {
		Graphite.getMySQL().query("DELETE FROM guilds_roles WHERE GuildId = ? AND `Type` = ?", guild.getID(), type);
	}

	public List<GraphiteRole> getAccessibleRoles(){
		return Graphite.getMySQL().queryArray(String.class, "SELECT RoleId FROM guilds_roles WHERE GuildId = ? AND Type = ?", guild.getID(), ROLE_TYPE_ACCESSIBLE).orElse(Collections.emptyList()).stream()
				.map(guild::getRoleByID)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public void setAccessibleRoles(List<GraphiteRole> roles) {
		setRoles(ROLE_TYPE_ACCESSIBLE, roles);
	}

	public void addAccessibleRole(GraphiteRole role) {
		Graphite.getMySQL().query("INSERT IGNORE INTO guilds_roles(GuildId, RoleId, Type) VALUES(?, ?, ?)", guild.getID(), role.getID(), ROLE_TYPE_ACCESSIBLE);
	}

	public void removeAccessibleRole(GraphiteRole role) {
		Graphite.getMySQL().query("DELETE FROM guilds_roles WHERE GuildId = ? AND RoleId = ? AND Type = ?", guild.getID(), role.getID(), ROLE_TYPE_ACCESSIBLE);
	}

	public List<GraphiteRole> getAutoRoles(){
		return Graphite.getMySQL().queryArray(String.class, "SELECT RoleId FROM guilds_roles WHERE GuildId = ? AND Type = ?", guild.getID(), ROLE_TYPE_AUTO).orElse(Collections.emptyList()).stream()
				.map(guild::getRoleByID)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public void setAutoRoles(List<GraphiteRole> roles) {
		setRoles(ROLE_TYPE_AUTO, roles);
	}

	public void addAutoRole(GraphiteRole role) {
		Graphite.getMySQL().query("INSERT IGNORE INTO guilds_roles(GuildId, RoleId, Type) VALUES(?, ?, ?)", guild.getID(), role.getID(), ROLE_TYPE_AUTO);
	}

	public void removeAutoRole(GraphiteRole role) {
		Graphite.getMySQL().query("DELETE FROM guilds_roles WHERE GuildId = ? AND RoleId = ? AND Type = ?", guild.getID(), role.getID(), ROLE_TYPE_AUTO);
	}

	public List<GraphiteRole> getBotRoles(){
		return Graphite.getMySQL().queryArray(String.class, "SELECT RoleId FROM guilds_roles WHERE GuildId = ? AND Type = ?", guild.getID(), ROLE_TYPE_BOT).orElse(Collections.emptyList()).stream()
				.map(guild::getRoleByID)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public void setBotRoles(List<GraphiteRole> roles) {
		setRoles(ROLE_TYPE_BOT, roles);
	}

	public void addBotRole(GraphiteRole role) {
		Graphite.getMySQL().query("INSERT IGNORE INTO guilds_roles(GuildId, RoleId, Type) VALUES(?, ?, ?)", guild.getID(), role.getID(), ROLE_TYPE_BOT);
	}

	public void removeBotRole(GraphiteRole role) {
		Graphite.getMySQL().query("DELETE FROM guilds_roles WHERE GuildId = ? AND RoleId = ? AND Type = ?", guild.getID(), role.getID(), ROLE_TYPE_BOT);
	}

	public List<GraphiteRole> getModeratorRoles(){
		return Graphite.getMySQL().queryArray(String.class, "SELECT RoleId FROM guilds_roles WHERE GuildId = ? AND Type = ?", guild.getID(), ROLE_TYPE_MODERATOR).orElse(Collections.emptyList()).stream()
				.map(guild::getRoleByID)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public void setModeratorRoles(List<GraphiteRole> roles) {
		setRoles(ROLE_TYPE_MODERATOR, roles);
	}

	public void addModeratorRole(GraphiteRole role) {
		Graphite.getMySQL().query("INSERT IGNORE INTO guilds_roles(GuildId, RoleId, Type) VALUES(?, ?, ?)", guild.getID(), role.getID(), ROLE_TYPE_MODERATOR);
	}

	public void removeModeratorRole(GraphiteRole role) {
		Graphite.getMySQL().query("DELETE FROM guilds_roles WHERE GuildId = ? AND RoleId = ? AND Type = ?", guild.getID(), role.getID(), ROLE_TYPE_MODERATOR);
	}

	public boolean isRoleAccessible(GraphiteRole role) {
		return getAccessibleRoles().contains(role);
	}

	public boolean isRoleAuto(GraphiteRole role) {
		return getAutoRoles().contains(role);
	}

	public boolean isBotRole(GraphiteRole role) {
		return getBotRoles().contains(role);
	}

	public boolean isModeratorRole(GraphiteRole role) {
		return getModeratorRoles().contains(role);
	}

	public GraphiteRole getOrSetupMutedRole() {
		GraphiteRole mRole = getMutedRoleRaw();
		if(mRole != null) return mRole;

		Role r = guild.getJDAGuild().createRole()
				.setName("Muted")
				.setColor(8487814)
				.complete();

		for(GuildChannel tCh : guild.getJDAGuild().getChannels()) {
			if(!(tCh instanceof GuildMessageChannel)) continue;
			GuildMessageChannel mCh = (GuildMessageChannel) tCh;
			mCh.getPermissionContainer().upsertPermissionOverride(r).deny(MUTED_ROLE_DENIED_PERMISSIONS).complete();
		}

		mRole = guild.getRole(r);
		setMutedRole(mRole);
		return mRole;
	}

	public void setMutedRole(GraphiteRole role) {
		setRoles(ROLE_TYPE_MUTED, Collections.singletonList(role));
	}

	public GraphiteRole getMutedRoleRaw() {
		String id = Graphite.getMySQL().query(String.class, null, "SELECT RoleId FROM guilds_roles WHERE GuildId = ? AND Type = ?", guild.getID(), ROLE_TYPE_MUTED)
				.orElseThrowOther(e -> new FriendlyException("Failed to load muted role from MySQL", e));
		if(id == null) return null;
		return guild.getRoleByID(id);
	}

	public void unsetMutedRole() {
		removeAllRoles(ROLE_TYPE_MUTED);
	}

}
