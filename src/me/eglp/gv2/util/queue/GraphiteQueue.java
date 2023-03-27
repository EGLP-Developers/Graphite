package me.eglp.gv2.util.queue;

import java.util.concurrent.Callable;

import me.eglp.gv2.util.base.guild.GraphiteGuild;

public class GraphiteQueue {

	private String name;
	private GraphiteTaskQueue taskQueue, heavyTaskQueue;
	
	public GraphiteQueue(String name, int numRunnerThreads, int numHeavyRunnerThreads) {
		this.name = name;
		this.taskQueue = new GraphiteTaskQueue(name, numRunnerThreads);
		this.heavyTaskQueue = new GraphiteTaskQueue(name + "-Heavy", numHeavyRunnerThreads);
	}
	
	public String getName() {
		return name;
	}
	
	public synchronized boolean isBusy(GraphiteGuild guild) {
		return taskQueue.isBusy(guild) || heavyTaskQueue.isBusy(guild);
	}
	
	public synchronized boolean isHeavyBusy(GraphiteGuild guild) {
		return heavyTaskQueue.isBusy(guild);
	}
	
	public boolean isBusy() {
		return taskQueue.isBusy();
	}
	
	public boolean isHeavyBusy() {
		return heavyTaskQueue.isBusy();
	}
	
	public GraphiteTaskInfo getHeavyTask(GraphiteGuild guild) {
		return heavyTaskQueue.getTask(guild);
	}
	
	public synchronized QueueTask<?> queue(GraphiteGuild guild, Runnable run) {
		return taskQueue.queue(guild, null, run);
	}
	
	public synchronized <T> QueueTask<T> queue(GraphiteGuild guild, Callable<T> callable) {
		return taskQueue.queue(guild, null, callable);
	}
	
	public synchronized QueueTask<?> queueHeavy(GraphiteGuild guild, GraphiteTaskInfo taskInfo, Runnable run) {
		return heavyTaskQueue.queue(guild, taskInfo, run);
	}
	
	public synchronized <T> QueueTask<T> queueHeavy(GraphiteGuild guild, GraphiteTaskInfo taskInfo, Callable<T> callable) {
		return heavyTaskQueue.queue(guild, taskInfo, callable);
	}
	
	public synchronized void stop(int timeoutSeconds) {
		taskQueue.stop(timeoutSeconds);
		heavyTaskQueue.stop(timeoutSeconds);
	}
	
}
