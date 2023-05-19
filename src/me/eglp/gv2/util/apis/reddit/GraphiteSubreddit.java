package me.eglp.gv2.util.apis.reddit;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.eglp.reddit.entity.data.Link;
import me.eglp.reddit.entity.data.Subreddit;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@JavaScriptClass(name = "Subreddit")
public class GraphiteSubreddit implements WebinterfaceObject, JSONConvertible {

	@JSONValue
	@JavaScriptValue(getter = "getSubreddit")
	private String subredditName;

	private Subreddit subreddit;

	@JSONValue
	@JavaScriptValue(getter = "getNotificationChannel", setter = "setNotificationChannel")
	private String notificationChannel;

	@JSONValue
	@JavaScriptValue(getter = "getColor", setter = "setColor")
	private int color;

	@JSONConstructor
	@JavaScriptConstructor
	public GraphiteSubreddit() {}

	public GraphiteSubreddit(String subredditName, Subreddit subreddit, String notificationChannel, int color) {
		this.subredditName = subredditName;
		this.subreddit = subreddit;
		this.notificationChannel = notificationChannel;
		this.color = color;
	}

	public Subreddit getSubredditReference() {
		return subreddit;
	}

	public String getSubreddit() {
		return subredditName;
	}

	public GraphiteGuildMessageChannel getNotificationChannel(GraphiteGuild guild) {
		if(notificationChannel == null) return null;
		return guild.getGuildMessageChannelByID(notificationChannel);
	}

	public String getNotificationChannelID() {
		return notificationChannel;
	}

	public void setNotificationChannel(GraphiteGuildMessageChannel notificationChannel) {
		this.notificationChannel = notificationChannel != null ? notificationChannel.getID() : null;
	}

	public void setNotificationChannelID(String id) {
		this.notificationChannel = id;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean isValid() {
		return subreddit != null;
	}

	public void sendNotificationMessage(GraphiteGuild guild, Link s) {
		GraphiteGuildMessageChannel notChannel = getNotificationChannel(guild);
		if(notChannel != null && notChannel.existsJDA() && notChannel.canWrite()) {
			EmbedBuilder eb = new EmbedBuilder();

			eb.setColor(color);
			eb.setAuthor("New post on " + subreddit.getURL(), subreddit.getFullURL());
			eb.setTitle(GraphiteUtil.truncateToLength(s.getTitle(), MessageEmbed.TITLE_MAX_LENGTH, true), s.getURL());
			if(s.getSelftext() != null) {
				String t = s.getSelftext();
				eb.setDescription(GraphiteUtil.truncateToLength(t, MessageEmbed.TEXT_MAX_LENGTH, true));
			}
			eb.addField("Author", s.getAuthor(), true);
			eb.addField("Content Warning", s.isOver18() ? "NSFW" : (s.isSpoiler() ? "Spoiler" : "None"), true);
			if(s.getURL().startsWith("https://i.redd.it/")) eb.setImage(s.getURL());

			notChannel.sendMessage(eb.build());
		}
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof GraphiteSubreddit)) return false;
		return ((GraphiteSubreddit) o).subredditName.equalsIgnoreCase(subredditName);
	}

	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("subreddit", getSubreddit());
	}

	@Override
	public void preDeserializeWI(JSONObject object) {
		this.subreddit = Graphite.getReddit().getRedditAPI().getAbout(object.getString("subreddit"));
	}

	@JavaScriptFunction(calling = "getSubreddits", returning = "subreddits", withGuild = true)
	public static void getSubreddits() {};

	@JavaScriptFunction(calling = "addSubreddit", returning = "subreddit", withGuild = true)
	public static void addSubreddit(@JavaScriptParameter(name = "subreddit") String subreddit, @JavaScriptParameter(name = "channel_id") String channelID) {};

	@JavaScriptFunction(calling = "removeSubreddit", withGuild = true)
	public static void removeSubreddit(@JavaScriptParameter(name = "subreddit") String subreddit) {};

	@JavaScriptFunction(calling = "updateSubreddit", withGuild = true)
	public static void updateSubreddit(@JavaScriptParameter(name = "object") JSONObject object) {};

}
