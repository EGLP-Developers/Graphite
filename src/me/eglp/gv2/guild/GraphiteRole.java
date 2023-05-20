package me.eglp.gv2.guild;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.GraphiteIdentifiable;
import me.eglp.gv2.util.permission.RolePermissions;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import net.dv8tion.jda.api.entities.Role;

@JavaScriptClass(name = "Role")
public class GraphiteRole implements GraphiteIdentifiable, WebinterfaceObject {

	private String id;
	private GraphiteGuild guild;

	protected GraphiteRole(GraphiteGuild guild, String id) {
		this.guild = guild;
		this.id = id;
	}

	public Role getJDARole() {
		return guild.getJDAGuild().getRoleById(id);
	}

	public GraphiteGuild getGuild() {
		return guild;
	}

	public RolePermissions getRolePermissions() {
		return guild.getPermissionManager().getPermissions(this);
	}

	@JavaScriptGetter(name = "getName", returning = "roleName")
	public String getName() {
		return getJDARole().getName();
	}

	public String getAsMention() {
		return getJDARole().getAsMention();
	}

	@JavaScriptGetter(name = "isPublicRole", returning = "publicRole")
	public boolean isPublicRole() {
		return getJDARole().isPublicRole();
	}

	public boolean isManaged() {
		return getJDARole().isManaged();
	}

	@JavaScriptGetter(name = "getColorRaw", returning = "roleColor")
	private int getColorRaw() {
		return getJDARole().getColorRaw();
	}

	@Override
	@JavaScriptGetter(name = "getID", returning = "roleID")
	public String getID() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof GraphiteRole)) return false;
		return id.equals(((GraphiteRole)o).getID());
	}

	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("roleID", getID());
		object.put("roleName", getName());
		object.put("publicRole", isPublicRole());
		object.put("roleColor", getColorRaw());
	}

	@JavaScriptFunction(calling = "getRoles", returning = "roles", withGuild = true)
	public static void getRoles() {};

	@JavaScriptFunction(calling = "getAccessroles", returning = "accessroles", withGuild = true)
	public static void getAccessroles() {};

	@JavaScriptFunction(calling = "addAccessrole", returning = "accessrole", withGuild = true)
	public static void addAccessrole(@JavaScriptParameter(name = "id") String id) {};

	@JavaScriptFunction(calling = "removeAccessrole", withGuild = true)
	public static void removeAccessrole(@JavaScriptParameter(name = "id") String id) {};

	@JavaScriptFunction(calling = "getAutoroles", returning = "autoroles", withGuild = true)
	public static void getAutoroles() {};

	@JavaScriptFunction(calling = "addAutorole", returning = "autorole", withGuild = true)
	public static void addAutorole(@JavaScriptParameter(name = "id") String id) {};

	@JavaScriptFunction(calling = "removeAutorole", withGuild = true)
	public static void removeAutorole(@JavaScriptParameter(name = "id") String id) {};

	@JavaScriptFunction(calling = "getBotroles", returning = "botroles", withGuild = true)
	public static void getBotroles() {};

	@JavaScriptFunction(calling = "addBotrole", returning = "botrole", withGuild = true)
	public static void addBotrole(@JavaScriptParameter(name = "id") String id) {};

	@JavaScriptFunction(calling = "removeBotrole", withGuild = true)
	public static void removeBotrole(@JavaScriptParameter(name = "id") String id) {};

}
