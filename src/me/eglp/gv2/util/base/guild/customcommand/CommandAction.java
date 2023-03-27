package me.eglp.gv2.util.base.guild.customcommand;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandAction implements WebinterfaceObject, JSONConvertible {
	
	@JSONValue
	@JavaScriptValue(getter = "getType", setter = "setType")
	private CommandActionType type;

	@JSONValue
	@JSONComplexListType(CommandActionPropertyRef.class)
	@JavaScriptValue(getter = "getPropertyRefs", setter = "setPropertyRefs")
	private List<CommandActionPropertyRef> propertyRefs;
	
	@JSONConstructor
	@JavaScriptConstructor
	private CommandAction() {}
	
	public CommandAction(CommandActionType type, List<CommandActionPropertyRef> propertyRefs) {
		this.type = type;
		this.propertyRefs = propertyRefs;
	}
	
	public CommandActionType getType() {
		return type;
	}
	
	public List<CommandActionPropertyRef> getPropertyRefs() {
		return propertyRefs;
	}
	
	public List<OptionData> getOptions() {
		return propertyRefs.stream()
			.filter(p -> p.isArgument())
			.map(p -> {
				CommandActionProperty prop = type.getProperty(p.getForProperty());
				switch(prop.getType()) {
					case BOOLEAN:
						return new OptionData(OptionType.BOOLEAN, p.getArgumentName(), prop.getFriendlyName(), true);
					case COLOR:
						return new OptionData(OptionType.STRING, p.getArgumentName(), prop.getFriendlyName(), true);
					case INTEGER:
						return new OptionData(OptionType.INTEGER, p.getArgumentName(), prop.getFriendlyName(), true);
					case ROLE:
						return new OptionData(OptionType.ROLE, p.getArgumentName(), prop.getFriendlyName(), true);
					case STRING:
						return new OptionData(OptionType.STRING, p.getArgumentName(), prop.getFriendlyName(), true);
					case TEXT_CHANNEL:
						return new OptionData(OptionType.CHANNEL, p.getArgumentName(), prop.getFriendlyName(), true).setChannelTypes(ChannelType.TEXT);
					case USER:
						return new OptionData(OptionType.USER, p.getArgumentName(), prop.getFriendlyName(), true);
					case VOICE_CHANNEL:
						return new OptionData(OptionType.CHANNEL, p.getArgumentName(), prop.getFriendlyName(), true).setChannelTypes(ChannelType.VOICE);
					default:
						break;
				}
				return null;
			})
			.collect(Collectors.toList());
	}
	
}
