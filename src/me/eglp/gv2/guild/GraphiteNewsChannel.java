package me.eglp.gv2.guild;

import me.eglp.gv2.util.base.GraphiteIdentifiable;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;

@JavaScriptClass(name = "NewsChannel")
public class GraphiteNewsChannel implements GraphiteIdentifiable, GraphiteGuildMessageChannel, WebinterfaceObject {

	private String id;
	private GraphiteGuild guild;

	protected GraphiteNewsChannel(GraphiteGuild guild, String id) {
		this.guild = guild;
		this.id = id;
	}

	@Override
	public NewsChannel getJDAChannel() {
		return guild.getJDAGuild().getNewsChannelById(id);
	}

	@Override
	public GraphiteGuild getGuild() {
		return guild;
	}

	@Override
	@JavaScriptGetter(name = "getName", returning = "name")
	public String getName() {
		return getJDAChannel().getName();
	}

	@Override
	@JavaScriptGetter(name = "getCategory", returning = "category")
	public GraphiteCategory getCategory() {
		Category c = getJDAChannel().getParentCategory();
		if(c == null) return null;
		return guild.getCategory(c);
	}

	@Override
	@JavaScriptGetter(name = "getID", returning = "id")
	public String getID() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof GraphiteTextChannel)) return false;
		return id.equals(((GraphiteTextChannel)o).getID());
	}

	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("id", getID());
		object.put("name", getName());
		object.put("category", getCategory() == null ? null : getCategory().toWebinterfaceObject());
	}

	@JavaScriptFunction(calling = "getNewsChannels", returning = "newschannels", withGuild = true)
	public static void getNewsChannels() {};

}
