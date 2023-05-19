package me.eglp.gv2.util.webinterface.handlers;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.modlog.ModLogEntry;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class ModLogRequestHandler {

	@WebinterfaceHandler(requestMethod = "getModLogEntries", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse getModLogEntries(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		List<ModLogEntry> mle = g.getModerationConfig().getModLogEntries();
		JSONObject o = new JSONObject();
		o.put("entries", new JSONArray(mle.stream().map(e -> e.toWebinterfaceObject()).collect(Collectors.toList())));
		return WebinterfaceResponse.success(o);
	}

}
