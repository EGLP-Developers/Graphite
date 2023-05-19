package me.eglp.gv2.guild.automod;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.event.AnnotationEventHandler;
import me.eglp.gv2.util.event.EventHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface AutoModMessageActionHandler<T extends AbstractAutoModSettings> extends AnnotationEventHandler {

	@EventHandler
	public default void onMessage(MessageReceivedEvent event) {
		if(event.isFromGuild() && !event.getAuthor().isBot()) {
			GraphiteGuild g = Graphite.getGuild(event.getGuild());

			T settings = getRelevantSettings(g.getAutoModSettings());

			if(settings.getAction() != AutoModAction.DISABLED
					&& g.getConfig().hasModuleEnabled(GraphiteModule.MODERATION)) {
				if(event.getMember().hasPermission(Permission.ADMINISTRATOR)
						|| settings.getChannels().stream().anyMatch(c -> g.getGuildMessageChannel(event.getGuildChannel()).getID().equals(c))
						|| event.getMember().getRoles().stream().anyMatch(r -> settings.getRoles().stream().anyMatch(rl -> g.getRole(r).getID().equals(rl)))
						|| g.getRolesConfig().getModeratorRoles().stream().anyMatch(rl -> event.getMember().getRoles().stream().anyMatch(r -> g.getRole(r).equals(rl)))) return;

				takeAction(event, settings);
			}
		}
	}

	public T getRelevantSettings(GuildAutoModSettings settings);

	public void takeAction(MessageReceivedEvent event, T settings);

}
