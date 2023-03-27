package me.eglp.gv2.util.base.guild.customcommand;

import java.awt.Color;
import java.util.function.BiFunction;

import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.LocalizedString;
import me.eglp.gv2.util.webinterface.js.JavaScriptEnum;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

@JavaScriptEnum
public enum CommandParameterType implements WebinterfaceObject {

	STRING(DefaultLocaleString.CUSTOMCOMMAND_ARG_TYPE_STRING, (event, key) -> event.getRawParameters().getString(key)),
	INTEGER(DefaultLocaleString.CUSTOMCOMMAND_ARG_TYPE_INTEGER, (event, key) -> event.getRawParameters().getInt(key)),
	BOOLEAN(DefaultLocaleString.CUSTOMCOMMAND_ARG_TYPE_BOOLEAN, (event, key) -> event.getRawParameters().getBoolean(key)),
	USER(DefaultLocaleString.CUSTOMCOMMAND_ARG_TYPE_USER, (event, key) -> {
		String u = event.getRawParameters().getString(key);
		if(u.equals("sender")) return event.getCommandEvent().getAuthor();
		return event.getCommandEvent().getGuild().getMember(u);
	}),
	TEXT_CHANNEL(DefaultLocaleString.CUSTOMCOMMAND_ARG_TYPE_TEXT_CHANNEL, (event, key) -> {
		String ch = event.getRawParameters().getString(key);
		if(ch.equals("sent-from")) return event.getCommandEvent().getChannel();
		return event.getGuild().getTextChannelByID(ch);
	}),
	VOICE_CHANNEL(DefaultLocaleString.CUSTOMCOMMAND_ARG_TYPE_VOICE_CHANNEL, (event, key) -> event.getGuild().getVoiceChannelByID(event.getRawParameters().getString(key))),
	ROLE(DefaultLocaleString.CUSTOMCOMMAND_ARG_TYPE_ROLE, (event, key) -> event.getGuild().getRoleByID(event.getRawParameters().getString(key))),
	COLOR(DefaultLocaleString.CUSTOMCOMMAND_ARG_TYPE_COLOR, (event, key) -> new Color(event.getRawParameters().getInt(key))),
	;
	
	private final LocalizedString friendlyName;
	private final BiFunction<CustomCommandInvokedEvent, String, Object> valueGetter;
	
	private CommandParameterType(LocalizedString friendlyName, BiFunction<CustomCommandInvokedEvent, String, Object> valueGetter) {
		this.friendlyName = friendlyName;
		this.valueGetter = valueGetter;
	}
	
	public LocalizedString getFriendlyName() {
		return friendlyName;
	}
	
	public Object getValue(CustomCommandInvokedEvent event, String key) {
		return valueGetter.apply(event, key);
	}
	
	@JavaScriptFunction(name = "getFriendlyName", calling = "getCommandParameterFriendlyName", callingParameters = "enum_name", returning = "friendly_name", withGuild = true)
	public void getFriendlyNameJS() {}
	
}
