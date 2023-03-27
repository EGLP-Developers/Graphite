package me.eglp.gv2.util.command.text;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteRole;
import me.eglp.gv2.util.event.AnnotationEventHandler;
import me.eglp.gv2.util.event.EventHandler;
import me.eglp.gv2.util.event.custom.impl.GraphiteMessageReceivedEvent;
import me.eglp.gv2.util.mention.MentionFinder;
import me.eglp.gv2.util.mention.MentionFinder.SearchResult;
import me.eglp.gv2.util.mention.MentionType;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandListener implements AnnotationEventHandler {

	@EventHandler
	public void onMessage(MessageReceivedEvent event) {
		if(event.getAuthor().isBot() || event.isWebhookMessage()) return;
		String prefix;
		if(event.isFromGuild()) {
			GraphiteGuild g = Graphite.getGuild(event.getGuild());
			if(!g.getConfig().hasTextCommands()) return;
			if(event.getMessage().getContentRaw().startsWith(g.getConfig().getPrefix())) {
				prefix = g.getConfig().getPrefix();
			}else {
				SearchResult sr = MentionFinder.findFirstMention(g, event.getMessage().getContentRaw());
				boolean mentionValid = sr != null && sr.getIndex() == 0 && sr.getMention().isValid();
				if(sr == null || !mentionValid) return; // Redundant null check
				boolean selfUserMentioned = mentionValid && sr.getMention().isOfType(MentionType.USER)
						&& Graphite.getUser(event.getJDA().getSelfUser()).equals(sr.getMention().asUserMention().getMentionedUser());
				
				GraphiteRole selfRole = g.getSelfRole();
				boolean selfRoleMentioned = mentionValid && selfRole != null && sr.getMention().isOfType(MentionType.ROLE)
						&& selfRole.equals(sr.getMention().asRoleMention().getMentionedRole());
				
				if(selfUserMentioned || selfRoleMentioned) {
					prefix = sr.getFullResult();
				}else {
					Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event));
					return;
				}
			}
		}else if(event.isFromType(ChannelType.PRIVATE)) {
			if(event.getMessage().getContentRaw().startsWith(Graphite.getMainBotInfo().getDefaultPrefix())) {
				prefix = Graphite.getMainBotInfo().getDefaultPrefix();
			}else {
				SearchResult sr = MentionFinder.findFirstMention(null, event.getMessage().getContentRaw());
				boolean selfUserMentioned = sr != null
						&& sr.getIndex() == 0
						&& sr.getMention().isValid()
						&& sr.getMention().isOfType(MentionType.USER)
						&& Graphite.getUser(event.getJDA().getSelfUser()).equals(sr.getMention().asUserMention().getMentionedUser());
				if(selfUserMentioned) {
					prefix = event.getJDA().getSelfUser().getAsMention();
				}else {
					prefix = "";
				}
			}
		}else return;
		
		String cmdLine = event.getMessage().getContentRaw().substring(prefix.length()).trim();
		CommandHandler.handleCommand(event, prefix, cmdLine);
	}

}
