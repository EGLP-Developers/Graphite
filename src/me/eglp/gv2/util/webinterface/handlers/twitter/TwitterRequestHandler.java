package me.eglp.gv2.util.webinterface.handlers.twitter;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.util.apis.twitter.GraphiteTwitterUser;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.eglp.gv2.util.webinterface.js.ObjectSerializer;
import me.eglp.twitter.entity.TwitterUser;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class TwitterRequestHandler {
	
	@WebinterfaceHandler(requestMethod = "getTwitterUsers", requireGuild = true, requireFeatures = GraphiteFeature.TWITTER)
	public static WebinterfaceResponse getTwitterUsers(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		List<GraphiteTwitterUser> users = g.getTwitterConfig().getTwitterUsers();
		JSONObject o = new JSONObject();
		o.put("users", new JSONArray(users.stream().map(s -> s.toWebinterfaceObject()).collect(Collectors.toList())));
		return WebinterfaceResponse.success(o);
	}
	
	@WebinterfaceHandler(requestMethod = "addTwitterUser", requireGuild = true, requireFeatures = GraphiteFeature.TWITTER)
	public static WebinterfaceResponse addTwitterUser(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String user = event.getRequestData().getString("user");
		String channel = event.getRequestData().getString("channel_id");
		
		GraphiteGuildMessageChannel tc = g.getGuildMessageChannelByID(channel);
		if(tc == null) {
			return WebinterfaceResponse.error("Channel doesn't exist");
		}
		
		TwitterUser u = Graphite.getTwitter().getTwitterAPI().getUserByUsername(user);
		if(u == null) {
			return WebinterfaceResponse.error("User doesn't exist on Twitter");
		}
		
		if(g.getTwitterConfig().getTwitterUserByName(user) != null) {
			return WebinterfaceResponse.error("User already added");
		}
		
		GraphiteTwitterUser tu = g.getTwitterConfig().createTwitterUser(u, tc);
		
		JSONObject o = new JSONObject();
		o.put("user", tu.toWebinterfaceObject());
		
		return WebinterfaceResponse.success(o);
	}
	
	@WebinterfaceHandler(requestMethod = "removeTwitterUser", requireGuild = true, requireFeatures = GraphiteFeature.TWITTER)
	public static WebinterfaceResponse removeTwitterUser(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String id = event.getRequestData().getString("user_id");
		GraphiteTwitterUser tu = g.getTwitterConfig().getTwitterUserByID(id);
		if(tu == null) {
			return WebinterfaceResponse.error("Can't find user in your notification list");
		}
		g.getTwitterConfig().removeTwitterUser(tu);
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestMethod = "updateTwitterUser", requireGuild = true, requireFeatures = GraphiteFeature.TWITTER)
	public static WebinterfaceResponse updateTwitterUser(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		JSONObject o = event.getRequestData().getJSONObject("object");
		GraphiteTwitterUser sets = (GraphiteTwitterUser) ObjectSerializer.deserialize(o);
		if(sets == null) {
			return WebinterfaceResponse.error("User doesn't exist in your notification list");
		}
		g.getTwitterConfig().updateTwitterUser(sets);
		return WebinterfaceResponse.success();
	}
	
}
