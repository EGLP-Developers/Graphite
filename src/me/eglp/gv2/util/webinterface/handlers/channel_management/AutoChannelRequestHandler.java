package me.eglp.gv2.util.webinterface.handlers.channel_management;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.util.base.guild.GraphiteCategory;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteVoiceChannel;
import me.eglp.gv2.util.base.guild.GuildAutoChannel;
import me.eglp.gv2.util.base.guild.config.GuildChannelsConfig;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class AutoChannelRequestHandler {
	
	@WebinterfaceHandler(requestMethod = "getAutoChannels", requireGuild = true, requireFeatures = GraphiteFeature.CHANNEL_MANAGEMENT)
	public static WebinterfaceResponse getAutoChannels(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		List<GuildAutoChannel> ac = g.getChannelsConfig().getAutoChannels();
		JSONObject channel = new JSONObject();
		channel.put("channels", new JSONArray(ac.stream().map(c -> c.toWebinterfaceObject()).collect(Collectors.toList())));
		
		return WebinterfaceResponse.success(channel);
	}
	
	@WebinterfaceHandler(requestMethod = "addAutoChannel", requireGuild = true, requireFeatures = GraphiteFeature.CHANNEL_MANAGEMENT)
	public static WebinterfaceResponse addAutoChannel(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		
		String id = event.getRequestData().getString("channel_id");
		
		GraphiteVoiceChannel channel = g.getVoiceChannelByID(id);
		if(channel == null) {
			return WebinterfaceResponse.error("Channel doesn't exist");
		}
		
		if(g.getChannelsConfig().isAutoChannel(channel)) {
			return WebinterfaceResponse.error("AutoChannel already exists");
		}
		
		GuildAutoChannel c = g.getChannelsConfig().createAutoChannel(channel, channel.getCategory());
		
		JSONObject o = new JSONObject();
		o.put("channel", c.toWebinterfaceObject());
		
		return WebinterfaceResponse.success(o);
	}
	
	@WebinterfaceHandler(requestMethod = "removeAutoChannel", requireGuild = true, requireFeatures = GraphiteFeature.CHANNEL_MANAGEMENT)
	public static WebinterfaceResponse removeAutoChannel(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		
		String id = event.getRequestData().getString("channel_id");
		
		GuildAutoChannel ac = g.getChannelsConfig().getAutoChannelByID(id);
		if(ac == null) {
			return WebinterfaceResponse.error("Unknown channel");
		}
		
		ac.delete();
		
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestMethod = "setAutoChannelCategory", requireGuild = true, requireFeatures = GraphiteFeature.CHANNEL_MANAGEMENT)
	public static WebinterfaceResponse setAutoChannelCategory(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildChannelsConfig cfg = g.getChannelsConfig();
		
		String id = event.getRequestData().getString("channel_id");
		GuildAutoChannel ac = cfg.getAutoChannelByID(id);
		
		String cID = event.getRequestData().getString("category_id");
		GraphiteCategory c = g.getCategoryByID(cID);
		
		if(ac == null || c == null) {
			return WebinterfaceResponse.error("Unknown channel or category");
		}
		
		cfg.setAutoChannelCategory(ac, c);
		
		return WebinterfaceResponse.success();
	}

}
