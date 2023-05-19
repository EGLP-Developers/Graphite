package me.eglp.gv2.util.webinterface.handlers;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.customcommand.GraphiteCustomCommand;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.eglp.gv2.util.webinterface.js.ObjectSerializer;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class CommandRequestHandler {

	@WebinterfaceHandler(requestMethod = "getCustomCommands", requireGuild = true, requireFeatures = GraphiteFeature.CUSTOM_COMMANDS)
	public static WebinterfaceResponse getCustomCommands(WebinterfaceRequestEvent event) {
		JSONObject obj = new JSONObject();
		List<GraphiteCustomCommand> commands = event.getSelectedGuild().getCustomCommandsConfig().getCustomCommands();
		obj.put("commands", new JSONArray(commands.stream().map(GraphiteCustomCommand::toWebinterfaceObject).collect(Collectors.toList())));
		return WebinterfaceResponse.success(obj);
	}

	@WebinterfaceHandler(requestMethod = "getCustomCommandByName", requireGuild = true, requireFeatures = GraphiteFeature.CUSTOM_COMMANDS)
	public static WebinterfaceResponse getCustomCommandByName(WebinterfaceRequestEvent event) {
		String name = event.getRequestData().getString("name");

		GraphiteCustomCommand cc = event.getSelectedGuild().getCustomCommandsConfig().getCustomCommandByName(name);
		JSONObject obj = new JSONObject();
		obj.put("command", cc == null ? null : cc.toWebinterfaceObject());

		return WebinterfaceResponse.success(obj);
	}

	@WebinterfaceHandler(requestMethod = "createCustomCommand", requireGuild = true, requireFeatures = GraphiteFeature.CUSTOM_COMMANDS)
	public static WebinterfaceResponse createCustomCommand(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();

		String name = event.getRequestData().getString("name");

		if(!GraphiteCustomCommand.NAME_PATTERN.matcher(name).matches()) {
			return WebinterfaceResponse.error("Name contains invalid characters, only lowercase alphanumeric names (3-32 characters) are allowed");
		}

		if(g.getCustomCommandsConfig().getCustomCommandByName(name) != null) {
			return WebinterfaceResponse.error("CustomCommand already added");
		}

		GraphiteCustomCommand cc = event.getSelectedGuild().getCustomCommandsConfig().createCustomCommand(name);

		JSONObject obj = new JSONObject();
		obj.put("command", cc.toWebinterfaceObject());

		return WebinterfaceResponse.success(obj);
	}

	@WebinterfaceHandler(requestMethod = "updateCustomCommand", requireGuild = true, requireFeatures = GraphiteFeature.CUSTOM_COMMANDS)
	public static WebinterfaceResponse updateCustomCommand(WebinterfaceRequestEvent event) {
		JSONObject obj = event.getRequestData().getJSONObject("command");

		GraphiteCustomCommand cmd = (GraphiteCustomCommand) ObjectSerializer.deserialize(obj);

		GraphiteCustomCommand cc = event.getSelectedGuild().getCustomCommandsConfig().getCustomCommandByName(cmd.getName());
		if(cc == null) cc = new GraphiteCustomCommand(cmd.getName());

		cc.setPermission(cmd.getPermission());
		cc.setActions(cmd.getActions());

		if(!cc.checkValid()) {
			return WebinterfaceResponse.error("Missing action parameters or invalid parameter names");
		}

		event.getSelectedGuild().getCustomCommandsConfig().addOrUpdateCustomCommand(cc);

		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "deleteCustomCommand", requireGuild = true, requireFeatures = GraphiteFeature.CUSTOM_COMMANDS)
	public static WebinterfaceResponse deleteCustomCommand(WebinterfaceRequestEvent event) {
		String name = event.getRequestData().getString("name");

		GraphiteGuild g =  event.getSelectedGuild();
		GraphiteCustomCommand cc = g.getCustomCommandsConfig().getCustomCommandByName(name);
		if(cc == null) {
			return WebinterfaceResponse.error("CustomCommand not found");
		}

		g.getCustomCommandsConfig().removeCustomCommand(cc);

		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "deleteAllCustomCommands", requireGuild = true, requireFeatures = GraphiteFeature.CUSTOM_COMMANDS)
	public static WebinterfaceResponse deleteAllCustomCommands(WebinterfaceRequestEvent event) {
		GraphiteGuild g =  event.getSelectedGuild();
		g.getCustomCommandsConfig().removeAllCustomCommands();
		return WebinterfaceResponse.success();
	}

}
