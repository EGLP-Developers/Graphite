package me.eglp.gv2.util.webinterface.handlers.role_management;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteRole;
import me.eglp.gv2.guild.config.GuildRolesConfig;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class AccessroleRequestHandler {

	@WebinterfaceHandler(requestMethod = "addAccessrole", requireGuild = true, requireFeatures = GraphiteFeature.ROLE_MANAGEMENT)
	public static WebinterfaceResponse addAccessrole(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String id = event.getRequestData().getString("id");
		GraphiteRole r = g.getRoleByID(id);
		if(r == null) {
			return WebinterfaceResponse.error("Role doesn't exist");
		}
		GuildRolesConfig c = g.getRolesConfig();
		if(c.isRoleAccessible(r)) {
			return WebinterfaceResponse.error("Role already accessible");
		}
		c.addAccessibleRole(r);
		JSONObject o = new JSONObject();
		o.put("accessrole", r.toWebinterfaceObject());
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "removeAccessrole", requireGuild = true, requireFeatures = GraphiteFeature.ROLE_MANAGEMENT)
	public static WebinterfaceResponse removeAccessrole(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String id = event.getRequestData().getString("id");
		GraphiteRole r = g.getRoleByID(id);
		if(r == null) {
			return WebinterfaceResponse.error("Role doesn't exist");
		}
		GuildRolesConfig c = g.getRolesConfig();
		c.removeAccessibleRole(r);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "getAccessroles", requireGuild = true, requireFeatures = GraphiteFeature.ROLE_MANAGEMENT)
	public static WebinterfaceResponse getAccessroles(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		List<GraphiteRole> roles = g.getRolesConfig().getAccessibleRoles();
		JSONObject o = new JSONObject();
		o.put("accessroles", new JSONArray(roles.stream().map(r -> r.toWebinterfaceObject()).collect(Collectors.toList())));
		return WebinterfaceResponse.success(o);
	}

}
