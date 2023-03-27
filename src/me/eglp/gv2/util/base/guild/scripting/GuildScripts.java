package me.eglp.gv2.util.base.guild.scripting;

import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.mysql.SQLTable;
import me.eglp.gv2.util.scripting.GraphiteScript;
import me.mrletsplay.mrcore.misc.ErroringNullableOptional;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "guilds_scripts",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"ScriptName varchar(255) NOT NULL",
		"ScriptContent blob DEFAULT NULL",
		"PRIMARY KEY (GuildId, ScriptName)"
	},
	guildReference = "GuildId"
)
public class GuildScripts {
	
	private GraphiteGuild guild;
	private List<GraphiteScript> scripts;
	
	public GuildScripts(GraphiteGuild guild) {
		this.guild = guild;
		this.scripts = loadScripts();
	}
	
	public GraphiteGuild getOwner() {
		return guild;
	}
	
	public List<GraphiteScript> getScripts() {
		return scripts;
	}
	
	public GraphiteScript getScript(String name) {
		return scripts.stream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}
	
	private List<GraphiteScript> loadScripts() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_scripts WHERE GuildId = ?")) {
				s.setString(1, guild.getID());
				try(ResultSet r = s.executeQuery()) {
					List<GraphiteScript> scripts = new ArrayList<>();
					while(r.next()) {
						Blob b = r.getBlob("ScriptContent");
						byte[] bs = b.getBytes(1, (int) b.length());
						var re = loadScript(r.getString("ScriptName"), new String(bs, StandardCharsets.UTF_8));
						if(!re.isPresent()) {
							removeScript(r.getString("ScriptName"));
							continue;
						}
						scripts.add(re.get());
					}
					return scripts;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load scripts from MySQL", e));
	}
	
	public ErroringNullableOptional<GraphiteScript, Exception> addOrReplaceScript(String scriptName, byte[] scriptData) {
		return Graphite.getMySQL().run(con -> {
			var r = loadScript(scriptName, new String(scriptData, StandardCharsets.UTF_8));
			if(!r.isPresent()) return r;
			scripts.removeIf(s -> s.getName().equalsIgnoreCase(scriptName));
			scripts.add(r.get());
			try(PreparedStatement s = con.prepareStatement("INSERT INTO guilds_scripts(GuildId, ScriptName, ScriptContent) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE ScriptContent = VALUES(ScriptContent)")) {
				s.setString(1, guild.getID());
				s.setString(2, scriptName);
				s.setBlob(3, new SerialBlob(scriptData));
				s.execute();
				return r;
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to save script to MySQL", e));
	}
	
	public void removeScript(String scriptName) {
		scripts.removeIf(s -> s.getName().equalsIgnoreCase(scriptName));
		Graphite.getMySQL().query("DELETE FROM guilds_scripts WHERE GuildId = ? AND ScriptName = ?", guild.getID(), scriptName);
	}
	
	private ErroringNullableOptional<GraphiteScript, Exception> loadScript(String scriptName, String scriptContent) {
		try {
			return ErroringNullableOptional.ofErroring(new GraphiteScript(guild, scriptName, scriptContent));
		}catch(Exception e) {
			return ErroringNullableOptional.ofErroring(e);
		}
	}
	
}
