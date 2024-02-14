package me.eglp.gv2.guild;

import me.eglp.gv2.util.base.GraphiteIdentifiable;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

@JavaScriptClass(name = "ThreadChannel")
public class GraphiteThreadChannel implements GraphiteIdentifiable, GraphiteGuildMessageChannel, WebinterfaceObject {

	private String id;
	private GraphiteGuild guild;

	protected GraphiteThreadChannel(GraphiteGuild guild, String id) {
		this.guild = guild;
		this.id = id;
	}

	@Override
	public ThreadChannel getJDAChannel() {
		return guild.getJDAGuild().getThreadChannelById(id);
	}

	/**
	 * @see ThreadChannel#getParentMessageChannel()
	 * @return
	 */
	public GraphiteGuildMessageChannel getParentMessageChannel() {
		return getGuild().getGuildMessageChannel(getJDAChannel().getParentMessageChannel());
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
	public GraphiteCategory getCategory() {
		return null;
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
	}

	@JavaScriptFunction(calling = "getThreadChannels", returning = "threadchannels", withGuild = true)
	public static void getThreadChannels() {};

}
