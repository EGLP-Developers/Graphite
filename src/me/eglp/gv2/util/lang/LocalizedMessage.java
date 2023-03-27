package me.eglp.gv2.util.lang;

import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.base.GraphiteLocalizedObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class LocalizedMessage implements GraphiteLocalizedObject<MessageEmbed> {

	private MessageIdentifier message;
	
	public LocalizedMessage(MessageIdentifier message) {
		this.message = message;
	}
	
	@Override
	public MessageEmbed getFor(GraphiteLocalizable guild, String... params) {
		return new EmbedBuilder()
				.setDescription(message.getMessageText().getFor(guild, params))
				.setColor(message.getMessageColor())
				.build();
	}
	
	public static String formatMessage(String message, String... params) {
		if(params.length % 2 != 0){
			throw new IllegalArgumentException("Invalid number of params");
		}
        for(int i = 0; i<params.length; i += 2){
        	message = message.replace("{" + params[i] + "}", params[i+1]);
		}
        return message;
	}

}
