package me.eglp.gv2.guild.config;

import java.time.ZoneId;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.lang.GraphiteLocale;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "guilds_prefixes",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"Prefix varchar(255) DEFAULT NULL",
		"PRIMARY KEY (GuildId)"
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
		"TextCommands bool NOT NULL DEFAULT 0",
		"PRIMARY KEY (GuildId)"
	},
	guildReference = "GuildId"
)
public class GuildConfig {

	public static final Pattern PREFIX_PATTERN = Pattern.compile("[a-zA-Z_\\-~.!?]{1,16}");

	private GraphiteGuild guild;

	public GuildConfig(GraphiteGuild guild) {
		this.guild = guild;
	}

	public GraphiteGuild getGuild() {
		return guild;
	}

	public void setPrefix(String prefix) {
		Graphite.getMySQL().query("INSERT INTO guilds_prefixes(GuildId, Prefix) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE Prefix = VALUES(Prefix)", guild.getID(), prefix);
	}

	public String getPrefix() {
		String prefix = Graphite.getMySQL().query(String.class, null, "SELECT Prefix from guilds_prefixes WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load prefix from MySQL", e));
		if(prefix == null) prefix = Graphite.getBotInfo().getDefaultPrefix();
		return prefix;
	}

	public void addEnabledModule(GraphiteModule module) {
		Set<GraphiteModule> mods = getEnabledModules();
		if(!mods.add(module)) return;
		setEnabledModules(mods);
	}

	public void removeEnabledModule(GraphiteModule module) {
		Set<GraphiteModule> mods = getEnabledModules();
		if(!mods.remove(module)) return;
		if(module == GraphiteModule.RECORD && guild.getRecorder().isRecording()) guild.getRecorder().stop(); //NONBETA If user disables module while recording check
		if(module == GraphiteModule.MUSIC && guild.getMusic().isPlaying()) guild.getMusic().stop();
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
		Graphite.getMySQL().query("INSERT INTO guilds_settings(GuildId, Locale) VALUES(?, ?) ON DUPLICATE KEY UPDATE Locale = VALUES(Locale)", guild.getID(), localeShort);
	}

	public String getLocale() {
		String locale = Graphite.getMySQL().query(String.class, GraphiteLocale.DEFAULT_LOCALE_KEY, "SELECT Locale FROM guilds_settings WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load locale from MySQL", e));
		if(locale == null) locale = GraphiteLocale.DEFAULT_LOCALE_KEY;
		return locale;
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
		Graphite.getMySQL().query("INSERT INTO guilds_settings(GuildId, TextCommands) VALUES(?, ?) ON DUPLICATE KEY UPDATE TextCommands = VALUES(TextCommands)", guild.getID(), textCommands);
	}

	public boolean hasTextCommands() {
		boolean textCommands = Graphite.getMySQL().query(Boolean.class, false, "SELECT TextCommands FROM guilds_settings WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load text commands from MySQL", e));
		return textCommands;
	}

}
