package me.eglp.gv2.util.webinterface.handlers.role_management;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteRole;
import me.eglp.gv2.guild.config.GuildRolesConfig;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class BotroleRequestHandler {

	@WebinterfaceHandler(requestMethod = "getBotroles", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_ROLE_MANAGEMENT)
	public static WebinterfaceResponse getBotroles(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		List<GraphiteRole> roles = g.getRolesConfig().getBotRoles();
		JSONObject o = new JSONObject();
		o.put("botroles", new JSONArray(roles.stream().map(r -> r.toWebinterfaceObject()).collect(Collectors.toList())));
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "addBotrole", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_ROLE_MANAGEMENT)
	public static WebinterfaceResponse addBotrole(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String id = event.getRequestData().getString("id");
		GraphiteRole r = g.getRoleByID(id);
		if(r == null) {
			return WebinterfaceResponse.error("Role doesn't exist");
		}
		GuildRolesConfig c = g.getRolesConfig();
		if(c.isBotRole(r)) {
			return WebinterfaceResponse.error("Role is already a botrole");
		}
		c.addBotRole(r);
		JSONObject o = new JSONObject();
		o.put("botrole", r.toWebinterfaceObject());
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "removeBotrole", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_ROLE_MANAGEMENT)
	public static WebinterfaceResponse removeBotrole(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String id = event.getRequestData().getString("id");
		GraphiteRole r = g.getRoleByID(id);
		if(r == null) {
			return WebinterfaceResponse.error("Role doesn't exist");
		}
		GuildRolesConfig c = g.getRolesConfig();
		c.removeBotRole(r);
		return WebinterfaceResponse.success();
	}

}
