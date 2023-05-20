package me.eglp.gv2.util.apis.twitter;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.eglp.twitter.entity.Tweet;
import me.eglp.twitter.entity.TwitterUser;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

@JavaScriptClass(name = "TwitterUser")
public class GraphiteTwitterUser implements WebinterfaceObject, JSONConvertible {

	@JSONValue
	@JavaScriptValue(getter = "getID")
	private String twitterUserID;

	private TwitterUser twitterUser;

	@JSONValue
	@JavaScriptValue(getter = "getNotificationChannel", setter = "setNotificationChannel")
	private String notificationChannel;

	@JSONValue
	@JavaScriptValue(getter = "getColor", setter = "setColor")
	private int color;

	@JSONConstructor
	@JavaScriptConstructor
	public GraphiteTwitterUser() {}

	public GraphiteTwitterUser(String twitterUserID, TwitterUser twitterUser, String notificationChannel, int color) {
		this.twitterUserID = twitterUserID;
		this.twitterUser = twitterUser;
		this.notificationChannel = notificationChannel;
		this.color = color;
	}

	public String getID() {
		return twitterUserID;
	}

	@JavaScriptGetter(name = "getName", returning = "name")
	private String getName() {
		return twitterUser.getName();
	}

	@JavaScriptGetter(name = "getProfileImageURL", returning = "profileImageURL")
	private String getProfileImageURL() {
		return twitterUser.getOriginalProfileImageURL();
	}

	public void setNotificationChannel(GraphiteGuildMessageChannel notificationChannel) {
		this.notificationChannel = notificationChannel != null ? notificationChannel.getID() : null;
	}

	public void setNotificationChannelID(String id) {
		this.notificationChannel = id;
	}

	public GraphiteGuildMessageChannel getNotificationChannel(GraphiteGuild guild) {
		if(notificationChannel == null) return null;
		return guild.getTextChannelByID(notificationChannel);
	}

	public String getNotificationChannelID() {
		return notificationChannel;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean isValid() {
		return twitterUser != null;
	}

	public TwitterUser getTwitterUser() {
		return twitterUser;
	}

	public Message sendNotificationMessage(GraphiteGuild guild, Tweet tweet) {
		GraphiteGuildMessageChannel notChannel = getNotificationChannel(guild);
		if(notChannel != null && notChannel.canWrite()) {
			MessageCreateBuilder b = new MessageCreateBuilder();
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(color);
			eb.setAuthor(twitterUser.getName(), "https://twitter.com/" + twitterUser.getUsername() + "/status/" + tweet.getID(), twitterUser.getProfileImageURL());
			eb.setDescription(tweet.getText());
			eb.setTimestamp(tweet.getCreatedAt());
			b.setEmbeds(eb.build());
			return notChannel.sendMessageComplete(b.build());
		}
		return null;
	}

	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("id", getID());
		object.put("name", getName());
		object.put("profileImageURL", getProfileImageURL());
	}

	@Override
	public void preDeserializeWI(JSONObject object) {
		this.twitterUser = Graphite.getTwitter().getTwitterAPI().getUserByID(object.getString("id"));
	}

	@JavaScriptFunction(calling = "getTwitterUsers", returning = "users", withGuild = true)
	public static void getTwitterUsers() {};

	@JavaScriptFunction(calling = "addTwitterUser", returning = "user", withGuild = true)
	public static void addTwitterUser(@JavaScriptParameter(name = "user") String user, @JavaScriptParameter(name = "channel_id") String channelID) {};

	@JavaScriptFunction(calling = "removeTwitterUser", withGuild = true)
	public static void removeTwitterUser(@JavaScriptParameter(name = "user_id") String userID) {};

	@JavaScriptFunction(calling = "updateTwitterUser", withGuild = true)
	public static void updateTwitterUser(@JavaScriptParameter(name = "object") JSONObject object) {};

}
