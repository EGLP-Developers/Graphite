package me.eglp.gv2.util.backup.data.config.twitter;

import java.util.EnumSet;
import java.util.List;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.util.apis.twitter.GraphiteTwitterUser;
import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.backup.RestoreSelector;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class TwitterConfigData implements JSONConvertible {

	@JSONValue
	@JSONComplexListType(GraphiteTwitterUser.class)
	private List<GraphiteTwitterUser> twitterUsers;

	@JSONConstructor
	private TwitterConfigData() {}

	public TwitterConfigData(GraphiteGuild guild) {
		this.twitterUsers = guild.getTwitterConfig().getTwitterUsers();
	}

	public void restore(GraphiteGuild guild, EnumSet<RestoreSelector> selectors, IDMappings mappings) {
		if(RestoreSelector.TWITTER.appliesTo(selectors)) {
			twitterUsers.forEach(u -> u.setNotificationChannelID(mappings.getNewID(u.getNotificationChannelID())));
			guild.getTwitterConfig().setTwitterUsers(twitterUsers);
		}
	}


}
