package me.eglp.gv2.util.backup.data.config.reddit;

import java.util.EnumSet;
import java.util.List;

import me.eglp.gv2.util.apis.reddit.GraphiteSubreddit;
import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.backup.RestoreSelector;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class RedditConfigData implements JSONConvertible {
	
	@JSONValue
	@JSONComplexListType(GraphiteSubreddit.class)
	private List<GraphiteSubreddit> subreddits;
	
	@JSONConstructor
	private RedditConfigData() {}

	public RedditConfigData(GraphiteGuild guild) {
		this.subreddits = guild.getRedditConfig().getSubreddits();
	}
	
	public void restore(GraphiteGuild guild, EnumSet<RestoreSelector> selectors, IDMappings mappings) {
		if(RestoreSelector.REDDIT.appliesTo(selectors)) {
			subreddits.forEach(sr -> {
				sr.setNotificationChannelID(mappings.getNewID(sr.getNotificationChannelID()));
			});
			guild.getRedditConfig().setSubreddits(subreddits);
		}
	}
	
}
