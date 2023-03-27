package me.eglp.gv2.util.stats;

import java.util.Collections;
import java.util.Map;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.webinterface.js.JavaScriptEnum;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.misc.FriendlyException;

@JavaScriptEnum
public enum GraphiteStatistic implements WebinterfaceObject {
	
	HUMANS(DefaultLocaleString.STATISTIC_HUMANS_NAME, guild -> (int) guild.getJDAGuild().findMembers(m -> !m.getUser().isBot()).get().size()),
	BOTS(DefaultLocaleString.STATISTIC_BOTS_NAME, guild -> (int) guild.getJDAGuild().findMembers(m -> m.getUser().isBot()).get().size()),
	TOTAL_MEMBERS(DefaultLocaleString.STATISTIC_TOTAL_MEMBERS_NAME, guild -> (int) guild.getJDAGuild().getMemberCount()),
//	NONBETA Implement after discord enables intends
//	ONLINE_MEMBERS(DefaultLocaleString.STATISTIC_ONLINE_MEMBERS_NAME, guild -> (int) guild.getJDAGuild().getMembers().stream().filter(m -> m.getOnlineStatus() != OnlineStatus.OFFLINE).count()),
//	OFFLINE_MEMBERS(DefaultLocaleString.STATISTIC_OFFLINE_MEMBERS_NAME, guild -> (int) guild.getJDAGuild().getMembers().stream().filter(m -> m.getOnlineStatus() == OnlineStatus.OFFLINE).count()),
	TEXT_CHANNELS(DefaultLocaleString.STATISTIC_TEXT_CHANNELS_NAME, guild -> guild.getJDAGuild().getTextChannels().size()),
	VOICE_CHANNELS(DefaultLocaleString.STATISTIC_VOICE_CHANNELS_NAME, guild -> guild.getJDAGuild().getVoiceChannels().size()),
	TOTAL_CHANNELS(DefaultLocaleString.STATISTIC_TOTAL_CHANNELS_NAME, guild -> guild.getJDAGuild().getTextChannels().size() + guild.getJDAGuild().getVoiceChannels().size()), // Excluding categories
	ROLES(DefaultLocaleString.STATISTIC_ROLES_NAME, guild -> guild.getJDAGuild().getRoles().size()),
	
	NEW_MEMBERS(DefaultLocaleString.STATISTIC_NEW_MEMBERS_NAME, true),
	NEW_MESSAGES(DefaultLocaleString.STATISTIC_NEW_MESSAGES_NAME, true),
	NEW_MESSAGES_BY_CHANNEL(DefaultLocaleString.STATISTIC_NEW_MESSAGES_BY_CHANNEL_NAME, true, true),
	MESSAGE_EMOJI(DefaultLocaleString.STATISTIC_MESSAGE_EMOJI_NAME, true, true),
	REACTION_EMOJI(DefaultLocaleString.STATISTIC_REACTION_EMOJI_NAME, true, true);
	
	private DefaultLocaleString friendlyName;
	private boolean isCumulative;
	private CategorizedStatisticValueGetter valueFunction;
	private boolean hasCategories;
	
	private GraphiteStatistic(DefaultLocaleString friendlyName, boolean isCumulative, boolean hasCategories) {
		if(!isCumulative) throw new FriendlyException("isCumulative can only be true!");
		this.friendlyName = friendlyName;
		this.isCumulative = true;
		this.hasCategories = hasCategories;
	}
	
	private GraphiteStatistic(DefaultLocaleString friendlyName, CategorizedStatisticValueGetter valueFunction, boolean hasCategories) {
		this.friendlyName = friendlyName;
		this.valueFunction = valueFunction;
		this.hasCategories = hasCategories;
	}
	
	private GraphiteStatistic(DefaultLocaleString friendlyName, boolean isCumulative) {
		this(friendlyName, isCumulative, false);
	}
	
	private GraphiteStatistic(DefaultLocaleString friendlyName, StatisticValueGetter valueFunction) {
		this(friendlyName, g -> Collections.singletonMap("default", valueFunction.getCurrentValue(g)), false);
	}
	
	public boolean isCumulative() {
		return isCumulative;
	}
	
	public Map<String, Integer> getCurrentValue(GraphiteGuild guild) {
		if(isCumulative) throw new FriendlyException("Current value is not available for cumulative stats");
		return valueFunction.getCurrentValues(guild);
	}
	
	public String getFriendlyName(GraphiteGuild guild) {
		return friendlyName.getFor(guild);
	}
	
	public boolean hasCategories() {
		return hasCategories;
	}
	
}
