package me.eglp.gv2.util.apis.twitch;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.LocalizedMessage;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.eglp.twitch.entity.TwitchGame;
import me.eglp.twitch.entity.TwitchStream;
import me.eglp.twitch.entity.TwitchUser;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

@JavaScriptClass(name = "TwitchUser")
public class GraphiteTwitchUser implements WebinterfaceObject, JSONConvertible {
	
	private static final DateTimeFormatter TIME_FORMAT = new DateTimeFormatterBuilder()
			.appendPattern("EEE, dd MMM yyyy HH:mm:ss z")
			.toFormatter(Locale.UK);

	@JSONValue
	private String twitchUserID;
	
	private TwitchUser twitchUser;

	@JSONValue
	@JavaScriptValue(getter = "getNotificationChannel", setter = "setNotificationChannel")
	private String notificationChannel;

	@JSONValue
	@JavaScriptValue(getter = "getNotificationMessage", setter = "setNotificationMessage")
	private String notificationMessage;

	@JavaScriptValue(getter = "getParameters", setter = "setParameters")
	private List<TwitchAnnouncementParameter> parameters;

	@JSONValue
	@JavaScriptValue(getter = "getColor", setter = "setColor")
	private int color;
	
	@JSONConstructor
	@JavaScriptConstructor
	public GraphiteTwitchUser() {}
	
	public GraphiteTwitchUser(String twitchUserID, TwitchUser twitchUser, String notificationChannel, String notificationMessage, List<TwitchAnnouncementParameter> parameters, int color) {
		this.twitchUserID = twitchUserID;
		this.twitchUser = twitchUser;
		this.notificationChannel = notificationChannel;
		this.notificationMessage = notificationMessage;
		this.parameters = parameters;
		this.color = color;
	}
	
	public TwitchUser getTwitchUser() {
		return twitchUser;
	}

	@JavaScriptGetter(name = "getID", returning = "id")
	public String getID() {
		return twitchUserID;
	}

	@JavaScriptGetter(name = "getName", returning = "name")
	private String getName() {
		return twitchUser.getDisplayName();
	}

	@JavaScriptGetter(name = "getProfileImageURL", returning = "profileImageURL")
	private String getProfileImageURL() {
		return twitchUser.getProfileImageURL();
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
	
	public String getNotificationMessage() {
		return notificationMessage;
	}
	
	public void setNotificationMessage(String notificationMessage) {
		this.notificationMessage = notificationMessage;
	}
	
	public List<TwitchAnnouncementParameter> getParameters() {
		return parameters;
	}
	
	public void setParameters(List<TwitchAnnouncementParameter> parameters) {
		this.parameters = parameters;
	}
	
	public int getColor() {
		return color;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
	public boolean isValid() {
		return twitchUser != null;
	}
	
	public Message sendNotificationMessage(GraphiteGuild guild) {
		GraphiteGuildMessageChannel notChannel = getNotificationChannel(guild);
		if(notChannel != null && notChannel.existsJDA() && notChannel.canWrite()) {
			TwitchStream str = twitchUser.getStream();
			if(str == null) return null;
			String link = "https://twitch.tv/" + twitchUser.getLogin();
			String preview = "https://static-cdn.jtvnw.net/previews-ttv/live_user_" + twitchUser.getLogin() + ".jpg?id=" + System.currentTimeMillis();
			MessageCreateBuilder b = new MessageCreateBuilder();
			b.addContent(LocalizedMessage.formatMessage(getNotificationMessage(), "streamer", twitchUser.getDisplayName().replace("_", "\\_"), "link", link));
			
			if(!parameters.isEmpty()) {
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(color);
				eb.setAuthor(twitchUser.getDisplayName(), link, twitchUser.getProfileImageURL());
				TwitchGame game = str.getGame();
				if(parameters.contains(TwitchAnnouncementParameter.SHOW_GAME)) eb.addField(DefaultLocaleString.TWITCH_NOTIFICATION_GAME.getFor(guild), (game == null ? DefaultLocaleString.TWITCH_NOTIFICATION_GAME_NONE.getFor(guild) : game.getName()), false);
				if(parameters.contains(TwitchAnnouncementParameter.SHOW_TITLE))eb.addField(DefaultLocaleString.TWITCH_NOTIFICATION_TITLE.getFor(guild), str.getTitle(), false);
				if(parameters.contains(TwitchAnnouncementParameter.SHOW_VIEWERS)) eb.addField("Viewers", ""+str.getViewerCount(), false);
				if(parameters.contains(TwitchAnnouncementParameter.SHOW_STARTED_AT)) eb.addField("Started At", TIME_FORMAT.format(str.getStartedAt().atZone(guild.getConfig().getTimezone())), false);
				if(parameters.contains(TwitchAnnouncementParameter.SHOW_STREAMER_LINK)) eb.addField(DefaultLocaleString.TWITCH_NOTIFICATION_STREAMER.getFor(guild), "[" + twitchUser.getDisplayName() + "](" + link + ")", false);
				if(parameters.contains(TwitchAnnouncementParameter.SHOW_PROFILE_ICON)) eb.setThumbnail(twitchUser.getProfileImageURL());
				if(parameters.contains(TwitchAnnouncementParameter.SHOW_STREAM_PREVIEW)) eb.setImage(preview);
				b.setEmbeds(eb.build());
			}
			
			return notChannel.sendMessageComplete(b.build());
		}
		return null;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof GraphiteTwitchUser)) return false;
		return ((GraphiteTwitchUser) o).getTwitchUser().getID().equals(twitchUser.getID());
	}
	
	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("id", getID());
		object.put("name", getName());
		object.put("profileImageURL", getProfileImageURL());
	}
	
	@Override
	public void preDeserializeWI(JSONObject object) {
		this.twitchUser = Graphite.getTwitch().getTwitchAPI().getUserByID(object.getString("id"));
	}
	
	@Override
	public void preSerialize(JSONObject object) {
		object.put("parameters", parameters.stream().map(p -> p.toJSONPrimitive()).collect(Collectors.toCollection(JSONArray::new)));
	}
	
	@Override
	public void preDeserialize(JSONObject object) {
		this.parameters = object.getJSONArray("parameters").stream()
			.map(s -> TwitchAnnouncementParameter.decodePrimitive(s))
			.collect(Collectors.toList());
	}
	
	@JavaScriptFunction(calling = "getTwitchStreamers", returning = "streamers", withGuild = true)
	public static void getTwitchStreamers() {};
	
	@JavaScriptFunction(calling = "addTwitchStreamer", returning = "streamer", withGuild = true)
	public static void addTwitchStreamer(@JavaScriptParameter(name = "streamer_name") String streamerID, @JavaScriptParameter(name = "channel_id") String channelID) {};
	
	@JavaScriptFunction(calling = "removeTwitchStreamer", withGuild = true)
	public static void removeTwitchStreamer(@JavaScriptParameter(name = "streamer_id") String streamerID) {};
	
	@JavaScriptFunction(calling = "updateTwitchStreamer", withGuild = true)
	public static void updateTwitchStreamer(@JavaScriptParameter(name = "object") JSONObject object) {};
	
	@JavaScriptFunction(calling = "getDefaultTwitchNotificationMessage", returning = "message", withGuild = true)
	public static void getDefaultTwitchNotificationMessage() {};
	
	@JavaScriptFunction(calling = "sendTwitchAnnouncement", withGuild = true)
	public static void sendTwitchAnnouncement(@JavaScriptParameter(name = "streamer_id") String streamerID) {};
	
}
