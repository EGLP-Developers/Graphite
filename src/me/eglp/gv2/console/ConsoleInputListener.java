package me.eglp.gv2.console;

import java.util.Scanner;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.multiplex.bot.GlobalBot;

public class ConsoleInputListener {

	private static Thread cThread;
	
	public static void init() {
		if(cThread != null) return;
		cThread = new Thread(() -> {
			GraphiteMultiplex.setCurrentBot(GlobalBot.INSTANCE);
			try (Scanner s = new Scanner(System.in)){
				while(!Thread.interrupted()) {
					try {
						String cmd = s.nextLine();
						Graphite.getLogger().log("> " + cmd);
						ConsoleCommandProvider.onCommand(cmd);
					}catch(Exception ignored) {}
				}
			}
		});
		cThread.setName("Console-Listener-Thread");
		cThread.start();
		Graphite.log("Started console listener thread");
	}
	
}
