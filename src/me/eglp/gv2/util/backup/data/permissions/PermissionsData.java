package me.eglp.gv2.util.backup.data.permissions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.mysql.GraphiteMySQL.UnsafeConsumer;
import me.eglp.gv2.util.permission.EveryonePermissions;
import me.eglp.gv2.util.permission.MemberPermissions;
import me.eglp.gv2.util.permission.RolePermissions;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONListType;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import me.mrletsplay.mrcore.misc.Complex;

public class PermissionsData implements JSONConvertible {
	
	@JSONValue("everyone")
	@JSONListType(JSONType.STRING)
	private List<String> everyonePermissions;
	
	private Map<String, List<String>> rolePermissions;
	private Map<String, List<String>> memberPermissions;

	@JSONConstructor
	private PermissionsData() {
		this.everyonePermissions = new ArrayList<>();
		this.rolePermissions = new HashMap<>();
		this.memberPermissions = new HashMap<>();
	}
	
	public PermissionsData(GraphiteGuild guild) {
		this();
		Graphite.getMySQL().run((UnsafeConsumer<Connection>) con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT PermissibleType, PermissibleId, Permission FROM guilds_permissions WHERE GuildId = ?")) {
				st.setString(1, guild.getID());
				
				try(ResultSet r = st.executeQuery()) {
					while(r.next()) {
						String id = r.getString("PermissibleId");
						String perm = r.getString("Permission");
						switch(r.getString("PermissibleType")) {
							case EveryonePermissions.PERMISSIBLE_TYPE:
							{
								everyonePermissions.add(perm);
								break;
							}
							case MemberPermissions.PERMISSIBLE_TYPE:
							{
								List<String> p = memberPermissions.getOrDefault(id, new ArrayList<>());
								p.add(perm);
								memberPermissions.put(id, p);
								break;
							}
							case RolePermissions.PERMISSIBLE_TYPE:
							{
								List<String> p = rolePermissions.getOrDefault(id, new ArrayList<>());
								p.add(perm);
								rolePermissions.put(id, p);
								break;
							}
						}
					}
				}
			}
		});
	}
	
	public List<String> getEveryonePermissions() {
		return everyonePermissions;
	}
	
	public Map<String, List<String>> getMemberPermissions() {
		return memberPermissions;
	}
	
	public Map<String, List<String>> getRolePermissions() {
		return rolePermissions;
	}
	
	public void restore(GraphiteGuild guild) {
		guild.getPermissionManager().discardEverything();
		Graphite.getMySQL().run((UnsafeConsumer<Connection>) con -> {
			try(PreparedStatement st = con.prepareStatement("INSERT INTO guilds_permissions(GuildId, PermissibleType, PermissibleId, Permission) VALUES(?, ?, ?, ?)")) {
				for(String p : everyonePermissions) {
					st.setString(1, guild.getID());
					st.setString(2, EveryonePermissions.PERMISSIBLE_TYPE);
					st.setString(3, null);
					st.setString(4, p);
					st.addBatch();
				}
	
				for(Map.Entry<String, List<String>> en : memberPermissions.entrySet()) {
					for(String p : en.getValue()) {
						st.setString(1, guild.getID());
						st.setString(2, MemberPermissions.PERMISSIBLE_TYPE);
						st.setString(3, en.getKey());
						st.setString(4, p);
						st.addBatch();
					}
				}
	
				for(Map.Entry<String, List<String>> en : rolePermissions.entrySet()) {
					for(String p : en.getValue()) {
						st.setString(1, guild.getID());
						st.setString(2, RolePermissions.PERMISSIBLE_TYPE);
						st.setString(3, en.getKey());
						st.setString(4, p);
						st.addBatch();
					}
				}
				
				st.executeBatch();
			}
		});
	}
	
	@Override
	public void preSerialize(JSONObject object) {
		JSONObject members = new JSONObject();
		memberPermissions.forEach((id, p) -> members.put(id, new JSONArray(p)));
		object.put("members", members);
		
		JSONObject roles = new JSONObject();
		rolePermissions.forEach((id, p) -> roles.put(id, new JSONArray(p)));
		object.put("roles", roles);
	}
	
	@Override
	public void preDeserialize(JSONObject object) {
		object.getJSONObject("members").forEach((k, v) -> memberPermissions.put(k, new ArrayList<>(Complex.castList((JSONArray) v, String.class).get())));
		object.getJSONObject("roles").forEach((k, v) -> rolePermissions.put(k, new ArrayList<>(Complex.castList((JSONArray) v, String.class).get())));
	}
	
	public static PermissionsData load(String json) {
		return JSONConverter.decodeObject(new JSONObject(json), PermissionsData.class);
	}
	
}
