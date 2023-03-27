package me.eglp.gv2.util.lang;

import java.awt.Color;

import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public interface MessageIdentifier extends LocalizedString {

	public LocalizedString getMessageText();
	
	public Color getMessageColor();
	
	@Override
	public default String getMessagePath() {
		return getMessageText().getMessagePath();
	}
	
	@Override
	public default String getFallback() {
		return getMessageText().getFallback();
	}
	
	public default MessageEmbed createEmbed(GraphiteLocalizable localizable, String... params) {
		return new EmbedBuilder()
			.setColor(getMessageColor())
			.setDescription(getMessageText().getFor(localizable, params))
			.build();
	}
	
	public default MessageCreateData createMessage(GraphiteLocalizable localizable, String... params) {
		return new MessageCreateBuilder()
			.setEmbeds(
				new EmbedBuilder()
					.setColor(getMessageColor())
					.setDescription(getMessageText().getFor(localizable, params))
					.build()
			)
			.build();
	}
	
	public default void sendMessage(GraphiteMessageChannel<?> channel, String... params) {
		channel.sendMessage(createEmbed(channel.getOwner(), params));
	}
	
	public default Message sendMessageComplete(GraphiteMessageChannel<?> channel, String... params) {
		return channel.sendMessageComplete(createEmbed(channel.getOwner(), params));
	}
	
	public default void reply(CommandInvokedEvent event, String... params) {
		event.reply(createEmbed(event.getSender(), params));
	}
	
}
