package me.eglp.gv2.util.backup.data.roles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.GraphiteRole;
import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class RolesData implements JSONConvertible, WebinterfaceObject {

	@JavaScriptValue(getter = "getRoles")
	@JSONValue
	@JSONComplexListType(BackupRole.class)
	private List<BackupRole> roles;

	private Map<String, List<String>> roleAssignments;

	@JSONConstructor
	private RolesData() {
		this.roleAssignments = new HashMap<>();
	}

	public RolesData(GraphiteGuild guild) {
		if(guild.getJDAGuild() == null) throw new IllegalStateException("Unknown guild or invalid context");

		this.roles = guild.getRoles().stream()
				.filter(r -> !r.isManaged() && shouldBackupRole(r))
				.map(BackupRole::new)
				.collect(Collectors.toList());
		this.roleAssignments = new HashMap<>();

		for(GraphiteRole r : guild.getRoles()) {
			if(r.isManaged() || !shouldBackupRole(r)) continue;
			List<String> memIDs = guild.getMembersWithAllRoles(r).stream()
					.map(m -> m.getID())
					.collect(Collectors.toList());
			roleAssignments.put(r.getID(), memIDs);
		}
	}

	private boolean shouldBackupRole(GraphiteRole role) {
		return !role.equals(role.getGuild().getRolesConfig().getMutedRoleRaw());
	}

	public List<BackupRole> getRoles() {
		return roles;
	}

	public void restore(GraphiteGuild guild, boolean restoreAssignments, IDMappings mappings) {
		Member selfMember = guild.getSelfMember().getMember();
		guild.getJDAGuild().getRoles().forEach(r -> {
			if(r.isManaged() || r.isPublicRole() || !selfMember.canInteract(r)) return;
			r.delete().complete();
		});

		for(BackupRole r : roles) {
			if(!r.restore(guild, mappings)) break;
		}

		if(restoreAssignments) {
			Map<String, List<Role>> membersToRoles = new HashMap<>();

			roleAssignments.forEach((oldRoleID, users) -> {
				String newRoleID = mappings.getNewID(oldRoleID);
				if(newRoleID == null) return; // Because of the role rate limit, the role might not have been restored
				GraphiteRole rol = guild.getRoleByID(newRoleID);
				if(rol == null) return;
				Role jdaRole = rol.getJDARole();
				if(jdaRole == null) return;
				users.forEach(u -> {
					List<Role> roles = membersToRoles.getOrDefault(u, new ArrayList<>());
					roles.add(jdaRole);
					membersToRoles.put(u, roles);
				});
			});

			membersToRoles.forEach((user, roles) -> {
				GraphiteMember m = guild.getMember(user);
				if(m == null) return;
				Member mem = m.getMember();
				if(mem == null) return;
				roles.removeIf(r -> !selfMember.canInteract(r));
				mem.getRoles().stream().filter(r -> !selfMember.canInteract(r) || r.isManaged()).forEach(roles::add);
				guild.getJDAGuild().modifyMemberRoles(mem, roles).queue();
			});
		}
	}

	@Override
	public void preSerialize(JSONObject object) {
		JSONObject o = new JSONObject();
		roleAssignments.forEach((k, v) -> o.put(k, new JSONArray(v)));
		object.put("assignments", o);
	}

	@Override
	public void preDeserialize(JSONObject object) {
		object.getJSONObject("assignments").keys().forEach(id -> {
			roleAssignments.put(id, object.getJSONArray(id).stream().map(o -> (String) o).collect(Collectors.toList()));
		});
	}

	public static RolesData load(String json) {
		return JSONConverter.decodeObject(new JSONObject(json), RolesData.class);
	}

}
