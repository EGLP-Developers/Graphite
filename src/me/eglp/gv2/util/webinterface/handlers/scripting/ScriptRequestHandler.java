package me.eglp.gv2.util.webinterface.handlers.scripting;

import java.util.Base64;

import me.eglp.gv2.commands.scripting.CommandScript;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.scripting.GraphiteScript;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class ScriptRequestHandler {

	@WebinterfaceHandler(requestMethod = "getScripts", requireGuild = true, requireFeatures = GraphiteFeature.SCRIPTING)
	public static WebinterfaceResponse getScripts(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		JSONArray arr = new JSONArray();
		for(GraphiteScript script : g.getScripts().getScripts()) arr.add(script.toWebinterfaceObject());
		JSONObject r = new JSONObject();
		r.put("scripts", arr);
		return WebinterfaceResponse.success(r);
	}

	@WebinterfaceHandler(requestMethod = "uploadScript", requireGuild = true, requireFeatures = GraphiteFeature.SCRIPTING)
	public static WebinterfaceResponse uploadScript(WebinterfaceRequestEvent event) {
		String name = event.getRequestData().getString("script_name");
		if(!CommandScript.SCRIPT_NAME_PATTERN.matcher(name).matches()) {
			return WebinterfaceResponse.error("Invalid name");
		}

		byte[] content = Base64.getDecoder().decode(event.getRequestData().getString("content"));
		var r = event.getSelectedGuild().getScripts().addOrReplaceScript(name, content);
		if(!r.isPresent()) {
			return WebinterfaceResponse.error("Exception: " + r.getException().toString());
		}
		
		JSONObject o = new JSONObject();
		o.put("script", r.get().toWebinterfaceObject());
		
		return WebinterfaceResponse.success(o);
	}
	
	@WebinterfaceHandler(requestMethod = "deleteGuildScript", requireGuild = true, requireFeatures = GraphiteFeature.SCRIPTING)
	public static WebinterfaceResponse deleteGuildScript(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String name = event.getRequestData().getString("script_name");
		GraphiteScript s = g.getScripts().getScript(name);
		if(s == null) {
			return WebinterfaceResponse.error("Script doesn't exist");
		}
		
		g.getScripts().removeScript(name);
		return WebinterfaceResponse.success();
	}
	
	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "blockScripts")
	public static WebinterfaceResponse blockScripts(WebinterfaceRequestEvent event) {
		GraphiteUser u = event.getUser().getDiscordUser();
		GraphiteGuild g = Graphite.getGuild(event.getRequestData().getString("guild"));
		if(g == null) return WebinterfaceResponse.error("Invalid guild");
		u.getConfig().addBlockedGuild(g);
		return WebinterfaceResponse.success(null);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "unblockScripts")
	public static WebinterfaceResponse unblockScripts(WebinterfaceRequestEvent event) {
		GraphiteUser u = event.getUser().getDiscordUser();
		GraphiteGuild g = Graphite.getGuild(event.getRequestData().getString("guild"));
		if(g == null) return WebinterfaceResponse.error("Invalid guild");
		u.getConfig().removeBlockedGuild(g);
		return WebinterfaceResponse.success(null);
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "unblockAllScripts")
	public static WebinterfaceResponse unblockAllScripts(WebinterfaceRequestEvent event) {
		GraphiteUser u = event.getUser().getDiscordUser();
		u.getConfig().removeAllBlockedGuilds();
		return WebinterfaceResponse.success(null);
	}
	
}
