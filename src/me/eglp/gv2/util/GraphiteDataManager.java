package me.eglp.gv2.util;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.bot.GlobalBot;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "global_guilds",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"DeleteAt bigint DEFAULT -1",
		"PRIMARY KEY (GuildId)"
	}
)
public class GraphiteDataManager {
	
	private Map<String, String> guildReferences;
	
	public GraphiteDataManager() {
		this.guildReferences = new HashMap<>();
	}
	
	public void addGuildReference(String tableName, String columnName) {
		guildReferences.put(tableName, columnName);
	}
	
	public void addGuild(String guildID) {
		Graphite.getMySQL().query("INSERT INTO global_guilds(GuildId, DeleteAt) VALUES(?, -1) ON DUPLICATE KEY UPDATE DeleteAt = -1", guildID);
	}
	
	public void updateGuilds() {
		Graphite.withBot(GlobalBot.INSTANCE, () -> {
			Graphite.getMySQL().run(con -> {
				try(PreparedStatement s = con.prepareStatement("INSERT INTO global_guilds(GuildId, DeleteAt) VALUES(?, -1) ON DUPLICATE KEY UPDATE DeleteAt = -1")) {
					for(GraphiteGuild guild : Graphite.getGuilds()) {
						s.setString(1, guild.getID());
						s.addBatch();
					}
					
					s.executeBatch();
				}
			});
		});
	}
	
	public void purgeGuilds() {
		if(!Graphite.isOnline()) return;
		
		Graphite.withBot(GlobalBot.INSTANCE, () -> {
			List<String> savedGuilds = Graphite.getMySQL().queryArray(String.class, "SELECT GuildId FROM global_guilds WHERE DeleteAt = -1")
				.orElseThrowOther(e -> new FriendlyException("Failed to load stored guilds from MySQL", e));
			
			List<String> newlyRemovedGuilds = new ArrayList<>();
			for(String s : savedGuilds) {
				if(Graphite.getGuild(s) == null) {
					newlyRemovedGuilds.add(s);
				}
			}
			
			Graphite.getMySQL().run(con -> {
				long deleteAt = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000;
				try(PreparedStatement s = con.prepareStatement("UPDATE global_guilds SET DeleteAt = ? WHERE GuildId = ?")) {
					for(String r : newlyRemovedGuilds) {
						s.setLong(1, deleteAt);
						s.setString(2, r);
						s.addBatch();
					}
					
					s.executeBatch();
				}
			});
		});
		
		List<String> deletedGuilds = Graphite.getMySQL().queryArray(String.class, "DELETE FROM global_guilds WHERE DeleteAt != -1 AND DeleteAt < ? RETURNING GuildId", System.currentTimeMillis())
				.orElseThrowOther(e -> new FriendlyException("Failed to delete guilds from MySQL", e));
		
		new HashMap<>(guildReferences).forEach((tableName, columnName) -> {
			Graphite.getMySQL().run(con -> {
				try(PreparedStatement s = con.prepareStatement("DELETE FROM " + tableName + " WHERE " + columnName + " = ?")) {
					for(String id : deletedGuilds) {
						s.setString(1, id);
						s.addBatch();
					}
					
					s.executeBatch();
				}
			});
		});
	}

}
