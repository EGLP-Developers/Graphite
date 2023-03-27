package me.eglp.gv2.util.webinterface.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.task.GraphiteTask;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.eglp.gv2.util.webinterface.js.classes.Task;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class TaskRequestHandler {

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getTasks", requireGuild = true)
	public static WebinterfaceResponse getTasks(WebinterfaceRequestEvent event) {
		if(!event.getUser().isAdmin()) return WebinterfaceResponse.error("You are not allowed to do admin stuff");
		List<GraphiteTask> tasks = new ArrayList<>(Graphite.getScheduler().getTasks());
		JSONObject o = new JSONObject();
		o.put("tasks", new JSONArray(tasks.stream().map(t -> new Task(t).toWebinterfaceObject()).collect(Collectors.toList())));
		return WebinterfaceResponse.success(o);
	}
	
	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "dumpStoppedTasks", requireGuild = true)
	public static WebinterfaceResponse dumpStoppedTasks(WebinterfaceRequestEvent event) {
		if(!event.getUser().isAdmin()) return WebinterfaceResponse.error("You are not allowed to do admin stuff");
		Graphite.log("Stopped tasks dumped on webinterface by " + event.getUser().getDiscordUser().getName());
		List<GraphiteTask> stoppedTasks = Graphite.getScheduler().getTasks().stream().filter(t -> t.hasTerminated()).collect(Collectors.toList());
		if(stoppedTasks.isEmpty()) {
			return WebinterfaceResponse.error("All tasks are up and running well");
		}
		for(GraphiteTask t : stoppedTasks) Graphite.log("Stopped Task: " + t.getName());
		return WebinterfaceResponse.success();
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "restartAllTasks", requireGuild = true)
	public static WebinterfaceResponse restartAllTasks(WebinterfaceRequestEvent event) {
		if(!event.getUser().isAdmin()) return WebinterfaceResponse.error("You are not allowed to do admin stuff");
		Graphite.log("Tasks restarted on webinterface by " + event.getUser().getDiscordUser().getName());
		for(GraphiteTask t : Graphite.getScheduler().getTasks()) t.restart();
		return WebinterfaceResponse.success();
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "restartTask", requireGuild = true)
	public static WebinterfaceResponse restartTask(WebinterfaceRequestEvent event) {
		if(!event.getUser().isAdmin()) return WebinterfaceResponse.error("You are not allowed to do admin stuff");
		String name = event.getRequestData().getString("name");
		GraphiteTask task = Graphite.getScheduler().getTaskByName(name);
		if(task == null) {
			return WebinterfaceResponse.error("Task not found");
		}
		if(!task.hasTerminated()) {
			return WebinterfaceResponse.error("Task is already running");
		}
		Graphite.log("Task " + task.getName() + " restarted on webinterface by " + event.getUser().getDiscordUser().getName());
		task.restart();
		return WebinterfaceResponse.success();
	}

}
