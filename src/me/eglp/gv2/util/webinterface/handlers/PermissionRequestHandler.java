package me.eglp.gv2.util.webinterface.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.customcommand.GraphiteCustomCommand;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.text.CommandHandler;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.permission.EveryonePermissions;
import me.eglp.gv2.util.permission.MemberPermissions;
import me.eglp.gv2.util.permission.Permission;
import me.eglp.gv2.util.permission.RolePermissions;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.eglp.gv2.util.webinterface.js.classes.JSPermission;
import me.eglp.gv2.util.webinterface.js.classes.PermissionGroup;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class PermissionRequestHandler {

	@WebinterfaceHandler(requestMethod = "getPermissionGroups", requireGuild = true, requireGuildAdmin = true)
	public static WebinterfaceResponse getPermissionGroups(WebinterfaceRequestEvent event) {
		JSONArray e = new JSONArray();

		Map<String, List<JSPermission>> perms = new HashMap<>();

		for(String permission : DefaultPermissions.getPermissions()) {
			addPermission(perms, permission, findCommands(CommandHandler.getAllCommands(), permission).stream()
					.map(c -> c.getFullName())
					.collect(Collectors.toList()));
		}

		for(GraphiteCustomCommand cc : event.getSelectedGuild().getCustomCommandsConfig().getCustomCommands()) {
			if(cc.getPermission() == null) continue;
			addPermission(perms, cc.getPermission(), Collections.singletonList(cc.getName()));
		}

		e.add(new PermissionGroup(null, Collections.singletonList(new JSPermission("*", Collections.emptyList()))).toWebinterfaceObject());
		perms.forEach((group, permissions) -> { // NONBETA: sort
			permissions.add(0, new JSPermission(group + ".*", Collections.emptyList()));
			e.add(new PermissionGroup(group, permissions).toWebinterfaceObject());
		});

		JSONObject o = new JSONObject();
		o.put("permissionGroups", e);
		return WebinterfaceResponse.success(o);
	}

	private static void addPermission(Map<String, List<JSPermission>> perms, String permission, List<String> commands) {
		String group = permission.split("\\.")[0];
		List<JSPermission> ps = perms.getOrDefault(group, new ArrayList<>());
		JSPermission p = ps.stream()
				.filter(pr -> pr.getPermission().equals(permission))
				.findFirst().orElse(null);
		if(p == null) {
			ps.add(new JSPermission(permission, commands));
			perms.put(group, ps);
		}else {
			p.getAvailableCommands().addAll(commands);
		}
	}

	private static List<Command> findCommands(List<Command> commands, String permission) {
		List<Command> res = new ArrayList<>();
		for(Command c : commands) {
			if(c.getPermission() != null && c.getPermission().equals(permission)) res.add(c);
			res.addAll(findCommands(c.getSubCommands(), permission));
		}
		return res;
	}

	@WebinterfaceHandler(requestMethod = "getMemberPermissions", requireGuild = true, requireGuildAdmin = true)
	public static WebinterfaceResponse getMemberPermissions(WebinterfaceRequestEvent event) {
		JSONArray arr = new JSONArray();
		GraphiteGuild g = event.getSelectedGuild();
		List<Permission> perms = g.getPermissionManager().getPermissions(g.getMember(event.getRequestData().getString("memberID"))).getPermissions();
		for(Permission p : perms) {
			arr.add(new JSPermission(p.getPermission(), null).toWebinterfaceObject());
		}
		JSONObject o = new JSONObject();
		o.put("permissions", arr);
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "setMemberPermission", requireGuild = true, requireGuildAdmin = true)
	public static WebinterfaceResponse setMemberPermission(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String perm = event.getRequestData().getString("permission");
		String memberID = event.getRequestData().getString("memberID");
		boolean allow = event.getRequestData().getBoolean("allow");
		MemberPermissions mp = g.getPermissionManager().getPermissions(Graphite.getUser(memberID));
		if(allow) {
			if(!mp.hasPermissionExactly(perm)) mp.addPermission(perm);
		}else {
			if(mp.hasPermissionExactly(perm)) mp.removePermission(perm);
		}
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "getRolePermissions", requireGuild = true, requireGuildAdmin = true)
	public static WebinterfaceResponse getRolePermissions(WebinterfaceRequestEvent event) {
		JSONArray arr = new JSONArray();
		for(Permission p : event.getSelectedGuild().getRoleByID(event.getRequestData().getString("roleID")).getRolePermissions().getPermissions()) {
			arr.add(new JSPermission(p.getPermission(), null).toWebinterfaceObject());
		}
		JSONObject o = new JSONObject();
		o.put("permissions", arr);
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "setRolePermission", requireGuild = true, requireGuildAdmin = true)
	public static WebinterfaceResponse setRolePermission(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String perm = event.getRequestData().getString("permission");
		String roleID = event.getRequestData().getString("roleID");
		boolean allow = event.getRequestData().getBoolean("allow");
		RolePermissions rp = g.getPermissionManager().getPermissions(g.getRoleByID(roleID));
		if(allow) {
			if(!rp.hasPermissionExactly(perm)) rp.addPermission(perm);
		}else {
			if(rp.hasPermissionExactly(perm)) rp.removePermission(perm);
		}
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "getEveryonePermissions", requireGuild = true, requireGuildAdmin = true)
	public static WebinterfaceResponse getEveryonePermissions(WebinterfaceRequestEvent event) {
		JSONArray arr = new JSONArray();
		for(Permission p : event.getSelectedGuild().getPermissionManager().getEveryonePermissions().getPermissions()) {
			arr.add(new JSPermission(p.getPermission(), null).toWebinterfaceObject());
		}
		JSONObject o = new JSONObject();
		o.put("permissions", arr);
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "setEveryonePermission", requireGuild = true, requireGuildAdmin = true)
	public static WebinterfaceResponse setEveryonePermission(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String perm = event.getRequestData().getString("permission");
		boolean allow = event.getRequestData().getBoolean("allow");
		EveryonePermissions ep = g.getPermissionManager().getEveryonePermissions();
		if(allow) {
			if(!ep.hasPermissionExactly(perm)) ep.addPermission(perm);
		}else {
			if(ep.hasPermissionExactly(perm)) ep.removePermission(perm);
		}
		return WebinterfaceResponse.success();
	}

}
