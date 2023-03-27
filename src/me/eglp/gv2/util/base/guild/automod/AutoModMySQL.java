package me.eglp.gv2.util.base.guild.automod;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "guilds_infractions",
	columns = {
		"Id varchar(255) NOT NULL DEFAULT 'uuid()'",
		"Timestamp bigint NOT NULL",
		"GuildId varchar(255) NOT NULL",
		"UserId varchar(255) NOT NULL",
		"Type varchar(255) NOT NULL",
		"PRIMARY KEY (Id)"
	},
	guildReference = "GuildId"
)
public class AutoModMySQL {
	
	public static void addInfraction(String guildID, String userID, String type) {
		Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("INSERT INTO guilds_infractions(Id, Timestamp, GuildId, UserId, Type) VALUES(uuid(), ?, ?, ?, ?)")) {
				s.setLong(1, System.currentTimeMillis());
				s.setString(2, guildID);
				s.setString(3, userID);
				s.setString(4, type);
				s.execute();
			}
		});
	}
	
	public static List<Long> getInfractionTimestamps(String guildID, String userID) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT Timestamp FROM guilds_infractions WHERE GuildId = ? AND UserId = ?")) {
				s.setString(1, guildID);
				s.setString(2, userID);
				
				try(ResultSet r = s.executeQuery()) {
					List<Long> timestamps = new ArrayList<>();
					while(r.next()) timestamps.add(r.getLong("Timestamp"));
					return timestamps;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve infractions from MySQL", e));
	}
	
	public static void removeOldInfractions() {
		Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("DELETE FROM guilds_infractions WHERE Timestamp < ?")) {
				s.setLong(1, System.currentTimeMillis() - 365 * 24 * 60 * 60 * 1000);
				s.execute();
			}
		});
	}

}
