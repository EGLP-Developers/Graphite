package me.eglp.gv2.multiplex;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import me.eglp.gv2.multiplex.bot.MultiplexBot;

public class MultiplexExecutor extends ScheduledThreadPoolExecutor {

	private MultiplexBot bot;
	private int i;
	
	public MultiplexExecutor(MultiplexBot bot) {
		super(15);
		this.bot = bot;
		setThreadFactory(this::createThread);
	}
	
	private Thread createThread(Runnable run) {
		return new Thread(() -> {
			GraphiteMultiplex.setCurrentBot(bot);
			run.run();
		}, "Multiplex-" + bot.getBotInfo().getName() + "-" + (i++));
	}
	
}
