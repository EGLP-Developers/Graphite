package me.eglp.gv2.util.webinterface.handlers;

import java.util.stream.Collectors;

import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.automod.autoactions.AutoModAutoAction;
import me.eglp.gv2.util.base.guild.automod.autoactions.AutoModPunishment;
import me.eglp.gv2.util.lang.GraphiteTimeParser;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.eglp.gv2.util.webinterface.js.ObjectSerializer;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class AutoActionRequestHandler {
	
	@WebinterfaceHandler(requestMethod = "getAutoActions", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse getAutoActions(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		
		JSONObject o = new JSONObject();
		o.put("autoactions", new JSONArray(g.getAutoModSettings().getAutoActions().stream().map(a -> a.toWebinterfaceObject()).collect(Collectors.toList())));
		
		return WebinterfaceResponse.success(o);
	}
	
	@WebinterfaceHandler(requestMethod = "addAutoAction", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse addAutoAction(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		
		String punishment = event.getRequestData().getString("punishment");
		String duration = event.getRequestData().optString("duration").orElse(null);
		int minInfractions = event.getRequestData().getInt("infractions");
		String timeframe = event.getRequestData().getString("timeframe");
		
		if(minInfractions < 1) return WebinterfaceResponse.error("Need to have at least 1 infraction");
		
		AutoModPunishment p = AutoModPunishment.valueOf(punishment);
		
		long fDuration = 0;
		if(p.requiresDuration()) {
			if(duration == null) return WebinterfaceResponse.error("Duration required");
			fDuration = GraphiteTimeParser.parseShortDuration(duration);
			if(fDuration == -1) {
				return WebinterfaceResponse.error("Invalid duration format");
			}
		}
		
		long fTimeframe = GraphiteTimeParser.parseShortDuration(timeframe);
		if(fTimeframe == -1) {
			return WebinterfaceResponse.error("Invalid timeframe format");
		}
		
		AutoModAutoAction action = new AutoModAutoAction(p, fDuration, minInfractions, fTimeframe);
		g.getAutoModSettings().addAutoAction(action);
		
		JSONObject obj = new JSONObject();
		obj.put("action", action.toWebinterfaceObject());
		
		return WebinterfaceResponse.success(obj);
	}
	
	@WebinterfaceHandler(requestMethod = "removeAutoAction", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse removeAutoAction(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		
		JSONObject o = event.getRequestData().getJSONObject("object");
		AutoModAutoAction action = (AutoModAutoAction) ObjectSerializer.deserialize(o);
		
		g.getAutoModSettings().removeAutoAction(action);
		
		return WebinterfaceResponse.success();
	}

}
