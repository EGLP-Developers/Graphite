package me.eglp.gv2.util.base.guild.automod;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.automod.autoactions.AutoModAutoAction;
import me.eglp.gv2.util.base.guild.automod.autoactions.AutoModPunishment;
import me.eglp.gv2.util.base.guild.automod.badwords.BadWordsSettings;
import me.eglp.gv2.util.base.guild.automod.discord_invites.DiscordInvitesSettings;
import me.eglp.gv2.util.base.guild.automod.excessive_caps.ExcessiveCapsSettings;
import me.eglp.gv2.util.base.guild.automod.excessive_emoji.ExcessiveEmojiSettings;
import me.eglp.gv2.util.base.guild.automod.excessive_mentions.ExcessiveMentionsSettings;
import me.eglp.gv2.util.base.guild.automod.excessive_spoilers.ExcessiveSpoilersSettings;
import me.eglp.gv2.util.base.guild.automod.external_links.ExternalLinksSettings;
import me.eglp.gv2.util.base.guild.automod.repeated_text.RepeatedTextSettings;
import me.eglp.gv2.util.base.guild.automod.zalgo.ZalgoSettings;
import me.eglp.gv2.util.mysql.SQLTable;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.SerializationOption;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "guilds_automod_settings",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"Settings json NOT NULL",
		"Type varchar(255) GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(`Settings`, '$.type'))) VIRTUAL",
		"UNIQUE KEY (GuildId, Type)"
	},
	guildReference = "GuildId"
)
@SQLTable(
	name = "guilds_automod_actions",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"Punishment varchar(255) NOT NULL",
		"PunishmentDuration bigint NOT NULL",
		"MinCount integer NOT NULL",
		"Timeframe bigint NOT NULL"
	},
	guildReference = "GuildId"
)
@JavaScriptClass(name = "AutoModSettings")
public class GuildAutoModSettings implements WebinterfaceObject{

	private GraphiteGuild guild;
	
	public GuildAutoModSettings(GraphiteGuild guild) {
		this.guild = guild;
	}
	
	public GraphiteGuild getGuild() {
		return guild;
	}
	
	public List<AutoModAutoAction> getAutoActions() {
		return Graphite.getMySQL().run(con -> {
			List<AutoModAutoAction> acs = new ArrayList<>();
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_automod_actions WHERE GuildId = ?")) {
				s.setString(1, guild.getID());
				try(ResultSet r = s.executeQuery()) {
					while(r.next()) {
						acs.add(new AutoModAutoAction(AutoModPunishment.valueOf(r.getString("Punishment")), r.getLong("PunishmentDuration"), r.getInt("MinCount"), r.getLong("Timeframe")));
					}
				}
			}
			return acs;
		}).orElseThrowOther(e -> new FriendlyException("Failed to load automod actions from MySQL", e));
	}
	
	public void addAutoAction(AutoModAutoAction action) {
		Graphite.getMySQL().query("INSERT IGNORE INTO guilds_automod_actions(GuildId, Punishment, PunishmentDuration, MinCount, Timeframe) VALUES(?, ?, ?, ?, ?)", guild.getID(), action.getPunishment().name(), action.getPunishmentDuration(), action.getMinCount(), action.getTimeframe())
		.orElseThrowOther(e -> new FriendlyException("Failed to save automod action on MySQL"));
	}
	
	public void removeAutoAction(AutoModAutoAction action) {
		Graphite.getMySQL().query("DELETE FROM guilds_automod_actions WHERE GuildId = ? AND Punishment = ? AND PunishmentDuration = ? AND MinCount = ? AND Timeframe = ?", guild.getID(), action.getPunishment().name(), action.getPunishmentDuration(), action.getMinCount(), action.getTimeframe())
		.orElseThrowOther(e -> new FriendlyException("Failed to save automod action on MySQL"));
	}
	
	public void setSettings(AbstractAutoModSettings settings) {
		Graphite.getMySQL().query("INSERT INTO guilds_automod_settings(GuildId, Settings) VALUES(?, ?) ON DUPLICATE KEY UPDATE Settings = VALUES(Settings)", guild.getID(), settings.toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString());
	}
	
	public BadWordsSettings getBadWordsSettings() {
		String val = Graphite.getMySQL().query(String.class, null, "SELECT Settings FROM guilds_automod_settings WHERE GuildId = ? AND Type = ?", guild.getID(), BadWordsSettings.TYPE)
				.orElseThrowOther(e -> new FriendlyException("Failed to load bad words settings from MySQL", e));
		if(val == null) return new BadWordsSettings();
		return JSONConverter.decodeObject(new JSONObject(val), BadWordsSettings.class);
	}

	public ExcessiveMentionsSettings getExcessiveMentionsSettings() {
		String val = Graphite.getMySQL().query(String.class, null, "SELECT Settings FROM guilds_automod_settings WHERE GuildId = ? AND Type = ?", guild.getID(), ExcessiveMentionsSettings.TYPE)
				.orElseThrowOther(e -> new FriendlyException("Failed to load excessive mentions settings from MySQL", e));
		if(val == null) return new ExcessiveMentionsSettings();
		return JSONConverter.decodeObject(new JSONObject(val), ExcessiveMentionsSettings.class);
	}
	
	public ExcessiveCapsSettings getExcessiveCapsSettings() {
		String val = Graphite.getMySQL().query(String.class, null, "SELECT Settings FROM guilds_automod_settings WHERE GuildId = ? AND Type = ?", guild.getID(), ExcessiveCapsSettings.TYPE)
				.orElseThrowOther(e -> new FriendlyException("Failed to load excessive caps settings from MySQL", e));
		if(val == null) return new ExcessiveCapsSettings();
		return JSONConverter.decodeObject(new JSONObject(val), ExcessiveCapsSettings.class);
	}
	
	public ExcessiveEmojiSettings getExcessiveEmojiSettings() {
		String val = Graphite.getMySQL().query(String.class, null, "SELECT Settings FROM guilds_automod_settings WHERE GuildId = ? AND Type = ?", guild.getID(), ExcessiveEmojiSettings.TYPE)
				.orElseThrowOther(e -> new FriendlyException("Failed to load excessive emoji settings from MySQL", e));
		if(val == null) return new ExcessiveEmojiSettings();
		return JSONConverter.decodeObject(new JSONObject(val), ExcessiveEmojiSettings.class);
	}
	
	public ExternalLinksSettings getExternalLinksSettings() {
		String val = Graphite.getMySQL().query(String.class, null, "SELECT Settings FROM guilds_automod_settings WHERE GuildId = ? AND Type = ?", guild.getID(), ExternalLinksSettings.TYPE)
				.orElseThrowOther(e -> new FriendlyException("Failed to load external links settings from MySQL", e));
		if(val == null) return new ExternalLinksSettings();
		return JSONConverter.decodeObject(new JSONObject(val), ExternalLinksSettings.class);
	}
	
	public ExcessiveSpoilersSettings getExcessiveSpoilersSettings() {
		String val = Graphite.getMySQL().query(String.class, null, "SELECT Settings FROM guilds_automod_settings WHERE GuildId = ? AND Type = ?", guild.getID(), ExcessiveSpoilersSettings.TYPE)
				.orElseThrowOther(e -> new FriendlyException("Failed to load excessive spoilers settings from MySQL", e));
		if(val == null) return new ExcessiveSpoilersSettings();
		return JSONConverter.decodeObject(new JSONObject(val), ExcessiveSpoilersSettings.class);
	}
	
	public ZalgoSettings getZalgoSettings() {
		String val = Graphite.getMySQL().query(String.class, null, "SELECT Settings FROM guilds_automod_settings WHERE GuildId = ? AND Type = ?", guild.getID(), ZalgoSettings.TYPE)
				.orElseThrowOther(e -> new FriendlyException("Failed to load zalgo settings from MySQL", e));
		if(val == null) return new ZalgoSettings();
		return JSONConverter.decodeObject(new JSONObject(val), ZalgoSettings.class);
	}
	
	public RepeatedTextSettings getRepeatedTextSettings() {
		String val = Graphite.getMySQL().query(String.class, null, "SELECT Settings FROM guilds_automod_settings WHERE GuildId = ? AND Type = ?", guild.getID(), RepeatedTextSettings.TYPE)
				.orElseThrowOther(e -> new FriendlyException("Failed to load repeated text settings from MySQL", e));
		if(val == null) return new RepeatedTextSettings();
		return JSONConverter.decodeObject(new JSONObject(val), RepeatedTextSettings.class);
	}
	
	public DiscordInvitesSettings getDiscordInvitesSettings() {
		String val = Graphite.getMySQL().query(String.class, null, "SELECT Settings FROM guilds_automod_settings WHERE GuildId = ? AND Type = ?", guild.getID(), DiscordInvitesSettings.TYPE)
				.orElseThrowOther(e -> new FriendlyException("Failed to load discord invites settings from MySQL", e));
		if(val == null) return new DiscordInvitesSettings();
		return JSONConverter.decodeObject(new JSONObject(val), DiscordInvitesSettings.class);
	}
	
	@JavaScriptFunction(calling = "getActions", returning = "actions", withGuild = true)
	private static void getActions() {}
	
	@JavaScriptFunction(calling = "setAutoModSettings", withGuild = true)
	private static void setAutoModSettings(@JavaScriptParameter(name = "object") JSONObject object) {}
	
}
