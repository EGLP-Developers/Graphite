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

public class AutoroleRequestHandler {

	@WebinterfaceHandler(requestMethod = "getAutoroles", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_ROLE_MANAGEMENT)
	public static WebinterfaceResponse getAutoroles(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		List<GraphiteRole> roles = g.getRolesConfig().getAutoRoles();
		JSONObject o = new JSONObject();
		o.put("autoroles", new JSONArray(roles.stream().map(r -> r.toWebinterfaceObject()).collect(Collectors.toList())));
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "addAutorole", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_ROLE_MANAGEMENT)
	public static WebinterfaceResponse addAutorole(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String id = event.getRequestData().getString("id");
		GraphiteRole r = g.getRoleByID(id);
		if(r == null) {
			return WebinterfaceResponse.error("Role doesn't exist");
		}
		GuildRolesConfig c = g.getRolesConfig();
		if(c.isRoleAuto(r)) {
			return WebinterfaceResponse.error("Role is already a autorole");
		}
		c.addAutoRole(r);
		JSONObject o = new JSONObject();
		o.put("autorole", r.toWebinterfaceObject());
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "removeAutorole", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_ROLE_MANAGEMENT)
	public static WebinterfaceResponse removeAutorole(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String id = event.getRequestData().getString("id");
		GraphiteRole r = g.getRoleByID(id);
		if(r == null) {
			return WebinterfaceResponse.error("Role doesn't exist");
		}
		GuildRolesConfig c = g.getRolesConfig();
		c.removeAutoRole(r);
		return WebinterfaceResponse.success();
	}

}
