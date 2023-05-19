package me.eglp.gv2.util.lang;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.util.lang.defaults.DefaultLocale;
import me.eglp.gv2.util.lang.defaults.DefaultLocales;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.config.CustomConfig;
import me.mrletsplay.mrcore.config.impl.yaml.YAMLFileCustomConfig;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "guilds_locales",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"Locale varchar(255) NOT NULL",
		"MessagePath varchar(255) NOT NULL",
		"Message text NOT NULL",
		"PRIMARY KEY (GuildId, Locale, MessagePath)"
	},
	guildReference = "GuildId"
)
public class GuildLocale implements GraphiteLocale {

	private GraphiteGuild guild;

	public GuildLocale(GraphiteGuild guild) {
		this.guild = guild;
	}

	public GraphiteGuild getGuild() {
		return guild;
	}

	@Override
	public String getString(String path, String fallback) {
		String selectedLocale = guild.getConfig().getLocale();
		return getString(selectedLocale, path, fallback);
	}

	private String getString(String localeIdentifier, String path, String fallback) {
		DefaultLocale def = DefaultLocales.getDefaultLocale(localeIdentifier);
		if(def != null) return def.getString(path, fallback);
		return getStringFromMySQL(localeIdentifier, path, fallback);
	}

	private String getStringFromMySQL(String localeIdentifier, String path, String fallback) {
		return Graphite.getMySQL().query(String.class, fallback, "SELECT Message FROM guilds_locales WHERE GuildId = ? AND Locale = ? AND MessagePath = ?", guild.getID(), localeIdentifier, path)
				.orElseThrowOther(e -> new FriendlyException("Failed to load message from MySQL", e));
	}

	public Set<String> getAvailableCustomLocales() {
		return new HashSet<>(Graphite.getMySQL().queryArray(String.class, "SELECT Locale FROM guilds_locales WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load available locales from MySQL", e)));
	}

	public Set<String> getAvailableLocales() {
		Set<String> locales = getAvailableCustomLocales();
		locales.add(DEFAULT_LOCALE_KEY);
		locales.addAll(DefaultLocales.getDefaultLocaleKeys());
		return locales;
	}

	public boolean hasLocale(String locale) {
		return getAvailableLocales().contains(locale);
	}

	public boolean hasCustomLocale(String locale) {
		return getAvailableCustomLocales().contains(locale);
	}

	public void deleteLocale(String locale) {
		Graphite.getMySQL().query("DELETE FROM guilds_locales WHERE GuildId = ? AND Locale = ?", guild.getID(), locale);
	}

	public CustomConfig generateLocaleFile(String localeIdentifier) {
		CustomConfig cc = new YAMLFileCustomConfig((File) null);
		for(Class<? extends Enum<? extends LocalizedString>> c : Graphite.getDefaultMessages()) {
			try {
				Object[] o = (Object[]) c.getMethod("values").invoke(null); // NONBETA: use getEnumConstants
				for(Object s : o) {
					LocalizedString st = (LocalizedString) s;
					cc.set(st.getMessagePath(), getString(localeIdentifier, st.getMessagePath(), st.getFallback()));
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				GraphiteDebug.log(DebugCategory.MISCELLANEOUS, e);
			}
		}
		return cc;
	}

	public void createOrOverrideLocale(String locale, Map<String, String> messages) {
		deleteLocale(locale); // Delete any existing messages for that locale

		Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("INSERT INTO guilds_locales(GuildId, Locale, MessagePath, Message) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE Message = VALUES(Message)")) {
				for(var entry : messages.entrySet()) {
					st.setString(1, guild.getID());
					st.setString(2, locale);
					st.setString(3, entry.getKey());
					st.setString(4, entry.getValue());
					st.addBatch();
				}

				st.executeBatch();
			}
		});
	}

}
