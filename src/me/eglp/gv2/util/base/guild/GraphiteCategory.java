package me.eglp.gv2.util.base.guild;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.bot.GlobalBot;
import me.eglp.gv2.util.base.id.GraphiteIdentifiable;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import net.dv8tion.jda.api.entities.channel.concrete.Category;

@JavaScriptClass(name = "Category")
public class GraphiteCategory implements GraphiteIdentifiable, WebinterfaceObject, GraphiteGuildChannel {

	private String id;
	private GraphiteGuild guild;
	
	protected GraphiteCategory(GraphiteGuild guild, String id) {
		this.guild = guild;
		this.id = id;
	}

	public Category getJDACategory() {
		return getGuild().getJDAGuild().getCategoryById(id);
	}

	@Override
	public Category getJDAChannel() {
		return getJDACategory();
	}
	
	@Override
	public GraphiteGuild getGuild() {
		return guild;
	}

	@Override
	@JavaScriptGetter(name = "getName", returning = "categoryName")
	public String getName() {
		return getJDACategory().getName();
	}
	
	@Override
	@JavaScriptGetter(name = "getID", returning = "categoryID")
	public String getID() {
		return id;
	}
	
	@Override
	public GraphiteCategory getCategory() {
		return null;
	}
	
	public List<GraphiteTextChannel> getTextChannels() {
		return getJDACategory().getTextChannels().stream().map(t -> guild.getTextChannel(t)).collect(Collectors.toList());
	}
	
	public List<GraphiteNewsChannel> getNewsChannels() {
		return getJDACategory().getNewsChannels().stream().map(t -> guild.getNewsChannel(t)).collect(Collectors.toList());
	}
	
	public List<GraphiteVoiceChannel> getVoiceChannels() {
		return getJDACategory().getVoiceChannels().stream().map(t -> guild.getVoiceChannel(t)).collect(Collectors.toList());
	}
	
	public List<GraphiteStageChannel> getStageChannels() {
		return getJDACategory().getStageChannels().stream().map(t -> guild.getStageChannel(t)).collect(Collectors.toList());
	}
	
	@Override
	public boolean existsJDA() {
		return Graphite.withBot(GlobalBot.INSTANCE, () -> getJDACategory() != null);
	}
	
	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("categoryID", id.toString());
		object.put("categoryName", getName());
	}
	
	@JavaScriptFunction(calling = "getCategories", returning = "categories", withGuild = true)
	public static void getCategories() {};
	
}
