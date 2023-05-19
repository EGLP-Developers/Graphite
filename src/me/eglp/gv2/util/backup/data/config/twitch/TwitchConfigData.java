package me.eglp.gv2.util.backup.data.config.twitch;

import java.util.EnumSet;
import java.util.List;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.util.apis.twitch.GraphiteTwitchUser;
import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.backup.RestoreSelector;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class TwitchConfigData implements JSONConvertible {

	@JSONValue
	@JSONComplexListType(GraphiteTwitchUser.class)
	private List<GraphiteTwitchUser> twitchUsers;

	@JSONConstructor
	private TwitchConfigData() {}

	public TwitchConfigData(GraphiteGuild guild) {
		this.twitchUsers = guild.getTwitchConfig().getTwitchUsers();
	}

	public void restore(GraphiteGuild guild, EnumSet<RestoreSelector> selectors, IDMappings mappings) {
		if(RestoreSelector.TWITCH.appliesTo(selectors)) {
			twitchUsers.forEach(u -> u.setNotificationChannelID(mappings.getNewID(u.getNotificationChannelID())));
			guild.getTwitchConfig().setTwitchUsers(twitchUsers);
		}
	}


}
