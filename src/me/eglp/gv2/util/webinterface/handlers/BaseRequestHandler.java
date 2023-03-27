package me.eglp.gv2.util.webinterface.handlers; 

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.java_websocket.WebSocket;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.base.guild.GraphiteCategory;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteMember;
import me.eglp.gv2.util.base.guild.GraphiteNewsChannel;
import me.eglp.gv2.util.base.guild.GraphiteRole;
import me.eglp.gv2.util.base.guild.GraphiteStageChannel;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.base.guild.GraphiteVoiceChannel;
import me.eglp.gv2.util.base.guild.config.GuildChannelsConfig;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.GraphiteWebinterfaceGuild;
import me.eglp.gv2.util.webinterface.base.GraphiteWebinterfaceUser;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.eglp.gv2.util.webinterface.js.ObjectSerializer;
import me.eglp.gv2.util.webinterface.js.classes.User;
import me.eglp.gv2.util.webinterface.session.WebinterfaceSession;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.JSONType;
import net.dv8tion.jda.api.Permission;

public class BaseRequestHandler {
	
	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "isOnServer")
	public static WebinterfaceResponse isOnServer(WebinterfaceRequestEvent req) {
		JSONObject o = new JSONObject();
		o.put("isOnServer", Graphite.getGlobalGuild(req.getRequestData().getString("guild")) != null);
		
		return WebinterfaceResponse.success(o);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getSelectedGuild", requireGuild = true)
	public static WebinterfaceResponse getSelectedGuild(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		
		JSONObject obj = new JSONObject();
		obj.put("guild", g.toWebinterfaceObject());
		
		return WebinterfaceResponse.success(obj);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getSelfUser")
	public static WebinterfaceResponse getSelfUser(WebinterfaceRequestEvent event) {
		GraphiteWebinterfaceUser usr = event.getUser();
		GraphiteUser u = usr.getDiscordUser();
		
		if(u == null || !u.isAvailable()) {
			return WebinterfaceResponse.error("Unknown self user");
		}
		
		JSONObject obj = new JSONObject();
		obj.put("user", new User(usr).toWebinterfaceObject());
		
		return WebinterfaceResponse.success(obj);
	}
	
	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getUserByID")
	public static WebinterfaceResponse getUserByID(WebinterfaceRequestEvent event) {
		String id = event.getRequestData().getString("id");
		
		GraphiteWebinterfaceUser usr = Graphite.getWebinterfaceUser(id);
		GraphiteUser u = usr.getDiscordUser();
		
		if(u == null || !u.isAvailable()) {
			return WebinterfaceResponse.error("Unknown user");
		}
		
		JSONObject obj = new JSONObject();
		obj.put("user", new User(usr).toWebinterfaceObject());
		
		return WebinterfaceResponse.success(obj);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "isGuildAdmin", requireGuild = true)
	public static WebinterfaceResponse isGuildAdmin(WebinterfaceRequestEvent event) {
		GraphiteWebinterfaceUser usr = event.getUser();
		GraphiteUser u = usr.getDiscordUser();
		GraphiteGuild g = event.getSelectedGuild();
		
		if(u == null || !u.isAvailable() || g.getMember(u) == null) {
			return WebinterfaceResponse.error("Unknown self user");
		}
		
		JSONObject obj = new JSONObject();
		obj.put("isAdmin", g.getMember(u).getJDAMember().hasPermission(Permission.ADMINISTRATOR));
		
		return WebinterfaceResponse.success(obj);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "isGuildOwner", requireGuild = true)
	public static WebinterfaceResponse isGuildOwner(WebinterfaceRequestEvent event) {
		GraphiteWebinterfaceUser usr = event.getUser();
		GraphiteUser u = usr.getDiscordUser();
		GraphiteGuild g = event.getSelectedGuild();
		
		if(u == null || !u.isAvailable() || g.getMember(u) == null) {
			return WebinterfaceResponse.error("Unknown self user");
		}
		
		JSONObject obj = new JSONObject();
		obj.put("isOwner", g.getMember(u).getJDAMember().isOwner());
		
		return WebinterfaceResponse.success(obj);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "init")
	public static WebinterfaceResponse init(WebinterfaceRequestEvent event) {
		JSONObject o = new JSONObject();
		o.put("classes", new JSONArray(ObjectSerializer.generateClassDescriptors()));
		return WebinterfaceResponse.success(o);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getRoles", requireGuild = true)
	public static WebinterfaceResponse getRoles(WebinterfaceRequestEvent event) {
		List<GraphiteRole> roles = event.getSelectedGuild().getRoles();
		GraphiteMember selfM = event.getSelectedGuild().getSelfMember();
		
		JSONArray arr = new JSONArray();
		for(GraphiteRole r : roles) {
			if(r.isManaged()) continue;
			if(!selfM.canInteract(r)) continue;
			arr.add(r.toWebinterfaceObject());
		}
		
		JSONObject o = new JSONObject();
		o.put("roles", arr);
		
		return WebinterfaceResponse.success(o);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getMembers", requireGuild = true)
	public static WebinterfaceResponse getMembers(WebinterfaceRequestEvent event) {
		List<GraphiteMember> members = event.getSelectedGuild().getMembers();
		GraphiteMember selfM = event.getSelectedGuild().getSelfMember();
		
		JSONArray arr = new JSONArray();
		for(GraphiteMember m : members) {
			if(m.isBot()) continue;
			if(!selfM.canInteract(m)) continue;
			arr.add(m.toWebinterfaceObject());
		}
		
		JSONObject o = new JSONObject();
		o.put("members", arr);
		
		return WebinterfaceResponse.success(o);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getPermittedGuilds")
	public static WebinterfaceResponse getPermittedGuilds(WebinterfaceRequestEvent event) {
		List<GraphiteWebinterfaceGuild> permittedGuilds = event.getUser().getGuilds();
		
		JSONObject o = new JSONObject();
		o.put("guilds", new JSONArray(permittedGuilds.stream()
				.filter(g -> g.isGraphiteGuild())
				.filter(g -> Arrays.stream(GraphiteFeature.values()).anyMatch(f -> f.getWebinterfacePermission() != null && g.getGraphiteGuild().getPermissionManager().hasPermission(g.getGraphiteGuild().getMember(event.getUser().getDiscordUser()), f.getWebinterfacePermission())))
				.map(g -> g.toWebinterfaceObject())
				.collect(Collectors.toList())));
		
		return WebinterfaceResponse.success(o);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getCategories", requireGuild = true)
	public static WebinterfaceResponse getCategories(WebinterfaceRequestEvent event) {
		List<GraphiteCategory> categories = event.getSelectedGuild().getCategories();
		
		JSONObject o = new JSONObject();
		o.put("categories", new JSONArray(categories.stream().map(c -> c.toWebinterfaceObject()).collect(Collectors.toList())));
		
		return WebinterfaceResponse.success(o);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getTextChannels", requireGuild = true)
	public static WebinterfaceResponse getTextChannels(WebinterfaceRequestEvent event) {
		List<GraphiteTextChannel> channels = event.getSelectedGuild().getTextChannels();
		
		JSONObject o = new JSONObject();
		o.put("textchannels", new JSONArray(channels.stream().map(gtc -> gtc.toWebinterfaceObject()).collect(Collectors.toList())));
		
		return WebinterfaceResponse.success(o);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getNewsChannels", requireGuild = true)
	public static WebinterfaceResponse getNewsChannels(WebinterfaceRequestEvent event) {
		List<GraphiteNewsChannel> channels = event.getSelectedGuild().getNewsChannels();
		
		JSONObject o = new JSONObject();
		o.put("newschannels", new JSONArray(channels.stream().map(gtc -> gtc.toWebinterfaceObject()).collect(Collectors.toList())));
		
		return WebinterfaceResponse.success(o);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getVoiceChannels", requireGuild = true)
	public static WebinterfaceResponse getVoiceChannels(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildChannelsConfig c = g.getChannelsConfig();
		List<GraphiteVoiceChannel> channels = g.getVoiceChannels();
		
		JSONArray arr = new JSONArray();
		for(GraphiteVoiceChannel gvc : channels) {
			if(c.isAutoCreatedChannel(gvc) || c.isUserChannel(gvc)) continue;
			arr.add(gvc.toWebinterfaceObject());
		}
		
		JSONObject o = new JSONObject();
		o.put("voicechannels", arr);
		
		return WebinterfaceResponse.success(o);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getStageChannels", requireGuild = true)
	public static WebinterfaceResponse getStageChannels(WebinterfaceRequestEvent event) {
		List<GraphiteStageChannel> channels = event.getSelectedGuild().getStageChannels();
		
		JSONObject o = new JSONObject();
		o.put("stagechannels", new JSONArray(channels.stream().map(gtc -> gtc.toWebinterfaceObject()).collect(Collectors.toList())));
		
		return WebinterfaceResponse.success(o);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getAvailableBots", requireGuild = true)
	public static WebinterfaceResponse getAvailableBots(WebinterfaceRequestEvent event) {
		List<MultiplexBot> bots = GraphiteMultiplex.getAvailableBots(event.getSelectedGuild());
		
		JSONObject o = new JSONObject();
		o.put("bots", new JSONArray(bots.stream().map(b -> b.toWebinterfaceObject()).collect(Collectors.toList())));
		
		return WebinterfaceResponse.success(o);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "restart")
	public static WebinterfaceResponse restart(WebinterfaceRequestEvent event) {
		if(!event.getUser().isAdmin()) {
			return WebinterfaceResponse.error("Not allowed");
		}
		
		Graphite.log("Bot restarted on webinterface by " + event.getUser().getDiscordUser().getName());
		Graphite.restart();
		
		return WebinterfaceResponse.success();
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "shutdown")
	public static WebinterfaceResponse shutdown(WebinterfaceRequestEvent event) {
		if(!event.getUser().isAdmin()) {
			return WebinterfaceResponse.error("Not allowed");
		}
		
		Graphite.log("Bot shutdowned on webinterface by " + event.getUser().getDiscordUser().getName());
		Graphite.shutdown(true);
		
		return WebinterfaceResponse.success();
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "isAboveUserRoles", requireGuild = true)
	public static WebinterfaceResponse isAboveUserRoles(WebinterfaceRequestEvent event) {
		boolean hR = event.getSelectedGuild().isAboveUserRoles();
		
		JSONObject o = new JSONObject();
		o.put("highest_role", hR);
		
		return WebinterfaceResponse.success(o);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getLoggedInUsers", requireGuild = true, requireGuildAdmin = true)
	public static WebinterfaceResponse getLoggedInUsers(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		JSONArray arr = new JSONArray();
		for(WebSocket socket : Graphite.getWebinterface().getWebSocketServer().getConnections()) {
			WebinterfaceSession sess = socket.getAttachment();
			if(sess == null) continue;
			if(sess.getData().isOfType("lastKnownGuildID", JSONType.STRING)
					&& sess.getData().getString("lastKnownGuildID").equals(g.getID())) {
				if(sess.getUserID().equals(event.getUser().getDiscordUser().getID())) continue;
				arr.add(new User(sess.getUser()).toWebinterfaceObject());
			}
		}

		JSONObject o = new JSONObject();
		o.put("users", arr);
		return WebinterfaceResponse.success(o);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "kickWIUser", requireGuild = true, requireGuildAdmin = true)
	public static WebinterfaceResponse kickWIUser(WebinterfaceRequestEvent event) {
		String userID = event.getRequestData().getString("user_id");
		Graphite.getWebinterface().kick(userID);
		return WebinterfaceResponse.success();
	}
	
}
