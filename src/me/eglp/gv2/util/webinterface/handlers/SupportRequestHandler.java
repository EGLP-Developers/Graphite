package me.eglp.gv2.util.webinterface.handlers;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteVoiceChannel;
import me.eglp.gv2.guild.config.GuildChannelsConfig;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.mrletsplay.mrcore.json.JSONObject;

public class SupportRequestHandler {

	@WebinterfaceHandler(requestMethod = "getSupportQueue", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse getSupportQueue(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildChannelsConfig c = g.getChannelsConfig();
		GraphiteVoiceChannel vc = c.getSupportQueue();
		JSONObject o = new JSONObject();
		o.put("support_queue", (vc == null ? null : vc.toWebinterfaceObject()));
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "setSupportQueue", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse setSupportQueue(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildChannelsConfig c = g.getChannelsConfig();
		String channel = event.getRequestData().getString("channel");
		GraphiteVoiceChannel vc = g.getVoiceChannelByID(channel);
		if(vc == null) {
			return WebinterfaceResponse.error("Invalid voicechannel");
		}
		c.setSupportQueue(vc);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "unsetSupportQueue", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse unsetSupportQueue(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildChannelsConfig c = g.getChannelsConfig();
		c.unsetSupportQueue();
		return WebinterfaceResponse.success();
	}

}
