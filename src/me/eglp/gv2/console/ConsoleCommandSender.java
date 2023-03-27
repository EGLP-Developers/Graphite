package me.eglp.gv2.console;

import me.mrletsplay.mrcore.command.CommandSender;

public class ConsoleCommandSender implements CommandSender {

	public static final ConsoleCommandSender INSTANCE = new ConsoleCommandSender();
	
	@Override
	public void sendMessage(String message) {
		System.out.println(message);
	}
	
}
