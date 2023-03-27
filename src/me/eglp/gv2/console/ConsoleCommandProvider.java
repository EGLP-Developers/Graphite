package me.eglp.gv2.console;

import java.util.ArrayList;
import java.util.List;

import me.eglp.gv2.console.command.CommandBroadcast;
import me.eglp.gv2.console.command.CommandCache;
import me.eglp.gv2.console.command.CommandDebug;
import me.eglp.gv2.console.command.CommandDelTemplate;
import me.eglp.gv2.console.command.CommandGenKey;
import me.eglp.gv2.console.command.CommandGuilds;
import me.eglp.gv2.console.command.CommandHelp;
import me.eglp.gv2.console.command.CommandListKeys;
import me.eglp.gv2.console.command.CommandMembers;
import me.eglp.gv2.console.command.CommandMoney;
import me.eglp.gv2.console.command.CommandShutdown;
import me.eglp.gv2.console.command.CommandStats;
import me.eglp.gv2.console.command.CommandTask;
import me.eglp.gv2.console.command.CommandTwitch;
import me.eglp.gv2.console.command.CommandWIKick;
import me.eglp.gv2.console.command.CommandWIList;
import me.eglp.gv2.console.command.CommandWhoIs;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.parser.CommandParser;
import me.mrletsplay.mrcore.command.parser.CommandParsingException;
import me.mrletsplay.mrcore.command.parser.ParsedCommand;
import me.mrletsplay.mrcore.command.provider.CommandProvider;

public class ConsoleCommandProvider implements CommandProvider {
	
	public static final ConsoleCommandProvider INSTANCE = new ConsoleCommandProvider();
	
	private List<AbstractConsoleCommand> commands;
	private CommandParser parser;
	
	public ConsoleCommandProvider() {
		this.commands = new ArrayList<>();
		this.parser = new CommandParser(this);
		
		commands.add(new CommandDelTemplate());
		commands.add(new CommandGenKey());
		commands.add(new CommandGuilds());
		commands.add(new CommandHelp());
		commands.add(new CommandListKeys());
		commands.add(new CommandMembers());
		commands.add(new CommandMoney());
		commands.add(new CommandShutdown());
		commands.add(new CommandTask());
		commands.add(new CommandWhoIs());
		commands.add(new CommandStats());
		commands.add(new CommandTwitch());
		commands.add(new CommandDebug());
		commands.add(new CommandWIKick());
		commands.add(new CommandWIList());
		commands.add(new CommandBroadcast());
		commands.add(new CommandCache());
	}
	
	@Override
	public List<AbstractConsoleCommand> getCommands() {
		return commands;
	}
	
	@Override
	public CommandParser getCommandParser() {
		return parser;
	}
	
	public static void onCommand(String raw) {
		try {
			ParsedCommand p = INSTANCE.getCommandParser().parseCommand(ConsoleCommandSender.INSTANCE, raw);
			p.getCommand().action(new CommandInvokedEvent(ConsoleCommandSender.INSTANCE, p));
		}catch(CommandParsingException e) {
			e.send(ConsoleCommandSender.INSTANCE);
		}
	}

}
