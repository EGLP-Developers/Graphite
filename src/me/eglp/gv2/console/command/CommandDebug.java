package me.eglp.gv2.console.command;

import java.util.Arrays;
import java.util.stream.Collectors;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteOption;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;

public class CommandDebug extends AbstractConsoleCommand {

	public CommandDebug() {
		super("debug");
		
		AbstractConsoleCommand dump = addSubCommand(new AbstractConsoleCommand("dump") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				Thread[] threads = new Thread[Thread.activeCount()];
				Thread.enumerate(threads);
				
				Graphite.log("---THREAD DUMP START---");
				for(Thread t : threads) {
					Graphite.log("Thread name: " + t.getName());
					Graphite.log("Thread state: " + t.getState());
					
					Graphite.log("Stack trace:");
					for(StackTraceElement e : t.getStackTrace()) {
						Graphite.log("\t" + e.toString());
					}
				}
				Graphite.log("---THREAD DUMP END---");
			}
		});
		dump.setDescription("Dumps all currently running threads");
		dump.setUsage("debug dump");
		
		AbstractConsoleCommand enableoption = addSubCommand(new AbstractConsoleCommand("enableoption") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(event.getArguments().length != 1) {
					sendCommandInfo(event.getSender());
					return;
				}
				
				String opt = event.getArguments()[0];
				GraphiteOption op;
				try {
					op = GraphiteOption.valueOf(opt.toUpperCase());
				}catch(IllegalArgumentException e) {
					System.out.println("Available options: " + Arrays.stream(GraphiteOption.values()).map(o -> o.name()).collect(Collectors.joining(", ")));
					return;
				}
				
				if(!Graphite.getOptions().contains(op)) Graphite.getOptions().add(op);
				System.out.println("Option enabled");
			}
		});
		enableoption.addAlias("eop");
		enableoption.setDescription("Enables a debug option");
		enableoption.setUsage("debug enableoption <option>");
		
		AbstractConsoleCommand disableoption = addSubCommand(new AbstractConsoleCommand("disableoption") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(event.getArguments().length != 1) {
					sendCommandInfo(event.getSender());
					return;
				}
				
				String opt = event.getArguments()[0];
				GraphiteOption op;
				try {
					op = GraphiteOption.valueOf(opt.toUpperCase());
				}catch(IllegalArgumentException e) {
					System.out.println("Available options: " + Arrays.stream(GraphiteOption.values()).map(o -> o.name()).collect(Collectors.joining(", ")));
					return;
				}
				
				Graphite.getOptions().remove(op);
				System.out.println("Option disabled");
			}
		});
		disableoption.addAlias("dop");
		disableoption.setDescription("Disables a debug option");
		disableoption.setUsage("debug disableoption <option>");
		
		AbstractConsoleCommand options = addSubCommand(new AbstractConsoleCommand("options") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				System.out.println("Currently enabled options: " + Graphite.getOptions().stream().map(o -> o.name()).collect(Collectors.joining(", ")));
			}
		});
		options.addAlias("ops");
		options.setDescription("Lists all enabled debug options");
		options.setUsage("debug options");
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		sendCommandInfo(event.getSender());
	}
	
}
