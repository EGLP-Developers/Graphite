package me.eglp.gv2.util.base.guild.customcommand;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteRole;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.base.guild.GraphiteVoiceChannel;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.mrletsplay.mrcore.json.JSONException;
import me.mrletsplay.mrcore.json.JSONObject;

public class CustomCommandInvokedEvent {

	private CommandAction action;
	private CommandInvokedEvent commandEvent;
	private GraphiteCustomCommand customCommand;
	private JSONObject parameters;
	
	public CustomCommandInvokedEvent(CommandAction action, CommandInvokedEvent commandEvent, GraphiteCustomCommand customCommand, JSONObject parameters) {
		this.action = action;
		this.commandEvent = commandEvent;
		this.customCommand = customCommand;
		this.parameters = parameters;
	}
	
	public CommandAction getAction() {
		return action;
	}
	
	public Object getParameter(String key) {
		try {
			return action.getType().getProperty(key).getType().getValue(this, key);
		}catch(JSONException e) {
			throw new CustomCommandExecutionException(this, "Parameter \"" + key + "\" is not set", e);
		}
	}
	
	public CommandInvokedEvent getCommandEvent() {
		return commandEvent;
	}
	
	public GraphiteCustomCommand getCustomCommand() {
		return customCommand;
	}
	
	public GraphiteGuild getGuild() {
		return commandEvent.getGuild();
	}
	
	public JSONObject getRawParameters() {
		return parameters;
	}
	
	public static CustomCommandInvokedEvent create(CommandInvokedEvent event, GraphiteCustomCommand command, CommandAction action) {
		JSONObject params = new JSONObject();
		List<CommandActionPropertyRef> argRefs = action.getPropertyRefs().stream()
				.filter(a -> a.isArgument())
				.collect(Collectors.toList());
		List<CommandActionPropertyRef> fixedRefs = action.getPropertyRefs().stream()
				.filter(a -> !a.isArgument())
				.collect(Collectors.toList());
		
		for(CommandActionPropertyRef f : fixedRefs) {
			params.put(f.getForProperty(), f.getValue());
		}
		
		for(CommandActionPropertyRef f : argRefs) {
			CommandActionProperty prop = action.getType().getProperty(f.getForProperty());
			Object val = verifyArg(event, f.getArgumentName(), prop.getType());
			if(val == null) {
				DefaultMessage.CUSTOMCOMMAND_INVALID_ARG_TYPE.reply(event, "arg", f.getArgumentName(), "type", prop.getType().getFriendlyName().getFor(event.getSender()));
				return null;
			}
			params.put(f.getForProperty(), val);
		}
		return new CustomCommandInvokedEvent(action, event, command, params);
	}
	
	private static Object verifyArg(CommandInvokedEvent event, String argName, CommandParameterType type) {
		switch(type) {
			case BOOLEAN:
				return (boolean) event.getOption(argName);
			case COLOR:
				int val;
				try {
					val = Integer.parseInt((String) event.getOption(argName), 16);
				}catch(NumberFormatException e) {
					return null;
				}
				return new Color(val).getRGB();
			case INTEGER:
				return (int) event.getOption(argName);
			case STRING:
				return (String) event.getOption(argName);
			case USER:
				return ((GraphiteUser) event.getOption(argName)).getID();
			case TEXT_CHANNEL:
				return ((GraphiteTextChannel) event.getOption(argName)).getID();
			case VOICE_CHANNEL:
				return ((GraphiteVoiceChannel) event.getOption(argName)).getID();
			case ROLE:
				return ((GraphiteRole) event.getOption(argName)).getID();
		}
		throw new IllegalStateException("Unimplemented parameter type: " + type);
	}
	
}
