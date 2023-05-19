package me.eglp.gv2.util.webinterface.handlers.twitch;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.util.apis.twitch.GraphiteTwitchUser;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.eglp.gv2.util.webinterface.js.ObjectSerializer;
import me.eglp.twitch.entity.TwitchUser;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class TwitchRequestHandler {

	@WebinterfaceHandler(requestMethod = "getTwitchStreamers", requireGuild = true, requireFeatures = GraphiteFeature.TWITCH)
	public static WebinterfaceResponse getTwitchStreamers(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		List<GraphiteTwitchUser> streamers = g.getTwitchConfig().getTwitchUsers();
		JSONObject o = new JSONObject();
		o.put("streamers", new JSONArray(streamers.stream().map(s -> s.toWebinterfaceObject()).collect(Collectors.toList())));
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "addTwitchStreamer", requireGuild = true, requireFeatures = GraphiteFeature.TWITCH)
	public static WebinterfaceResponse addTwitchStreamer(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String streamer = event.getRequestData().getString("streamer_name");
		String channel = event.getRequestData().getString("channel_id");

		GraphiteGuildMessageChannel tc = g.getGuildMessageChannelByID(channel);
		if(tc == null) {
			return WebinterfaceResponse.error("Channel doesn't exist");
		}

		TwitchUser u = Graphite.getTwitch().getTwitchAPI().getUserByName(streamer);
		if(u == null) {
			return WebinterfaceResponse.error("Streamer doesn't exist on twitch.tv");
		}

		if(g.getTwitchConfig().getTwitchUserByName(streamer) != null) {
			return WebinterfaceResponse.error("Streamer already added");
		}

		GraphiteTwitchUser tu = g.getTwitchConfig().createTwitchUser(u, tc);

		JSONObject o = new JSONObject();
		o.put("streamer", tu.toWebinterfaceObject());

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "removeTwitchStreamer", requireGuild = true, requireFeatures = GraphiteFeature.TWITCH)
	public static WebinterfaceResponse removeTwitchStreamer(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String id = event.getRequestData().getString("streamer_id");
		GraphiteTwitchUser tu = g.getTwitchConfig().getTwitchUserByID(id);
		if(tu == null) {
			return WebinterfaceResponse.error("Can't find streamer in your notification list");
		}
		g.getTwitchConfig().removeTwitchUser(tu);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "updateTwitchStreamer", requireGuild = true, requireFeatures = GraphiteFeature.TWITCH)
	public static WebinterfaceResponse updateTwitchStreamer(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		JSONObject o = event.getRequestData().getJSONObject("object");
		GraphiteTwitchUser sets = (GraphiteTwitchUser) ObjectSerializer.deserialize(o);
		if(sets == null) {
			return WebinterfaceResponse.error("Streamer doesn't exist in your notification list");
		}
		g.getTwitchConfig().updateTwitchUser(sets);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "getDefaultTwitchNotificationMessage", requireGuild = true, requireFeatures = GraphiteFeature.TWITCH)
	public static WebinterfaceResponse getDefaultTwitchNotificationMessage(WebinterfaceRequestEvent event) {
		JSONObject obj = new JSONObject();
		obj.put("message", DefaultLocaleString.TWITCH_NOTIFICATION_DEFAULT_MESSAGE.getFor(event.getSelectedGuild()));
		return WebinterfaceResponse.success(obj);
	}

	@WebinterfaceHandler(requestMethod = "sendTwitchAnnouncement", requireGuild = true, requireFeatures = GraphiteFeature.TWITCH)
	public static WebinterfaceResponse sendTwitchAnnouncement(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String id = event.getRequestData().getString("streamer_id");
		GraphiteTwitchUser u = g.getTwitchConfig().getTwitchUserByID(id);
		if(u == null) {
			return WebinterfaceResponse.error("Can't find streamer in your notification list");
		}

		if(!g.getTwitchConfig().getWasLive(u)) {
			return WebinterfaceResponse.error("Streamer needs to be live");
		}

		if(u.getNotificationChannel(g) == null) {
			return WebinterfaceResponse.error("You need to set a notification channel first");
		}

		u.sendNotificationMessage(g);
		return WebinterfaceResponse.success();
	}

}
