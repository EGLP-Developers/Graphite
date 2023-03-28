package me.eglp.gv2.util.base;

import java.util.function.Consumer;

import me.eglp.gv2.util.command.CommandSender;
import me.eglp.gv2.util.lang.LocalizedMessage;
import me.eglp.gv2.util.lang.LocalizedString;
import me.eglp.gv2.util.lang.MessageIdentifier;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public interface GraphiteMessageChannel<T extends CommandSender> {

	public T getOwner();

	public MessageChannel getJDAChannel();

	public default void sendMessage(String message, Consumer<? super Throwable> onFailure, String... params) {
		if (params.length % 2 != 0) {
			throw new IllegalArgumentException("Invalid params");
		}
		for (int i = 0; i < params.length; i += 2) {
			message = message.replace("{" + params[i] + "}", params[i + 1]);
		}
		getJDAChannel().sendMessage(message).queue(null, onFailure);
	}

	public default void sendMessage(String message, String... params) {
		sendMessage(message, null, params);
	}

	public default Message sendMessageComplete(String message, String... params) {
		if (params.length % 2 != 0) {
			throw new IllegalArgumentException("Invalid params");
		}
		for (int i = 0; i < params.length; i += 2) {
			message = message.replace("{" + params[i] + "}", params[i + 1]);
		}
		return getJDAChannel().sendMessage(message).complete();
	}

	public default void sendMessage(MessageEmbed message, Consumer<? super Throwable> onFailure) {
		getJDAChannel().sendMessageEmbeds(message).queue(null, onFailure);
	}

	public default void sendMessage(MessageEmbed message) {
		sendMessage(message, null);
	}

	public default Message sendMessageComplete(MessageEmbed message) {
		return getJDAChannel().sendMessageEmbeds(message).complete();
	}

	public default void sendMessage(MessageCreateData message, Consumer<? super Throwable> onFailure) {
		getJDAChannel().sendMessage(message).queue(null, onFailure);
	}

	public default void sendMessage(MessageCreateData message) {
		sendMessage(message, null);
	}

	public default void sendFiles(FileUpload... files) {
		getJDAChannel().sendFiles(files).queue();
	}

	public default Message sendMessageComplete(MessageCreateData message) {
		return getJDAChannel().sendMessage(message).complete();
	}

	public default void sendMessage(LocalizedString message, Consumer<? super Throwable> onFailure, String... params) {
		sendMessage(message.getFor(getOwner(), params), onFailure);
	}

	public default void sendMessage(LocalizedString message, String... params) {
		sendMessage(message.getFor(getOwner(), params));
	}

	public default Message sendMessageComplete(LocalizedString message, String... params) {
		return sendMessageComplete(message.getFor(getOwner(), params));
	}

	public default void sendMessage(LocalizedMessage message, Consumer<? super Throwable> onFailure, String... params) {
		sendMessage(message.getFor(getOwner(), params), onFailure);
	}

	public default void sendMessage(LocalizedMessage message, String... params) {
		sendMessage(message.getFor(getOwner(), params));
	}

	public default Message sendMessageComplete(LocalizedMessage message, String... params) {
		return sendMessageComplete(message.getFor(getOwner(), params));
	}

	public default void sendMessage(MessageIdentifier message, String... params) {
		message.sendMessage(this, params);
	}

	public default Message sendMessageComplete(MessageIdentifier message, String... params) {
		return message.sendMessageComplete(this, params);
	}

	public default void sendFilesComplete(FileUpload... files) {
		getJDAChannel().sendFiles(files).complete();
	}

	public default String getName() {
		return getJDAChannel().getName();
	}

}