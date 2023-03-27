package me.eglp.gv2.main.task;

import java.util.concurrent.Future;

import me.eglp.gv2.main.Graphite;

public class GraphiteAlwaysRepeatingTask implements GraphiteTask {
	
	private String name;
	private Future<?> task;
	private Runnable run;
	private boolean shutdownNormally, shouldExit;
	
	public GraphiteAlwaysRepeatingTask(String name, Runnable run) {
		this.name = name;
		this.run = () -> {
			shouldExit = false;
			while(!Thread.interrupted() && !shouldExit) {
				run.run();
			}
		};
	}
	
	@Override
	public void stop(boolean mayInterruptIfRunning) {
		shutdownNormally = true;
		shouldExit = true;
		task.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean hasTerminated() {
		return task.isDone();
	}
	
	public void setTask(Future<?> task) {
		this.task = task;
		this.shutdownNormally = false;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Runnable getCommand() {
		return run;
	}

	@Override
	public void restart() {
		if(!hasTerminated()) stop(false);
		Graphite.getScheduler().scheduleTask(this);
	}
	
	@Override
	public boolean hasTerminatedNormally() {
		return shutdownNormally;
	}

}
