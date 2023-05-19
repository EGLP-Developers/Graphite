package me.eglp.gv2.util.backup.data.config.customcommand;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.customcommand.GraphiteCustomCommand;
import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.backup.RestoreSelector;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class CustomCommandsConfigData implements JSONConvertible {

	@JSONValue
	@JSONComplexListType(GraphiteCustomCommand.class)
	private List<GraphiteCustomCommand> customCommands;

	private Map<String, String> commandAliases;

	@JSONConstructor
	private CustomCommandsConfigData() {}

	public CustomCommandsConfigData(GraphiteGuild guild) {
		this.customCommands = guild.getCustomCommandsConfig().getCustomCommands().stream()
			.map(c -> c.clone())
			.collect(Collectors.toList());
		this.commandAliases = guild.getCustomCommandsConfig().getCommandAliases().stream()
			.collect(Collectors.toMap(a -> a.getAlias(), a -> a.getForCommand()));
	}

	public void restore(GraphiteGuild guild, EnumSet<RestoreSelector> selectors, IDMappings mappings) {
		if(RestoreSelector.CUSTOM_COMMANDS.appliesTo(selectors)) {
			customCommands.forEach(cc -> cc.remapIDs(mappings));
			guild.getCustomCommandsConfig().setCustomCommands(customCommands);
		}

		if(RestoreSelector.COMMAND_ALIASES.appliesTo(selectors)) {
			guild.getCustomCommandsConfig().setCommandAliases(commandAliases);
		}
	}

	@Override
	public void preSerialize(JSONObject object) {
		JSONObject aliasesObj = new JSONObject();
		if(commandAliases != null) commandAliases.forEach(aliasesObj::put);
		object.put("commandAliases", aliasesObj);
	}

	@Override
	public void preDeserialize(JSONObject object) {
		JSONObject aliasesObj = object.getJSONObject("commandAliases");
		commandAliases = new HashMap<>();
		aliasesObj.keySet().forEach(k -> commandAliases.put(k, aliasesObj.getString(k)));
	}

}
