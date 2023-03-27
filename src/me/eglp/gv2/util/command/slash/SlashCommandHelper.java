package me.eglp.gv2.util.command.slash;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.util.base.GraphiteMessageChannel;
import me.eglp.gv2.util.base.guild.GraphiteGuildChannel;
import me.eglp.gv2.util.command.CommandSender;
import me.eglp.gv2.util.command.text.argument.CommandArgument;
import me.eglp.gv2.util.command.text.argument.MentionArgument;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.mention.GraphiteMention;
import me.eglp.gv2.util.mention.MentionType;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class SlashCommandHelper {
	
	public static boolean gatherOptions(CommandSender sender, CommandArgument[] args, GraphiteMessageChannel<?> channel, List<Attachment> attachments, Runnable sendHelp, Map<String, Object> options, List<OptionData> optionsData) {
		int argIdx = 0;
		for(int i = 0; i < optionsData.size(); i++) {
			boolean isLast = i == optionsData.size() - 1;
			OptionData d = optionsData.get(i);
			if((argIdx >= args.length && d.getType() != OptionType.ATTACHMENT) || (attachments.isEmpty() && d.getType() == OptionType.ATTACHMENT)) {
				if(d.isRequired()) {
					sendHelp.run();
					return false;
				}
				break;
			}
			
			CommandArgument arg = args[argIdx];
			switch(d.getType()) {
				case BOOLEAN:
					if(!arg.isBoolean(sender)) {
						DefaultMessage.ERROR_ARGUMENT_TYPE_BOOLEAN.sendMessage(channel, "parameter", d.getName());
						return false;
					}
					
					options.put(d.getName(), arg.asBoolean(sender));
					break;
				case CHANNEL:
				{
					MentionArgument m = arg.asMention();
					if(m == null || !m.isValid()) {
						DefaultMessage.ERROR_INVALID_MENTION.sendMessage(channel);
						return false;
					}
					
					if(m.getMention().isAmbiguous()) {
						DefaultMessage.ERROR_AMBIGUOUS_MENTION.sendMessage(channel);
						return false;
					}
					
					GraphiteMention mention = m.getMention();
					GraphiteGuildChannel ch;
					MentionType t = mention.getType();
					switch(t) {
						case TEXT_CHANNEL:
							ch = mention.asTextChannelMention().getMentionedTextChannel();
							break;
						case VOICE_CHANNEL:
							ch = mention.asVoiceChannelMention().getMentionedVoiceChannel();
							break;
						case NEWS_CHANNEL:
							ch = mention.asNewsChannelMention().getMentionedNewsChannel();
							break;
						case STAGE_CHANNEL:
							ch = mention.asStageChannelMention().getMentionedStageChannel();
							break;
						case CATEGORY:
							ch = mention.asCategoryMention().getMentionedCategory();
							break;
						default:
							ch = null;
							break;
					}
					
					if(ch == null || !d.getChannelTypes().contains(ch.getJDAChannel().getType())) {
						DefaultMessage.ERROR_ALLOWED_MENTION_TYPE_MESSAGE.sendMessage(channel,
							"mention_types",
							DefaultMessage.getMentionTypesString(sender, d.getChannelTypes().stream().map(MentionType::getMentionType).toArray(MentionType[]::new)));
						return false;
					}
					
					options.put(d.getName(), ch);
					break;
				}
				case INTEGER:
				{
					if(!arg.isLong()) {
						DefaultMessage.ERROR_ARGUMENT_TYPE_INTEGER.sendMessage(channel, "parameter", d.getName());
						return false;
					}
					
					long v = arg.asLong();
					if(!d.getChoices().isEmpty() && !d.getChoices().stream().anyMatch(c -> c.getAsLong() == v)) {
						DefaultMessage.ERROR_CHOICE_INVALID.sendMessage(channel,
								"parameter", d.getName(),
								"choices", d.getChoices().stream().map(vl -> String.valueOf(vl.getAsString())).collect(Collectors.joining(", ")));
						return false;
					}
					
					options.put(d.getName(), v);
					break;
				}
				case NUMBER:
				{
					if(!arg.isDouble()) {
						DefaultMessage.ERROR_ARGUMENT_TYPE_NUMBER.sendMessage(channel, "parameter", d.getName());
						return false;
					}
					
					double v = arg.asDouble();
					if(!d.getChoices().isEmpty() && !d.getChoices().stream().anyMatch(c -> c.getAsDouble() == v)) {
						DefaultMessage.ERROR_CHOICE_INVALID.sendMessage(channel,
								"parameter", d.getName(),
								"choices", d.getChoices().stream().map(vl -> String.valueOf(vl.getAsString())).collect(Collectors.joining(", ")));
						return false;
					}
					
					options.put(d.getName(), v);
					break;
				}
				case MENTIONABLE:
				{
					MentionArgument m = arg.asMention();
					if(m == null || !m.isValid()) {
						DefaultMessage.ERROR_INVALID_MENTION.sendMessage(channel);
						return false;
					}
					
					if(m.getMention().isAmbiguous()) {
						DefaultMessage.ERROR_AMBIGUOUS_MENTION.sendMessage(channel);
						return false;
					}
					
					GraphiteMention mention = m.getMention();
					MentionType t = mention.getType();
					switch(t) {
						case USER:
							options.put(d.getName(), mention.asUserMention().getMentionedUser());
							break;
						case ROLE:
							options.put(d.getName(), mention.asRoleMention().getMentionedRole());
							break;
						case EVERYONE:
							options.put(d.getName(), sender.asGuild().getPublicRole());
							break;
						default:
							DefaultMessage.ERROR_ALLOWED_MENTION_TYPE_MESSAGE.sendMessage(channel);
							return false;
					}
					break;
				}
				case ROLE:
				{
					MentionArgument m = arg.asMention();
					if(m == null || !m.isValid()) {
						DefaultMessage.ERROR_INVALID_MENTION.sendMessage(channel);
						return false;
					}
					
					GraphiteMention mention = m.getMention();
					MentionType t = mention.getType();
					switch(t) {
						case ROLE:
							options.put(d.getName(), mention.asRoleMention().getMentionedRole());
							break;
						case EVERYONE:
							options.put(d.getName(), sender.asGuild().getPublicRole());
							break;
						default:
							DefaultMessage.ERROR_ALLOWED_MENTION_TYPE_MESSAGE.sendMessage(channel,
								"mention_types",
								DefaultMessage.getMentionTypesString(sender, MentionType.ROLE));
							return false;
					}
					break;
				}
				case STRING:
				{
					String val = isLast ? Arrays.stream(args).skip(argIdx).map(a -> a.getRaw()).collect(Collectors.joining(" ")) : arg.getRaw();
					if(!d.getChoices().isEmpty()) {
						final String oldVal = val;
						val = d.getChoices().stream()
								.filter(c -> c.getAsString().equalsIgnoreCase(oldVal))
								.map(c -> c.getAsString())
								.findFirst().orElse(null);
						if(val == null) {
							DefaultMessage.ERROR_CHOICE_INVALID.sendMessage(channel,
									"parameter", d.getName(),
									"choices", d.getChoices().stream().map(vl -> String.valueOf(vl.getAsString())).collect(Collectors.joining(", ")));
							return false;
						}
					}
					
					options.put(d.getName(), val);
					if(isLast) argIdx = args.length;
					break;
				}
				case USER:
				{
					MentionArgument m = arg.asMention();
					if(m == null || !m.isValid()) {
						DefaultMessage.ERROR_INVALID_MENTION.sendMessage(channel);
						return false;
					}
					
					if(m.getMention().isAmbiguous()) {
						DefaultMessage.ERROR_AMBIGUOUS_MENTION.sendMessage(channel);
						return false;
					}
					
					GraphiteMention mention = m.getMention();
					if(mention.getType() != MentionType.USER) {
						DefaultMessage.ERROR_ALLOWED_MENTION_TYPE_MESSAGE.sendMessage(channel,
							"mention_types",
							DefaultMessage.getMentionTypesString(sender, MentionType.USER));
						return false;
					}
					options.put(d.getName(), mention.asUserMention().getMentionedUser());
					break;
				}
				case ATTACHMENT:
				{
					argIdx--; // Don't consume an argument
					options.put(d.getName(), attachments.remove(0));
					break;
				}
				default:
					throw new UnsupportedOperationException("Invalid option type");
			}
			
			argIdx++;
		}
		
		if(argIdx < args.length) {
			sendHelp.run();
			return false;
		}
		
		return true;
	}

}
