package me.eglp.gv2.guild.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.eglp.gv2.guild.GraphiteAudioChannel;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.GraphiteRole;
import me.eglp.gv2.guild.GuildChatMute;
import me.eglp.gv2.guild.GuildJail;
import me.eglp.gv2.guild.modlog.ModLogEntry;
import me.eglp.gv2.guild.modlog.ModLogEntryType;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "guilds_jails",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"MemberId varchar(255) NOT NULL",
		"ChannelId varchar(255) NOT NULL",
		"ExpiresAt bigint DEFAULT NULL",
		"LeaveAttempts tinyint DEFAULT 0",
		"PRIMARY KEY (GuildId, MemberId)"
	},
	guildReference = "GuildId"
)
@SQLTable(
	name = "guilds_chatmutes",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"MemberId varchar(255) NOT NULL",
		"ExpiresAt bigint DEFAULT NULL",
		"PRIMARY KEY (GuildId, MemberId)"
	},
	guildReference = "GuildId"
)
@SQLTable(
	name = "guilds_modlog",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"`Type` varchar(255) NOT NULL",
		"Timestamp bigint NOT NULL",
		"ActionDuration bigint NOT NULL",
		"MemberId varchar(255) NOT NULL",
		"ModeratorId varchar(255) NOT NULL",
		"Reason text"
	},
	guildReference = "GuildId"
)
public class GuildModerationConfig implements IGuildConfig {

	private GraphiteGuild guild;

	public GuildModerationConfig(GraphiteGuild guild) {
		this.guild = guild;
	}

	@ChannelRemoveListener
	private void removeChannel(GraphiteAudioChannel channel) {
		Graphite.getMySQL().query("DELETE FROM guilds_jails WHERE GuildId = ? AND ChannelId = ?", guild.getID(), channel.getID());
	}

	public List<GuildJail> getJails() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_jails WHERE GuildId = ?")) {
				s.setString(1, guild.getID());
				try(ResultSet r = s.executeQuery()) {
					List<GuildJail> jails = new ArrayList<>();
					while(r.next()) {
						GuildJail j = loadJail(r);
						if(j != null) jails.add(j);
					}
					return jails;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load jails from MySQL", e));
	}

	public GuildJail getJail(GraphiteMember member) {
		return getJail(member.getID());
	}

	public GuildJail getJail(String memberID) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_jails WHERE GuildId = ? AND MemberId = ?")) {
				s.setString(1, guild.getID());
				s.setString(2, memberID);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return null;
					return loadJail(r);
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load jail from MySQL", e));
	}

	public boolean isJailed(GraphiteMember member) {
		return getJail(member) != null;
	}

	private GuildJail loadJail(ResultSet r) throws SQLException {
		GraphiteMember m = guild.getMember(r.getString("MemberId"));
		if(m == null) {
			removeJailRaw(r.getString("MemberId"), guild.getSelfMember(), "Member left");
			return null;
		}

		GraphiteAudioChannel vc = guild.getAudioChannelByID(r.getString("ChannelId"));
		if(vc == null) {
			removeJailRaw(r.getString("MemberId"), guild.getSelfMember(), "Jail channel deleted");
			return null;
		}

		if(r.getLong("ExpiresAt") != -1 && r.getLong("ExpiresAt") < System.currentTimeMillis()) {
			removeJailRaw(r.getString("MemberId"), guild.getSelfMember(), "Jail expired");
			return null;
		}

		return new GuildJail(m, vc, r.getInt("LeaveAttempts"), r.getLong("ExpiresAt"));
	}

	private void addJail(GuildJail jail, long durationMs, GraphiteMember moderator, String reason) {
		Graphite.getMySQL().query("INSERT INTO guilds_jails(GuildId, MemberId, ChannelId, ExpiresAt, LeaveAttempts) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE ChannelId = VALUES(ChannelId), ExpiresAt = VALUES(ExpiresAt), LeaveAttempts = VALUES(LeaveAttempts)", guild.getID(), jail.getMember().getID(), jail.getChannel().getID(), jail.getExpirationTimeRaw(), jail.getLeaveAttempts());
		addModLogEntry(new ModLogEntry(jail.isTemporary() ? ModLogEntryType.TEMP_JAIL : ModLogEntryType.JAIL, durationMs, jail.getMember().getID(), moderator.getID(), reason));
	}

	public void removeJail(GuildJail jail, GraphiteMember moderator, String reason) {
		removeJailRaw(jail.getMember().getID(), moderator, reason);
	}

	public void removeJailByMemberID(String memberID, GraphiteMember moderator, String reason) {
		GuildJail j = getJail(memberID);
		if(j != null) removeJail(j, moderator, reason);
	}

	private void removeJailRaw(String memberID, GraphiteMember moderator, String reason) {
		Graphite.getMySQL().query("DELETE FROM guilds_jails WHERE GuildId = ? AND MemberId = ?", guild.getID(), memberID);
		addModLogEntry(new ModLogEntry(ModLogEntryType.UNJAIL, -1, memberID, moderator.getID(), reason));
	}

	public void addLeaveAttemptToJail(GuildJail jail) {
		Graphite.getMySQL().query("UPDATE guilds_jails SET LeaveAttempts = LeaveAttempts + 1 WHERE GuildId = ? AND MemberId = ?", guild.getID(), jail.getMember().getID());
	}

	public GuildJail createJail(GraphiteMember member, GraphiteAudioChannel channel, GraphiteMember moderator, String reason) {
		GuildJail j = new GuildJail(member, channel);
		addJail(j, -1, moderator, reason);
		return j;
	}

	public GuildJail createTempJail(GraphiteMember member, GraphiteAudioChannel channel, long durationMillis, GraphiteMember moderator, String reason) {
		GuildJail j = new GuildJail(member, channel, 0, System.currentTimeMillis() + durationMillis);
		addJail(j, durationMillis, moderator, reason);
		return j;
	}

	public List<GuildChatMute> getChatMutes() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_chatmutes WHERE GuildId = ?")) {
				s.setString(1, guild.getID());
				try(ResultSet r = s.executeQuery()) {
					List<GuildChatMute> chatMutes = new ArrayList<>();
					while(r.next()) {
						GuildChatMute j = loadChatMute(r);
						if(j != null) chatMutes.add(j);
					}
					return chatMutes;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load chat mutes from MySQL", e));
	}

	public GuildChatMute getChatMute(GraphiteMember member) {
		return getChatMute(member.getID());
	}

	public GuildChatMute getChatMute(String memberID) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_chatmutes WHERE GuildId = ? AND MemberId = ?")) {
				s.setString(1, guild.getID());
				s.setString(2, memberID);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return null;
					return loadChatMute(r);
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load chat mute from MySQL", e));
	}

	public boolean isChatMuted(GraphiteMember member) {
		return getChatMute(member) != null;
	}

	private GuildChatMute loadChatMute(ResultSet r) throws SQLException {
		GraphiteMember m = guild.getMember(r.getString("MemberId"));
		if(m == null) {
			removeChatMute(r.getString("MemberId"), guild.getSelfMember(), "Member left");
			return null;
		}

		GraphiteRole rl = guild.getRolesConfig().getMutedRoleRaw();
		if(rl == null || !m.getRoles().contains(rl)) {
			removeChatMute(r.getString("MemberId"), guild.getSelfMember(), "Muted role was removed");
			return null;
		}

		return new GuildChatMute(m, r.getLong("ExpiresAt"));
	}

	private void addChatMute(GuildChatMute chatMute, long durationMs, GraphiteMember moderator, String reason) {
		if(!chatMute.getMember().getGuild().getSelfMember().canInteract(chatMute.getMember())) return;
		guild.addRoleToMember(chatMute.getMember(), guild.getRolesConfig().getOrSetupMutedRole());
		Graphite.getMySQL().query("INSERT INTO guilds_chatmutes(GuildId, MemberId, ExpiresAt) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE ExpiresAt = VALUES(ExpiresAt)", guild.getID(), chatMute.getMember().getID(), chatMute.getExpirationTimeRaw());
		addModLogEntry(new ModLogEntry(chatMute.isTemporary() ? ModLogEntryType.TEMP_CHATMUTE : ModLogEntryType.CHATMUTE, durationMs, chatMute.getMember().getID(), moderator.getID(), reason));
	}

	public void removeChatMute(GuildChatMute chatMute, GraphiteMember moderator, String reason) {
		guild.removeRoleFromMember(chatMute.getMember(), guild.getRolesConfig().getOrSetupMutedRole());
		removeChatMute(chatMute.getMember().getID(), moderator, reason);
	}

	private void removeChatMute(String memberID, GraphiteMember moderator, String reason) {
		Graphite.getMySQL().query("DELETE FROM guilds_chatmutes WHERE GuildId = ? AND MemberId = ?", guild.getID(), memberID);
		addModLogEntry(new ModLogEntry(ModLogEntryType.CHATUNMUTE, -1, memberID, moderator.getID(), reason));
	}

	public GuildChatMute createChatMute(GraphiteMember member, GraphiteMember moderator, String reason) {
		GuildChatMute c = new GuildChatMute(member);
		addChatMute(c, -1, moderator, reason);
		return c;
	}

	public GuildChatMute createTempChatMute(GraphiteMember member, long durationMillis, GraphiteMember moderator, String reason) {
		GuildChatMute c = new GuildChatMute(member, System.currentTimeMillis() + durationMillis);
		addChatMute(c, durationMillis, moderator, reason);
		return c;
	}

	public void addModLogEntry(ModLogEntry entry) {
		Graphite.getMySQL().query("INSERT INTO guilds_modlog(GuildId, `Type`, Timestamp, ActionDuration, MemberId, ModeratorId, Reason) VALUES(?, ?, ?, ?, ?, ?, ?)", guild.getID(), entry.getType().name(), entry.getTimestamp(), entry.getActionDuration(), entry.getMemberID(), entry.getModeratorID(), entry.getReason());

		JSONObject o = new JSONObject();
		o.put("modlogEntry", entry.toWebinterfaceObject());
		Graphite.getWebinterface().sendRequestToGuildUsers("updateModLogEntries", o, guild.getID(), GraphiteFeature.MODERATION);
	}

	private void deleteOldModLogEntries() {
		Graphite.getMySQL().query("DELETE FROM guilds_modlog WHERE GuildId = ? AND Timestamp < ?", guild.getID(), System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);
	}

	public List<ModLogEntry> getModLogEntries() {
		deleteOldModLogEntries();
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_modlog WHERE GuildId = ? ORDER BY Timestamp DESC")) {
				s.setString(1, guild.getID());
				try(ResultSet r = s.executeQuery()) {
					List<ModLogEntry> entries = new ArrayList<>();
					while(r.next()) {
						try {
							ModLogEntryType type = ModLogEntryType.valueOf(r.getString("Type"));
							entries.add(new ModLogEntry(type, r.getLong("Timestamp"), r.getLong("ActionDuration"), r.getString("MemberId"), r.getString("ModeratorId"), r.getString("Reason")));
						}catch(IllegalArgumentException e) {
							continue;
						}
					}
					return entries;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load mod log entries from MySQL", e));
	}

}
