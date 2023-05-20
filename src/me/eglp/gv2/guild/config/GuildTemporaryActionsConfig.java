package me.eglp.gv2.guild.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.modlog.ModLogEntry;
import me.eglp.gv2.guild.modlog.ModLogEntryType;
import me.eglp.gv2.guild.temporary_actions.GuildTempBan;
import me.eglp.gv2.guild.temporary_actions.GuildTempVoiceMute;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "guilds_temporary_actions",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"UserId varchar(255) NOT NULL",
		"Reason longtext DEFAULT NULL",
		"ExpiresAt bigint DEFAULT NULL",
		"Type varchar(255) NOT NULL",
		"PRIMARY KEY (GuildId, UserId, Type)"
	},
	guildReference = "GuildId"
)
public class GuildTemporaryActionsConfig {

	public static final String
			TEMPORARY_ACTION_TYPE_BAN = "ban",
			TEMPORARY_ACTION_TYPE_MUTE = "mute";

	private GraphiteGuild guild;

	public GuildTemporaryActionsConfig(GraphiteGuild guild) {
		this.guild = guild;
	}

	public List<GuildTempBan> getTempBans() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT UserId, Reason, ExpiresAt FROM guilds_temporary_actions WHERE GuildId = ? AND Type = ?")) {
				st.setString(1, guild.getID());
				st.setString(2, TEMPORARY_ACTION_TYPE_BAN);

				try(ResultSet s = st.executeQuery()) {
					List<GuildTempBan> bans = new ArrayList<>();
					while(s.next()) {
						bans.add(new GuildTempBan(guild, s.getString("UserId"), s.getLong("ExpiresAt")));
					}

					return bans;
				}
			}
		}).orElse(Collections.emptyList());
	}

	public void addTempBan(GuildTempBan action, long durationMs, GraphiteMember moderator, String reason) {
		Graphite.getMySQL().query("INSERT IGNORE INTO guilds_temporary_actions(GuildId, UserId, ExpiresAt, Type) VALUES(?, ?, ?, ?)", guild.getID(), action.getUserID(), action.getExpirationTime(), TEMPORARY_ACTION_TYPE_BAN);
		guild.getModerationConfig().addModLogEntry(new ModLogEntry(ModLogEntryType.TEMP_BAN, durationMs, action.getUserID(), moderator.getID(), reason));
	}

	public void removeTempBan(GuildTempBan action, GraphiteMember moderator, String reason) {
		Graphite.getMySQL().query("DELETE FROM guilds_temporary_actions WHERE GuildId = ? AND UserId = ? AND Type = ?", guild.getID(), action.getUserID(), TEMPORARY_ACTION_TYPE_BAN);
		guild.getModerationConfig().addModLogEntry(new ModLogEntry(ModLogEntryType.UNBAN, -1, action.getUserID(), moderator.getID(), reason));
	}

	public GuildTempBan getTempBanByUserID(String userID) {
		return getTempBans().stream().filter(j -> j.getUserID().equals(userID)).findFirst().orElse(null);
	}

	public GuildTempBan tempBanMember(GraphiteMember member, long durationMs, GraphiteMember moderator, String reason) {
		if(guild.isBanned(member.getID())) throw new IllegalStateException("Member already temp-/banned");
		guild.getJDAGuild().ban(member.getMember(), 0, TimeUnit.SECONDS).reason(reason).complete();
		GuildTempBan ban = new GuildTempBan(guild, member.getID(), System.currentTimeMillis() + durationMs);
		addTempBan(ban, durationMs, moderator, reason);
		return ban;
	}

	public void removeTempBanForUser(GraphiteUser user, GraphiteMember moderator, String reason) {
		if(!guild.isBanned(user.getID())) throw new IllegalStateException("Member is not tempbanned");
		getTempBans().stream().filter(j -> j.getUserID().equals(user.getID())).collect(Collectors.toList()).forEach(b -> {
			guild.getJDAGuild().unban(user.getJDAUser()).complete();
			removeTempBan(b, moderator, reason);
		});
	}

	public List<GuildTempVoiceMute> getTempMutes() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT UserId, ExpiresAt FROM guilds_temporary_actions WHERE GuildId = ? AND Type = ?")) {
				st.setString(1, guild.getID());
				st.setString(2, TEMPORARY_ACTION_TYPE_MUTE);

				try(ResultSet s = st.executeQuery()) {
					List<GuildTempVoiceMute> mutes = new ArrayList<>();
					while(s.next()) {
						mutes.add(new GuildTempVoiceMute(guild, s.getString("UserId"), s.getLong("ExpiresAt")));
					}

					return mutes;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load temp mutes from MySQL", e));
	}

	public GuildTempVoiceMute getTempMuteByUserID(String userID) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT UserId, ExpiresAt FROM guilds_temporary_actions WHERE GuildId = ? AND Type = ? AND UserId = ?")) {
				st.setString(1, guild.getID());
				st.setString(2, TEMPORARY_ACTION_TYPE_MUTE);
				st.setString(3, userID);

				try(ResultSet s = st.executeQuery()) {
					if(!s.next()) return null;
					return new GuildTempVoiceMute(guild, s.getString("UserId"), s.getLong("ExpiresAt"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load temp mute from MySQL", e));
	}

	public void addTempMute(GuildTempVoiceMute action, long durationMs, GraphiteMember moderator, String reason) {
		Graphite.getMySQL().query("INSERT IGNORE INTO guilds_temporary_actions(GuildId, UserId, ExpiresAt, Type) VALUES(?, ?, ?, ?)", guild.getID(), action.getUserID(), action.getExpirationTime(), TEMPORARY_ACTION_TYPE_MUTE);
		guild.getModerationConfig().addModLogEntry(new ModLogEntry(ModLogEntryType.TEMP_VOICEMUTE, durationMs, action.getUserID(), moderator.getID(), reason));
	}

	public void removeTempMute(GuildTempVoiceMute action, GraphiteMember moderator, String reason) {
		Graphite.getMySQL().query("DELETE FROM guilds_temporary_actions WHERE GuildId = ? AND UserId = ? AND Type = ?", guild.getID(), action.getUserID(), TEMPORARY_ACTION_TYPE_MUTE);
		guild.getModerationConfig().addModLogEntry(new ModLogEntry(ModLogEntryType.VOICEUNMUTE, -1, action.getUserID(), moderator.getID(), reason));
	}

	public boolean isTempMuted(GraphiteUser user) {
		return getTempMuteByUserID(user.getID()) != null;
	}

	public GuildTempVoiceMute tempMuteMember(GraphiteMember member, long durationMs, GraphiteMember moderator, String reason) {
		if(isTempMuted(member)) throw new IllegalStateException("Member already temp-/muted");
		guild.getJDAGuild().mute(member.getMember(), true).complete();
		GuildTempVoiceMute mute = new GuildTempVoiceMute(guild, member.getID(), System.currentTimeMillis() + durationMs);
		addTempMute(mute, durationMs, moderator, reason);
		return mute;
	}

	public void removeTempMuteForUser(GraphiteUser user, GraphiteMember moderator, String reason) {
		GuildTempVoiceMute m = getTempMuteByUserID(user.getID());
		if(m == null) throw new IllegalStateException("Member is not tempmuted");
		m.remove(moderator, reason);
	}

}
