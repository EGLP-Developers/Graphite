package me.eglp.gv2.util.event.custom.impl;

import me.eglp.gv2.util.base.guild.customcommand.GraphiteCustomCommand;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.event.custom.GraphiteCustomEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Only fired for messages by users from private channels + guild channels<br/>
 * Not fired if the bot doesn't have write permissions in the channel 
 */
public class GraphiteMessageReceivedEvent implements GraphiteCustomEvent {
	
	private MessageReceivedEvent jdaEvent;
	private Command commandTriggered;
	private GraphiteCustomCommand customCommandTriggered;
	private boolean isCommandExecuted;
	
	public GraphiteMessageReceivedEvent(MessageReceivedEvent jdaEvent) {
		this.jdaEvent = jdaEvent;
	}
	
	public GraphiteMessageReceivedEvent(MessageReceivedEvent jdaEvent, Command commandTriggered, boolean isCommandExecuted) {
		this.jdaEvent = jdaEvent;
		this.commandTriggered = commandTriggered;
		this.isCommandExecuted = isCommandExecuted;
	}
	
	public GraphiteMessageReceivedEvent(MessageReceivedEvent jdaEvent, GraphiteCustomCommand customCommandTriggered, boolean isCommandExecuted) {
		this.jdaEvent = jdaEvent;
		this.customCommandTriggered = customCommandTriggered;
		this.isCommandExecuted = isCommandExecuted;
	}
	
	public MessageReceivedEvent getJDAEvent() {
		return jdaEvent;
	}
	
	public Command getCommandTriggered() {
		return commandTriggered;
	}
	
	public GraphiteCustomCommand getCustomCommandTriggered() {
		return customCommandTriggered;
	}
	
	public boolean isCommandExecuted() {
		return isCommandExecuted;
	}
	
	public boolean wasAnyCommandTriggered() {
		return commandTriggered != null || customCommandTriggered != null;
	}
	
}
