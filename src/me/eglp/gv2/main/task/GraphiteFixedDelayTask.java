package me.eglp.gv2.main.task;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import me.eglp.gv2.main.Graphite;

public class GraphiteFixedDelayTask implements GraphiteScheduledTask {

	private String name;
	private ScheduledFuture<?> task;
	private Runnable run;
	private long delay;
	private boolean shutdownNormally;
	
	public GraphiteFixedDelayTask(String name, Runnable run, long delay) {
		this.name = name;
		this.run = run;
		this.delay = delay;
	}
	
	@Override
	public long getDelay(TimeUnit unit) {
		return task.getDelay(unit);
	}

	@Override
	public void stop(boolean mayInterruptIfRunning) {
		shutdownNormally = true;
		task.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean hasTerminated() {
		return task.isDone();
	}
	
	public void setTask(ScheduledFuture<?> task) {
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
	
	public long getDelay() {
		return delay;
	}

	@Override
	public boolean hasTerminatedNormally() {
		return shutdownNormally;
	}
	
}
