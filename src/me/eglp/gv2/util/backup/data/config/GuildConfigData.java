package me.eglp.gv2.util.backup.data.config;

import java.util.EnumSet;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.backup.RestoreSelector;
import me.eglp.gv2.util.backup.data.config.channels.ChannelsConfigData;
import me.eglp.gv2.util.backup.data.config.customcommand.CustomCommandsConfigData;
import me.eglp.gv2.util.backup.data.config.greeter.GreeterConfigData;
import me.eglp.gv2.util.backup.data.config.reddit.RedditConfigData;
import me.eglp.gv2.util.backup.data.config.roles.RolesConfigData;
import me.eglp.gv2.util.backup.data.config.statistics.StatisticsConfigData;
import me.eglp.gv2.util.backup.data.config.twitch.TwitchConfigData;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class GuildConfigData implements JSONConvertible {

	@JSONValue
	private ChannelsConfigData channelsConfig;

	@JSONValue
	private GreeterConfigData greeterConfig;

	@JSONValue
	private RolesConfigData rolesConfig;

	@JSONValue
	private CustomCommandsConfigData customCommandsConfig;

	@JSONValue
	private StatisticsConfigData statisticsConfig;

	@JSONValue
	private RedditConfigData redditConfig;

	@JSONValue
	private TwitchConfigData twitchConfig;

	@JSONConstructor
	private GuildConfigData() {}

	public GuildConfigData(GraphiteGuild guild) {
		this.channelsConfig = new ChannelsConfigData(guild);
		this.greeterConfig = new GreeterConfigData(guild);
		this.rolesConfig = new RolesConfigData(guild);
		this.customCommandsConfig = new CustomCommandsConfigData(guild);
		this.statisticsConfig = new StatisticsConfigData(guild);
		this.redditConfig = new RedditConfigData(guild);
		this.twitchConfig = new TwitchConfigData(guild);
	}

	public static GuildConfigData load(String json) {
		return JSONConverter.decodeObject(new JSONObject(json), GuildConfigData.class);
	}

	public void restore(GraphiteGuild guild, EnumSet<RestoreSelector> selectors, IDMappings mappings) {
		if(channelsConfig != null) channelsConfig.restore(guild, selectors, mappings);
		if(greeterConfig != null) greeterConfig.restore(guild, selectors, mappings);
		if(rolesConfig != null) rolesConfig.restore(guild, selectors, mappings);
		if(customCommandsConfig != null) customCommandsConfig.restore(guild, selectors, mappings);
		if(statisticsConfig != null) statisticsConfig.restore(guild, selectors, mappings);
		if(redditConfig != null) redditConfig.restore(guild, selectors, mappings);
		if(twitchConfig != null) twitchConfig.restore(guild, selectors, mappings);
	}

}
