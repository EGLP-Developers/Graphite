package me.eglp.gv2.util.webinterface.handlers;

import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.guild.config.GuildConfig;
import me.eglp.gv2.util.command.SpecialHelp;
import me.eglp.gv2.util.command.text.CommandHandler;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.eglp.gv2.util.webinterface.js.classes.Module;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class ModuleRequestHandler {

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getModules", requireGuild = true)
	public static WebinterfaceResponse getModules(WebinterfaceRequestEvent event) {
		JSONArray modules = new JSONArray();
		GraphiteGuild g = event.getSelectedGuild();
		for(GraphiteModule m : GraphiteModule.values()) {
			String avCmds = CommandHandler.getCommands().stream().filter(c -> {
				SpecialHelp h = c.getAnnotation(SpecialHelp.class);
				boolean hH = h!= null && h.hideSelf() && (h.hideSubCommands() || c.getSubCommands().isEmpty());
				return !hH && c.getModule() != null && c.getModule().equals(m);
			}).map(c -> c.getName()).collect(Collectors.joining(", "));
			modules.add(new Module(m.name(), m.getName(), avCmds, g.getConfig().hasModuleEnabled(m)).toWebinterfaceObject());
		}
		JSONObject res = new JSONObject();
		res.put("modules", modules);
		return WebinterfaceResponse.success(res);
	}

	@WebinterfaceHandler(requestMethod = "enableModule", requireGuild = true, requireGuildAdmin = true)
	public static WebinterfaceResponse enableModule(WebinterfaceRequestEvent event) {
		GuildConfig c = event.getSelectedGuild().getConfig();
		GraphiteModule m = GraphiteModule.getByValue(event.getRequestData().getString("module_id"));
		if(m == null) {
			return WebinterfaceResponse.error("Invalid module id");
		}
		if(c.hasModuleEnabled(m)) {
			return WebinterfaceResponse.error("Module already enabled");
		}
		c.addEnabledModule(m);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "disableModule", requireGuild = true, requireGuildAdmin = true)
	public static WebinterfaceResponse disableModule(WebinterfaceRequestEvent event) {
		GuildConfig c = event.getSelectedGuild().getConfig();
		GraphiteModule m = GraphiteModule.valueOf(event.getRequestData().getString("module_id"));
		if(m == null) {
			return WebinterfaceResponse.error("Invalid module id");
		}
		if(!c.hasModuleEnabled(m)) {
			return WebinterfaceResponse.error("Module not enabled");
		}
		c.removeEnabledModule(m);
		return WebinterfaceResponse.success();
	}

}
