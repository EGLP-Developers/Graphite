package me.eglp.gv2.util.webinterface.base;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.JSONType;

@JavaScriptClass(name = "WebinterfaceGuild")
public class GraphiteWebinterfaceGuild implements WebinterfaceObject {

	private GraphiteWebinterfaceUser user;
	private JSONObject rawGuild;
	
	public GraphiteWebinterfaceGuild(GraphiteWebinterfaceUser user, JSONObject rawGuild) {
		this.user = user;
		this.rawGuild = rawGuild;
	}
	
	public GraphiteWebinterfaceUser getUser() {
		return user;
	}
	
	public JSONObject getRawGuild() {
		return rawGuild;
	}
	
	@JavaScriptGetter(name = "getID", returning = "id")
	public String getID() {
		return rawGuild.getString("id");
	}

	@JavaScriptGetter(name = "getName", returning = "name")
	public String getName() {
		return rawGuild.getString("name");
	}

	@JavaScriptGetter(name = "getIconURL", returning = "iconURL")
	public String getIconURL() {
		if(!rawGuild.isOfType("icon", JSONType.STRING)) return null;
		return "https://cdn.discordapp.com/icons/" + getID() + "/" + rawGuild.getString("icon") + ".png";
	}
	
	public boolean isGraphiteGuild() {
		return getGraphiteGuild() != null;
	}
	
	public GraphiteGuild getGraphiteGuild() {
		return Graphite.getGuild(getID());
	}
	
	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("id", getID());
		object.put("name", getName());
		object.put("iconURL", getIconURL());
	}
	
	@JavaScriptFunction(calling = "getPermittedGuilds", returning = "guilds", withGuild = false)
	public static void getPermittedGuilds() {};

	
}
