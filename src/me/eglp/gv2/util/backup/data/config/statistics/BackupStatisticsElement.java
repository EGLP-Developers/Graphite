package me.eglp.gv2.util.backup.data.config.statistics;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteTextChannel;
import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.stats.element.GuildStatisticsElement;
import me.eglp.gv2.util.stats.element.StatisticsElementSettings;
import me.eglp.gv2.util.stats.element.StatisticsElementType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class BackupStatisticsElement implements JSONConvertible {

	@JSONValue
	private StatisticsElementType type;

	@JSONValue
	private StatisticsElementSettings settings;

	@JSONValue
	private String channelID;

	@JSONConstructor
	private BackupStatisticsElement() {}

	public BackupStatisticsElement(GuildStatisticsElement element) {
		this.type = element.getType();
		this.settings = element.getSettings();
		this.channelID = element.getChannelID();
	}

	public void restore(GraphiteGuild guild, IDMappings mappings) {
		GuildStatisticsElement newEl = guild.getStatisticsConfig().createStatisticsElement(type, settings);
		if(channelID != null) {
			String newChannelID = mappings.getNewID(channelID);
			GraphiteTextChannel ch = guild.getTextChannelByID(newChannelID);
			if(ch != null) newEl.sendMessage(ch);
		}
	}

}
