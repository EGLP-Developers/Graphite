package me.eglp.gv2.util.command.slash;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class DeferredReply {
	
	private InteractionHook interactionHook;
	private Message message;
	
	public DeferredReply(InteractionHook interactionHook) {
		this.interactionHook = interactionHook;
	}
	
	public DeferredReply(Message message) {
		this.message = message;
	}
	
	public void editOriginal(String message) {
		editOriginal(new MessageEditBuilder().setContent(message).build());
	}
	
	public void editOriginal(MessageEmbed embed) {
		editOriginal(new MessageEditBuilder().setEmbeds(embed).build());
	}
	
	public void editOriginal(MessageEditData message) {
		if(interactionHook != null) {
			interactionHook.editOriginal(message).setReplace(true).queue();
		}else {
			this.message.editMessage(message).setReplace(true).queue();
		}
	}
	
	public Message getMessage() {
		return interactionHook != null ? interactionHook.retrieveOriginal().complete() : message;
	}

}
