package me.eglp.gv2.util.webinterface.handlers;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteRole;
import me.eglp.gv2.guild.GraphiteTextChannel;
import me.eglp.gv2.guild.config.GuildChannelsConfig;
import me.eglp.gv2.guild.config.GuildRolesConfig;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class ModeratorRequestHandler {

	@WebinterfaceHandler(requestMethod = "getModLogChannel", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_MODERATION)
	public static WebinterfaceResponse getModLogChannel(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GraphiteTextChannel tc = g.getChannelsConfig().getModLogChannel();
		JSONObject o = new JSONObject();
		o.put("channel", tc != null ? tc.toWebinterfaceObject() : null);
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "setModLogChannel", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_MODERATION)
	public static WebinterfaceResponse setModLogChannel(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String id = event.getRequestData().getString("id");
		GraphiteTextChannel c = g.getTextChannelByID(id);
		if(c == null) {
			return WebinterfaceResponse.error("Textchannel doesn't exist");
		}

		g.getChannelsConfig().setModLogChannel(c);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "unsetModLogChannel", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_MODERATION)
	public static WebinterfaceResponse unsetModLogChannel(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildChannelsConfig c = g.getChannelsConfig();
		if(c.getModLogChannel() != null) {
			g.getChannelsConfig().unsetModLogChannel();
		}

		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "getModeratorRoles", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_MODERATION)
	public static WebinterfaceResponse getModeratorRoles(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		JSONArray arr = new JSONArray();
		for(GraphiteRole r : g.getRolesConfig().getModeratorRoles()) {
			arr.add(r.toWebinterfaceObject());
		}
		JSONObject o = new JSONObject();
		o.put("moderator", arr);
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "addModeratorRole", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_MODERATION)
	public static WebinterfaceResponse addModeratorRole(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildRolesConfig c = g.getRolesConfig();
		String id = event.getRequestData().getString("id");
		GraphiteRole r = g.getRoleByID(id);
		if(r == null) {
			return WebinterfaceResponse.error("Role doesn't exist");
		}

		if(c.isModeratorRole(r)) {
			return WebinterfaceResponse.error("Role is already a supporter role");
		}

		c.addModeratorRole(r);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "removeModeratorRole", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_MODERATION)
	public static WebinterfaceResponse removeModeratorRole(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildRolesConfig c = g.getRolesConfig();
		String id = event.getRequestData().getString("id");
		GraphiteRole r = g.getRoleByID(id);
		if(r == null) {
			return WebinterfaceResponse.error("Role doesn't exist");
		}

		if(!c.isModeratorRole(r)) {
			return WebinterfaceResponse.error("Role is already removed. Try to refresh the site");
		}

		c.removeModeratorRole(r);
		return WebinterfaceResponse.success();
	}

}
