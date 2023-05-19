package me.eglp.gv2.util.webinterface.handlers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class FeatureRequestHandler {

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getAvailableFeatures", requireGuild = true, requireBot = true)
	public static WebinterfaceResponse getAvailableFeatures(WebinterfaceRequestEvent event) {
		JSONArray a = GraphiteMultiplex.getCurrentBot().getBotInfo().getFeatures().stream()
				.map(f -> f.toWebinterfaceObject())
				.collect(Collectors.toCollection(JSONArray::new));

		JSONObject o = new JSONObject();
		o.put("available_features", a);

		return WebinterfaceResponse.success(o);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getPermittedFeatures", requireGuild = true)
	public static WebinterfaceResponse getPermittedFeatures(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();

		List<Object> fs = Arrays.stream(GraphiteFeature.values())
				.filter(f -> g.hasFeaturesAvailable(f)
						&& (f.getWebinterfacePermission() == null || g.getPermissionManager().hasPermission(event.getUser().getDiscordUser(), f.getWebinterfacePermission())))
				.map(GraphiteFeature::toWebinterfaceObject)
				.collect(Collectors.toList());

		JSONObject o = new JSONObject();
		o.put("permittedFeatures", new JSONArray(fs));

		return WebinterfaceResponse.success(o);
	}

}
