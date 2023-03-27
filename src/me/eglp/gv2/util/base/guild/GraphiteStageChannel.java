package me.eglp.gv2.util.base.guild;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.bot.GlobalBot;
import me.eglp.gv2.util.base.id.GraphiteIdentifiable;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;

@JavaScriptClass(name = "StageChannel")
public class GraphiteStageChannel implements GraphiteIdentifiable, WebinterfaceObject, GraphiteAudioChannel {

	private String id;
	private GraphiteGuild guild;
	
	protected GraphiteStageChannel(GraphiteGuild guild, String id) {
		this.guild = guild;
		this.id = id;
	}

	@Override
	public StageChannel getJDAChannel() {
		return guild.getJDAGuild().getStageChannelById(id);
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
		if(!(o instanceof GraphiteStageChannel)) return false;
		return id.equals(((GraphiteStageChannel)o).getID());
	}
	
	@Override
	public boolean existsJDA() {
		return Graphite.withBot(GlobalBot.INSTANCE, () -> getJDAChannel() != null);
	}
	
	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("id", getID());
		object.put("name", getName());
		object.put("category", getCategory() == null ? null : getCategory().toWebinterfaceObject());
	}
	
	@JavaScriptFunction(calling = "getStageChannels", returning = "stagechannels", withGuild = true)
	public static void getStageChannels() {};
	
}
