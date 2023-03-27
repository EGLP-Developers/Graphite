package me.eglp.gv2.util.webinterface.handlers.greeter;

import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.base.guild.config.GuildGreeterConfig;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.mrletsplay.mrcore.json.JSONObject;

public class GreeterRequestHandler {
	
	@WebinterfaceHandler(requestMethod = "getGreeterInfo", requireGuild = true, requireFeatures = GraphiteFeature.GREETER)
	public static WebinterfaceResponse getGreeterInfo(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildGreeterConfig c = g.getGreeterConfig();
		
		JSONObject o = new JSONObject();
		o.put("info", c.toWebinterfaceObject());
		
		return WebinterfaceResponse.success(o);
	}
	
	@WebinterfaceHandler(requestMethod = "setGreeterInfo", requireGuild = true, requireFeatures = GraphiteFeature.GREETER)
	public static WebinterfaceResponse setGreeterInfo(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildGreeterConfig c = g.getGreeterConfig();
		
		JSONObject data = event.getRequestData().getJSONObject("data");
		
		String gID = data.getString("greetingChannel");
		GraphiteTextChannel gTc = gID == null ? null : g.getTextChannelByID(gID);
		c.setGreetingChannel(gTc);
		c.setGreetingMessage(data.getString("greetingMessage"));
		c.enableGreeting(data.getBoolean("greetingEnabled"));

		String fID = data.getString("farewellChannel");
		GraphiteTextChannel fTc = fID == null ? null : g.getTextChannelByID(data.getString("farewellChannel"));
		c.setFarewellChannel(fTc);
		c.setFarewellMessage(data.getString("farewellMessage"));
		c.enableFarewell(data.getBoolean("farewellEnabled"));
		
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestMethod = "unsetFarewellChannel", requireGuild = true, requireFeatures = GraphiteFeature.GREETER)
	public static WebinterfaceResponse unsetFarewellChannel(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		g.getGreeterConfig().unsetFarewellChannel();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestMethod = "unsetGreetingChannel", requireGuild = true, requireFeatures = GraphiteFeature.GREETER)
	public static WebinterfaceResponse unsetGreetingChannel(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		g.getGreeterConfig().unsetGreetingChannel();
		return WebinterfaceResponse.success();
	}

}
