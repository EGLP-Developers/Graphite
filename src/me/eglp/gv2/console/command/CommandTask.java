package me.eglp.gv2.console.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.task.GraphiteTask;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.option.impl.DefaultCommandOption;

public class CommandTask extends AbstractConsoleCommand {

	public CommandTask() {
		super("task");
		setDescription("Manage tasks");
		addOption(DefaultCommandOption.HELP);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getParsedCommand().isOptionPresent(DefaultCommandOption.HELP)) {
			sendCommandInfo(event.getSender());
			return;
		}
		String[] args = event.getParsedCommand().getArguments();
		if(args.length == 0) {
			event.getSender().sendMessage("Usage: task <list/stop/restart>");
			return;
		}
		if(args[0].equalsIgnoreCase("list")) {
			String path = "";
			if(args.length == 2) path = args[1];
			
			List<GraphiteTask> tasks = new ArrayList<>(Graphite.getScheduler().getTasks());
			
			while(!tasks.isEmpty()) {
				GraphiteTask task = tasks.remove(0);
				if(!task.getName().startsWith(path)) continue;
				String[] tPath = task.getName().split("/", 2);
				List<GraphiteTask> groupedTasks = tPath[0].equalsIgnoreCase(path) ? Collections.emptyList() : tasks.stream()
						.filter(t -> t.getName().split("/", 2)[0].equalsIgnoreCase(tPath[0]))
						.collect(Collectors.toList());
				if(groupedTasks.isEmpty()) {
					event.getSender().sendMessage("[" + (task.hasTerminated() ? "STOPPED" : "RUNNING") + "] " + task.getName());
				}else {
					tasks.removeAll(groupedTasks);
					groupedTasks.add(task);
					int stoppedTasks = (int) groupedTasks.stream().filter(GraphiteTask::hasTerminated).count();
					int runningTasks = groupedTasks.size() - stoppedTasks;
					event.getSender().sendMessage("[" + runningTasks + " RUNNING, " + stoppedTasks + " STOPPED] " + tPath[0]);
					event.getSender().sendMessage("  + " + groupedTasks.size() + " tasks");
				}
			}
		}else if(args[0].equals("stop")) {
			if(args.length != 2) {
				event.getSender().sendMessage("Usage: task stop <task name/all>");
				return;
			}
			if(args[1].equalsIgnoreCase("all")) {
				for(GraphiteTask t : Graphite.getScheduler().getTasks()) {
					if(t.hasTerminated()) continue;
					t.stop(false);
				}
				event.getSender().sendMessage("All tasks stopped");
			}else {
				GraphiteTask task = Graphite.getScheduler().getTaskByName(args[1]);
				if(task == null) {
					event.getSender().sendMessage("Task not found");
					return;
				}
				if(task.hasTerminated()) {
					event.getSender().sendMessage("Task not running");
					return;
				}
				task.stop(false);
				event.getSender().sendMessage("Task stopped");
			}
		}else if(args[0].equals("restart")) {
			if(args.length != 2) {
				event.getSender().sendMessage("Usage: task restart <task name/all>");
				return;
			}
			if(args[1].equalsIgnoreCase("all")) {
				for(GraphiteTask t : Graphite.getScheduler().getTasks()) {
					t.restart();
				}
				event.getSender().sendMessage("All tasks restarted");
			}else {
				GraphiteTask task = Graphite.getScheduler().getTaskByName(args[1]);
				if(task == null) {
					event.getSender().sendMessage("Task not found");
					return;
				}
				if(!task.hasTerminated()) {
					event.getSender().sendMessage("Task is already running");
					return;
				}
				task.restart();
				event.getSender().sendMessage("Task restarted");
			}
		}
	}
	
}
