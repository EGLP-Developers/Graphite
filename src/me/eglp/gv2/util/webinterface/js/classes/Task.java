package me.eglp.gv2.util.webinterface.js.classes;

import me.eglp.gv2.main.task.GraphiteTask;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

public class Task implements WebinterfaceObject{
	
	@JavaScriptValue(getter = "getName")
	private String name;
	
	@JavaScriptValue(getter = "hasTerminated")
	private boolean terminated;
	
	@JavaScriptValue(getter = "hasTerminatedNormally")
	private boolean terminatedNormally;
	
	public Task(GraphiteTask t) {
		this.name = t.getName();
		this.terminated = t.hasTerminated();
		this.terminatedNormally = t.hasTerminatedNormally();
	}
	
	@JavaScriptFunction(calling = "getTasks", returning = "tasks", withGuild = true)
	public static void getTasks() {};
	
	@JavaScriptFunction(calling = "dumpStoppedTasks", withGuild = true)
	public static void dumpStoppedTasks() {};
	
	@JavaScriptFunction(calling = "restartAllTasks", withGuild = true)
	public static void restartAllTasks() {};
	
	@JavaScriptFunction(calling = "restartTask", withGuild = true)
	public static void restartTask(@JavaScriptParameter(name = "name") String name) {};

}
