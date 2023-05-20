package me.eglp.gv2.util.event;

import me.eglp.gv2.main.Graphite;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class GraphiteJDAListener extends GraphiteListener implements EventListener {

	@Override
	public void onEvent(GenericEvent event) {
		if(!Graphite.isOnline()) return;

		if(event.getJDA().getStatus().equals(Status.CONNECTED) && event instanceof GenericGuildEvent && !(event instanceof GuildJoinEvent)) {
			String id = ((GenericGuildEvent) event).getGuild().getId();
			if(!shouldHandleGuildEvents(id)) return;
		}

		Graphite.getScheduler().execute(() -> fire(event));
	}

	private boolean shouldHandleGuildEvents(String id) {
		return Graphite.isOnGuild(id);
	}

}
