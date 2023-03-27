package me.eglp.gv2.util.base.guild.config;

import java.time.ZoneId;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.lang.GraphiteLocale;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "guilds_prefixes",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"BotIdentifier varchar(255) NOT NULL",
		"Prefix varchar(255) DEFAULT NULL",
		"PRIMARY KEY (GuildId, BotIdentifier)"
	},
	guildReference = "GuildId"
)
@SQLTable(
	name = "guilds_settings",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"Modules text DEFAULT NULL",
		"Locale varchar(255) DEFAULT NULL",
		"Timezone varchar(255) DEFAULT NULL",
		"TextCommands bool NOT NULL DEFAULT 0", // NONBETA: mysql change
		"PRIMARY KEY (GuildId)"
	},
	guildReference = "GuildId"
)
public class GuildConfig {
	
	public static final Pattern PREFIX_PATTERN = Pattern.compile("[a-zA-Z_\\-~.!?]{1,16}");
	
	private GraphiteGuild guild;
	
	// Cache some values for performance reasons
	private Map<String, String> cachedPrefix;
	private String cachedLocale;
	private Boolean textCommands;
	
	public GuildConfig(GraphiteGuild guild) {
		this.guild = guild;
		this.cachedPrefix = new HashMap<>();
	}
	
	public GraphiteGuild getGuild() {
		return guild;
	}
	
	public void setPrefix(String prefix) {
		String id = GraphiteMultiplex.getCurrentBot().getIdentifier();
		cachedPrefix.put(id, prefix);
		Graphite.getMySQL().query("INSERT INTO guilds_prefixes(GuildId, BotIdentifier, Prefix) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE Prefix = VALUES(Prefix)", guild.getID(), id, prefix);
	}
	
	private String loadPrefix(String id) {
		String prefix = Graphite.getMySQL().query(String.class, null, "SELECT Prefix from guilds_prefixes WHERE GuildId = ? AND BotIdentifier = ?", guild.getID(), id)
				.orElseThrowOther(e -> new FriendlyException("Failed to load prefix from MySQL", e));
		if(prefix == null) prefix = Graphite.getBotInfo().getDefaultPrefix();
		cachedPrefix.put(id, prefix);
		return prefix;
	}
	
	public String getPrefix() {
		String id = GraphiteMultiplex.getCurrentBot().getIdentifier();
		if(!cachedPrefix.containsKey(id)) return loadPrefix(id);
		return cachedPrefix.get(id);
	}
	
	public void addEnabledModule(GraphiteModule module) {
		Set<GraphiteModule> mods = getEnabledModules();
		if(!mods.add(module)) return;
		setEnabledModules(mods);
	}
	
	public void removeEnabledModule(GraphiteModule module) {
		Set<GraphiteModule> mods = getEnabledModules();
		if(!mods.remove(module)) return;
		Graphite.withBot(Graphite.getGraphiteBot(), () -> {
			if(module == GraphiteModule.RECORD && guild.getRecorder().isRecording()) guild.getRecorder().stop(); //NONBETA If user disables module while recording check
			if(module == GraphiteModule.MUSIC && guild.getMusic().isPlaying()) guild.getMusic().stop();
		});
		setEnabledModules(mods);
	}
	
	private void setEnabledModules(Set<GraphiteModule> modules) {
		String mods = new JSONArray(modules.stream().map(GraphiteModule::name).collect(Collectors.toList())).toString();
		Graphite.getMySQL().query("INSERT INTO guilds_settings(GuildId, Modules) VALUES(?, ?) ON DUPLICATE KEY UPDATE Modules = VALUES(Modules)", guild.getID(), mods);
	}
	
	public boolean hasModuleEnabled(GraphiteModule module) {
		return getEnabledModules().contains(module);
	}
	
	public Set<GraphiteModule> getEnabledModules() {
		String str = Graphite.getMySQL().query(String.class, null, "SELECT Modules FROM guilds_settings WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load modules from MySQL", e));
		
		if(str == null) return new HashSet<>();
		
		return new JSONArray(str).stream()
				.map(m -> {
					try {
						return GraphiteModule.valueOf((String) m);
					}catch(IllegalArgumentException e) {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(() -> EnumSet.noneOf(GraphiteModule.class)));
	}
	
	public void setLocale(String localeShort) {
		cachedLocale = localeShort;
		Graphite.getMySQL().query("INSERT INTO guilds_settings(GuildId, Locale) VALUES(?, ?) ON DUPLICATE KEY UPDATE Locale = VALUES(Locale)", guild.getID(), localeShort);
	}
	
	private String loadLocale() {
		cachedLocale = Graphite.getMySQL().query(String.class, GraphiteLocale.DEFAULT_LOCALE_KEY, "SELECT Locale FROM guilds_settings WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load locale from MySQL", e));
		if(cachedLocale == null) cachedLocale = GraphiteLocale.DEFAULT_LOCALE_KEY;
		return cachedLocale;
	}
	
	public String getLocale() {
		if(cachedLocale == null) return loadLocale();
		return cachedLocale;
	}
	
	public void setTimezone(ZoneId timezone) {
		Graphite.getMySQL().query("INSERT INTO guilds_settings(GuildId, Timezone) VALUES(?, ?) ON DUPLICATE KEY UPDATE Timezone = VALUES(Timezone)", guild.getID(), timezone.getId());
	}
	
	public ZoneId getTimezone() {
		String zone = Graphite.getMySQL().query(String.class, "UTC", "SELECT Timezone FROM guilds_settings WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load timezone from MySQL", e));
		return ZoneId.of(zone == null ? "UTC" : zone);
	}
	
	public void setTextCommands(boolean textCommands) {
		this.textCommands = textCommands;
		Graphite.getMySQL().query("INSERT INTO guilds_settings(GuildId, TextCommands) VALUES(?, ?) ON DUPLICATE KEY UPDATE TextCommands = VALUES(TextCommands)", guild.getID(), textCommands);
	}
	
	private boolean loadTextCommands() {
		textCommands = Graphite.getMySQL().query(Boolean.class, false, "SELECT TextCommands FROM guilds_settings WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load text commands from MySQL", e));
		return textCommands;
	}
	
	public boolean hasTextCommands() {
		if(textCommands == null) return loadTextCommands();
		return textCommands;
	}
	
}
