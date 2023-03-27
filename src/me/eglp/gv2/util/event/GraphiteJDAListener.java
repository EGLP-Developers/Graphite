package me.eglp.gv2.util.event;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.ContextHandle;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.jdaobject.JDAObject;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class GraphiteJDAListener extends GraphiteListener implements EventListener {

	@Override
	public void onEvent(GenericEvent event) {
		MultiplexBot bot = GraphiteMultiplex.getBot(event.getJDA());
		
		if(!Graphite.isOnline() || bot == null || !bot.isOnline()) return;
		
		ContextHandle handle = GraphiteMultiplex.setCurrentBot(bot);
		
		if(event.getJDA().getStatus().equals(Status.CONNECTED) && event instanceof GenericGuildEvent && !(event instanceof GuildJoinEvent)) {
			String id = ((GenericGuildEvent) event).getGuild().getId();
			if(!shouldHandleGuildEvents(id)) return;
		}
		
		Graphite.getScheduler().execute(() -> {
			ContextHandle handleInner = GraphiteMultiplex.setCurrentBot(bot);
			fire(event);
			handleInner.reset();
		});
		
		JDAObject.clearCurrentCache();
		
		handle.reset();
	}
	
	private boolean shouldHandleGuildEvents(String id) {
		return Graphite.isOnGuild(id);
	}
	
}
