package me.eglp.gv2.util.base.guild.config;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "guilds_backup_settings",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"BackupInterval int DEFAULT NULL",
		"LastAutoBackup bigint DEFAULT NULL",
		"PRIMARY KEY (GuildId)"
	},
	guildReference = "GuildId"
)
public class GuildBackupConfig {
	
	private GraphiteGuild guild;
	
	public GuildBackupConfig(GraphiteGuild guild) {
		this.guild = guild;
	}
	
	public void setBackupInterval(int days) {
		Graphite.getMySQL().query("INSERT INTO guilds_backup_settings(GuildId, BackupInterval, LastAutoBackup) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE BackupInterval = VALUES(BackupInterval)", guild.getID(), days, -1);
	}
	
	public void disableBackupInterval() {
		setBackupInterval(-1);
	}
	
	public int getBackupInterval() {
		return Graphite.getMySQL().query(Integer.class, -1, "SELECT BackupInterval FROM guilds_backup_settings WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load backup interval from MySQL", e));
	}
	
	public void setLastAutoBackup(long lastAutoBackup) {
		Graphite.getMySQL().query("INSERT INTO guilds_backup_settings(GuildId, BackupInterval, LastAutoBackup) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE LastAutoBackup = VALUES(LastAutoBackup)", guild.getID(), -1, lastAutoBackup);
	}
	
	public void setLastAutoBackupNow() {
		setLastAutoBackup(System.currentTimeMillis());
	}
	
	public long getLastAutoBackup() {
		return Graphite.getMySQL().query(Long.class, 0L, "SELECT LastAutoBackup FROM guilds_backup_settings WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load last auto backup from MySQL", e));
	}

}
