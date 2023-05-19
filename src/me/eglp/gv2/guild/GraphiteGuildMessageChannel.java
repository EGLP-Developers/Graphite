package me.eglp.gv2.guild;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public interface GraphiteGuildMessageChannel extends GraphiteGuildChannel, GraphiteMessageChannel<GraphiteGuild> {

	public static final int MAX_CLEARS = 10;
	public static final Set<Permission> SEND_REQUIRED_PERMISSIONS = EnumSet.of(Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_MENTION_EVERYONE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS);

	@Override
	public default String getName() {
		return GraphiteMessageChannel.super.getName();
	}

	@Override
	public default GraphiteGuild getOwner() {
		return getGuild();
	}

	@Override
	public GuildMessageChannel getJDAChannel();

	public default boolean canWrite() {
		return hasPermissions(SEND_REQUIRED_PERMISSIONS);
	}

	public default boolean canAttachFiles() {
		return hasPermissions(Permission.MESSAGE_ATTACH_FILES);
	}

	@Override
	public default void sendMessage(MessageEmbed message, Consumer<? super Throwable> onFailure) {
		if(!canWrite()) return;
		getJDAChannel().sendMessageEmbeds(message).queue(null, onFailure);
	}

	public default void clear(int amount) {
		if(amount < 1 || amount > 100) throw new IllegalArgumentException("Amount cannot be < 0 or > 100");
		List<Message> messages = getJDAChannel().getHistory().retrievePast(amount).complete();
		deleteMessages(messages, true);
	}

	public default void clear(int amount, String beforeMessage) {
		if(amount < 1 || amount > 100) throw new IllegalArgumentException("Amount cannot be < 0 or > 100");
		List<Message> messages = getJDAChannel().getHistoryBefore(beforeMessage, amount).complete().getRetrievedHistory();
		deleteMessages(messages, true);
	}

	public default void deleteMessages(List<Message> messages, boolean complete) {
		OffsetDateTime twoWeeksAgoPlusALittleBit = OffsetDateTime.now().minusWeeks(2).plusHours(1);
		List<Message> oldMsgs = messages.stream().filter(m -> m.getTimeCreated().isBefore(twoWeeksAgoPlusALittleBit)).collect(Collectors.toList());
		List<Message> newMsgs = messages.stream().filter(m -> m.getTimeCreated().isAfter(twoWeeksAgoPlusALittleBit)).collect(Collectors.toList());
		if(complete) {
			if(!newMsgs.isEmpty()) {
				if(newMsgs.size() == 1) {
					newMsgs.get(0).delete().complete();
				}else {
					getJDAChannel().deleteMessages(newMsgs).complete();
				}
			}

			if(!oldMsgs.isEmpty()) {
				if(oldMsgs.size() == 1) {
					oldMsgs.get(0).delete().complete();
				}else {
					oldMsgs.forEach(m -> getJDAChannel().deleteMessageById(m.getId()).complete());
				}
			}
		}else {
			if(!newMsgs.isEmpty()) {
				if(newMsgs.size() == 1) {
					newMsgs.get(0).delete().queue(null, e -> {
						GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Failed to delete message", e);
					});
				}else {
					getJDAChannel().deleteMessages(newMsgs).queue(null, e -> {
						GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Failed to delete message", e);
					});
				}
			}

			if(!oldMsgs.isEmpty()) {
				if(oldMsgs.size() == 1) {
					oldMsgs.get(0).delete().queue(null, e -> {
						GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Failed to delete message", e);
					});
				}else {
					oldMsgs.forEach(m -> getJDAChannel().deleteMessageById(m.getId()).queue(null, e -> {
						GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Failed to delete message", e);
					}));
				}
			}
		}
	}

	public default void clearAll(boolean complete) {
		MessageHistory h = getJDAChannel().getHistory();
		int c = 0;
		while(c++ < MAX_CLEARS) {
			List<Message> ms = h.retrievePast(100).complete();
			if(ms.isEmpty()) return;
			deleteMessages(ms, complete);
		}
	}

	public default void clearAll(boolean complete, String beforeMessage) {
		MessageHistory h = getJDAChannel().getHistoryBefore(beforeMessage, 100).complete();
		if(h.getRetrievedHistory().isEmpty()) return;
		deleteMessages(h.getRetrievedHistory(), complete);
		int c = 0;
		while(c++ < MAX_CLEARS - 1) {
			List<Message> ms = h.retrievePast(100).complete();
			if(ms.isEmpty()) return;
			deleteMessages(ms, complete);
		}
	}

}
