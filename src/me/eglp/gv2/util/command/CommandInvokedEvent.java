package me.eglp.gv2.util.command;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.GraphiteTextChannel;
import me.eglp.gv2.guild.customcommand.GraphiteCustomCommand;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphitePrivateChannel;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import me.eglp.gv2.util.command.slash.DeferredReply;
import me.eglp.gv2.util.emote.JDAEmote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class CommandInvokedEvent {

	private Command command;
	private GraphiteCustomCommand customCommand;
	private MessageReceivedEvent jdaMessageEvent;
	private SlashCommandInteractionEvent jdaSlashCommandEvent;
	private GraphiteUser author;
	private CommandSender sender;
	private GraphiteMessageChannel<?> channel;
	private Map<String, Object> options;
	private String prefixUsed;

	private CommandInvokedEvent(Command command, GraphiteCustomCommand customCommand, SlashCommandInteractionEvent jdaSlashCommandEvent, GraphiteUser author, CommandSender sender, GraphiteMessageChannel<?> channel) {
		this.command = command;
		this.customCommand = customCommand;
		this.jdaSlashCommandEvent = jdaSlashCommandEvent;
		this.author = author;
		this.sender = sender;
		this.channel = channel;
		this.prefixUsed = "/";

		this.options = new HashMap<>();
		jdaSlashCommandEvent.getOptions().forEach(o -> {
			Object v = null;
			switch(o.getType()) {
				case BOOLEAN:
					v = o.getAsBoolean();
					break;
				case CHANNEL:
					v = Graphite.getGuildChannel(o.getAsChannel());
					break;
				case INTEGER:
					v = o.getAsLong();
					break;
				case MENTIONABLE:
					v = o.getAsMentionable();
					if(v instanceof User) {
						v = Graphite.getUser((User) v);
					}else if(v instanceof Member) {
						v = Graphite.getMember((Member) v);
					}else if(v instanceof Role) {
						v = Graphite.getRole((Role) v);
					}
					break;
				case ROLE:
					v = Graphite.getRole(o.getAsRole());
					break;
				case STRING:
					v = o.getAsString();
					break;
				case USER:
					Member m = o.getAsMember();
					if(m != null) {
						v = Graphite.getMember(m);
					}else {
						v = Graphite.getUser(o.getAsUser());
					}
					break;
				case NUMBER:
					v = o.getAsDouble();
					break;
				case ATTACHMENT:
					v = o.getAsAttachment();
					break;
				default:
					throw new UnsupportedOperationException("Unknown/Unsupported type");
			}
			options.put(o.getName(), v);
		});
	}

	private CommandInvokedEvent(Command command, GraphiteCustomCommand customCommand, MessageReceivedEvent jdaMessageEvent, GraphiteUser author, CommandSender sender, GraphiteMessageChannel<?> channel, String prefixUsed, Map<String, Object> options) {
		this.command = command;
		this.customCommand = customCommand;
		this.jdaMessageEvent = jdaMessageEvent;
		this.author = author;
		this.sender = sender;
		this.channel = channel;
		this.options = options;
		this.prefixUsed = prefixUsed;
	}

	private CommandInvokedEvent(CommandInvokedEvent event, Command command, Map<String, Object> options) {
		this.command = command;
		this.jdaMessageEvent = event.jdaMessageEvent;
		this.jdaSlashCommandEvent = event.jdaSlashCommandEvent;
		this.author = event.author;
		this.sender = event.sender;
		this.channel = event.channel;
		this.options = options;
		this.prefixUsed = event.prefixUsed;
	}

	public Command getCommand() {
		return command;
	}

	public GraphiteCustomCommand getCustomCommand() {
		return customCommand;
	}

	public Map<String, Object> getOptions() {
		return options;
	}

	public Object getOption(String name) {
		return options.get(name);
	}

	public boolean hasOption(String name) {
		return options.containsKey(name);
	}

	public GraphiteMessageChannel<?> getChannel() {
		return channel;
	}

	public GraphiteGuildMessageChannel getGuildChannel() {
		if(!(channel instanceof GraphiteGuildMessageChannel)) return null;
		return (GraphiteGuildMessageChannel) channel;
	}

	public GraphiteTextChannel getTextChannel() {
		if(!(channel instanceof GraphiteTextChannel)) return null;
		return (GraphiteTextChannel) channel;
	}

	public GraphitePrivateChannel getPrivateChannel() {
		if(!(channel instanceof GraphitePrivateChannel)) return null;
		return (GraphitePrivateChannel) channel;
	}

	public boolean isFromGuild() {
		return getChannelType().isGuild();
	}

	public boolean isFromUser() {
		return getChannelType() == ChannelType.PRIVATE;
	}

	public ChannelType getChannelType() {
		return jdaSlashCommandEvent != null ? jdaSlashCommandEvent.getChannelType() : jdaMessageEvent.getChannelType();
	}

	public GraphiteGuild getGuild() {
		return sender.asGuild();
	}

	public GraphiteUser getAuthor() {
		return author;
	}

	public CommandSender getSender() {
		return sender;
	}

	public boolean isUsingSlashCommand() {
		return jdaSlashCommandEvent != null;
	}

	public String getPrefixUsed() {
		return prefixUsed;
	}

	/**
	 * Either opens a private channel or returns the channel the command was sent from
	 * @return A channel to send messages to
	 */
	public GraphiteMessageChannel<?> getAuthorChannel() {
		GraphiteMessageChannel<?> ch = getAuthor().openPrivateChannel();
		if(ch == null) return getChannel();
		return ch;
	}

	public GraphiteMember getMember() {
		return Graphite.getMember(jdaSlashCommandEvent != null ? jdaSlashCommandEvent.getMember() : jdaMessageEvent.getMember());
	}

	public void sendCommandHelp() {
		Thread.dumpStack();
		if(command != null) {
			command.sendCommandHelp(channel);
		}else {
			customCommand.sendCommandHelp(channel);
		}
	}

	public void reply(String message) {
		reply(new MessageCreateBuilder().setContent(message).build());
	}

	public void reply(MessageEmbed embed) {
		reply(new MessageCreateBuilder().setEmbeds(embed).build());
	}

	public void reply(MessageCreateData message) {
		reply(message, null);
	}

	public void reply(MessageCreateData message, Consumer<Object> onSuccess) {
		if(jdaSlashCommandEvent != null) {
			if(jdaSlashCommandEvent.isAcknowledged()) {
				jdaSlashCommandEvent.getChannel().sendMessage(message).queue(onSuccess, e -> {});
				return;
			}

			jdaSlashCommandEvent.reply(message).queue(onSuccess);
		}else {
			jdaMessageEvent.getMessage().reply(message).mentionRepliedUser(false).queue(onSuccess);
		}
	}

	public void replyEphemeral(String message) {
		replyEphemeral(new MessageCreateBuilder().setContent(message).build());
	}

	public void replyEphemeral(MessageEmbed embed) {
		replyEphemeral(new MessageCreateBuilder().setEmbeds(embed).build());
	}

	public void replyEphemeral(MessageCreateData message) {
		replyEphemeral(message, null);
	}

	public void replyEphemeral(MessageCreateData message, Consumer<Object> onSuccess) {
		if(jdaSlashCommandEvent != null) {
			if(jdaSlashCommandEvent.isAcknowledged()) {
				jdaSlashCommandEvent.getChannel().sendMessage(message).queue(onSuccess);
				return;
			}

			jdaSlashCommandEvent.reply(message).setEphemeral(true).queue(onSuccess);
		}else {
			jdaMessageEvent.getMessage().reply(message).mentionRepliedUser(false).queue(onSuccess);
		}
	}

	public DeferredReply deferReply() {
		if(isUsingSlashCommand()) {
			return new DeferredReply(jdaSlashCommandEvent.deferReply().complete());
		}else {
			return deferReply("Thinking...");
		}
	}

	public DeferredReply deferReply(String message) {
		return deferReply(new MessageCreateBuilder().setContent(message).build());
	}

	public DeferredReply deferReply(MessageEmbed embed) {
		return deferReply(new MessageCreateBuilder().setEmbeds(embed).build());
	}

	public DeferredReply deferReply(MessageCreateData message) {
		if(isUsingSlashCommand()) {
			if(jdaSlashCommandEvent.isAcknowledged()) {
				return new DeferredReply(jdaSlashCommandEvent.getChannel().sendMessage(message).complete());
			}

			return new DeferredReply(jdaSlashCommandEvent.reply(message).complete());
		}else {
			return new DeferredReply(jdaMessageEvent.getMessage().reply(message).mentionRepliedUser(false).complete());
		}
	}

	public void react(JDAEmote emote) {
		if(isUsingSlashCommand()) {
			if(jdaSlashCommandEvent.isAcknowledged()) {
				jdaSlashCommandEvent.getChannel().sendMessage(emote.getEncoded()).queue();
				return;
			}

			reply(emote.getUnicode());
		}else {
			jdaMessageEvent.getMessage().addReaction(emote.getEmoji()).queue();
		}
	}

	public void deleteMessage(JDAEmote emoteIfSlashCommand) {
		if(isUsingSlashCommand()) {
			if(jdaSlashCommandEvent.isAcknowledged()) {
				jdaSlashCommandEvent.getChannel().sendMessage(emoteIfSlashCommand.getEncoded()).queue();
				return;
			}

			replyEphemeral(emoteIfSlashCommand.getUnicode());
		}else {
			jdaMessageEvent.getMessage().delete().queue();
		}
	}

	public SlashCommandInteractionEvent getJDASlashCommandEvent() {
		return jdaSlashCommandEvent;
	}

	public static CommandInvokedEvent ofMessageEvent(Command command, MessageReceivedEvent jdaMessageEvent, GraphiteUser author, CommandSender sender, GraphiteMessageChannel<?> channel, String prefixUsed, Map<String, Object> options) {
		return new CommandInvokedEvent(command, null, jdaMessageEvent, author, sender, channel, prefixUsed, options);
	}

	public static CommandInvokedEvent ofSlashEvent(Command command, SlashCommandInteractionEvent jdaSlashCommandEvent, GraphiteUser author, CommandSender sender, GraphiteMessageChannel<?> channel) {
		return new CommandInvokedEvent(command, null, jdaSlashCommandEvent, author, sender, channel);
	}

	public static CommandInvokedEvent ofCustomMessageEvent(GraphiteCustomCommand command, MessageReceivedEvent jdaMessageEvent, GraphiteUser author, CommandSender sender, GraphiteMessageChannel<?> channel, String prefixUsed, Map<String, Object> options) {
		return new CommandInvokedEvent(null, command, jdaMessageEvent, author, sender, channel, prefixUsed, options);
	}

	public static CommandInvokedEvent ofCustomSlashEvent(GraphiteCustomCommand command, SlashCommandInteractionEvent jdaSlashCommandEvent, GraphiteUser author, CommandSender sender, GraphiteMessageChannel<?> channel) {
		return new CommandInvokedEvent(null, command, jdaSlashCommandEvent, author, sender, channel);
	}

	public static CommandInvokedEvent copyOfEvent(CommandInvokedEvent event, Command command, Map<String, Object> options) {
		return new CommandInvokedEvent(event, command, options);
	}

}
