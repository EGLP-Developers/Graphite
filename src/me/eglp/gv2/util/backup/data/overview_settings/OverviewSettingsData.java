package me.eglp.gv2.util.backup.data.overview_settings;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.util.backup.IDMappings;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel;
import net.dv8tion.jda.api.entities.Guild.NotificationLevel;
import net.dv8tion.jda.api.entities.Guild.Timeout;
import net.dv8tion.jda.api.entities.Guild.VerificationLevel;
import net.dv8tion.jda.api.managers.GuildManager;

public class OverviewSettingsData implements JSONConvertible {

	@JSONValue
	private String
		serverName,
		afkChannelID,
		systemChannelID,
		notificationLevel,
		afkTimeout,
		verificationLevel,
		explicitContentLevel;

	@JSONConstructor
	private OverviewSettingsData() {}

	public OverviewSettingsData(GraphiteGuild guild) {
		if(guild.getJDAGuild() == null) throw new IllegalStateException("Unknown guild or invalid context");

		Guild g = guild.getJDAGuild();
		this.serverName = g.getName();
		this.afkChannelID = g.getAfkChannel() == null ? null : guild.getVoiceChannel(g.getAfkChannel()).getID();
		this.afkTimeout = g.getAfkTimeout().name();
		this.systemChannelID = g.getSystemChannel() == null ? null : guild.getTextChannel(g.getSystemChannel()).getID();
		this.notificationLevel = g.getDefaultNotificationLevel().name();
		this.verificationLevel = g.getVerificationLevel().name();
		this.explicitContentLevel = g.getExplicitContentLevel().name();
	}

	public String getServerName() {
		return serverName;
	}

	public String getAfkChannelID() {
		return afkChannelID;
	}

	public String getSystemChannelID() {
		return systemChannelID;
	}

	public String getNotificationLevel() {
		return notificationLevel;
	}

	public String getAfkTimeout() {
		return afkTimeout;
	}

	public String getVerificationLevel() {
		return verificationLevel;
	}

	public String getExplicitContentLevel() {
		return explicitContentLevel;
	}

	public void restore(GraphiteGuild guild, IDMappings mappings) {
		if(guild.getJDAGuild() == null) throw new IllegalStateException("Unknown guild or invalid context");
		GuildManager gM = guild.getJDAGuild().getManager();

		gM.setName(serverName);
		String newAFKChannelID = mappings.getNewID(afkChannelID);
		gM.setAfkChannel(afkChannelID == null ? null : guild.getVoiceChannelByID(newAFKChannelID).getJDAChannel());

		try {
			gM.setAfkTimeout(Timeout.valueOf(afkTimeout));
		}catch(Exception e) {}

		try {
			gM.setDefaultNotificationLevel(NotificationLevel.valueOf(notificationLevel));
		}catch(Exception e) {}

		String newSystemChannelID = mappings.getNewID(systemChannelID);
		gM.setSystemChannel(systemChannelID == null ? null : guild.getTextChannelByID(newSystemChannelID).getJDAChannel());

		try {
			gM.setVerificationLevel(VerificationLevel.valueOf(verificationLevel));
		}catch(Exception e) {}

		try {
			gM.setExplicitContentLevel(ExplicitContentLevel.valueOf(explicitContentLevel));
		}catch(Exception e) {}

		gM.complete();
	}

	public static OverviewSettingsData load(String json) {
		return JSONConverter.decodeObject(new JSONObject(json), OverviewSettingsData.class);
	}

}
