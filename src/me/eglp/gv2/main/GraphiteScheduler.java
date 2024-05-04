package me.eglp.gv2.main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import me.eglp.gv2.main.task.GraphiteAlwaysRepeatingTask;
import me.eglp.gv2.main.task.GraphiteFixedDelayTask;
import me.eglp.gv2.main.task.GraphiteFixedRateTask;
import me.eglp.gv2.main.task.GraphiteTask;
import me.eglp.gv2.multiplex.ContextHandle;
import me.eglp.gv2.multiplex.GraphiteMultiplex;

public class GraphiteScheduler {

	private ScheduledExecutorService executorService;
	private List<GraphiteTask> tasks;

	public GraphiteScheduler() {
		executorService = Executors.newScheduledThreadPool(40);
		tasks = new ArrayList<>();
		Graphite.log("Scheduler started");
	}

	public void execute(Runnable run) {
		executorService.execute(() -> {
			try {
				run.run();
			} catch(Exception e) {
				GraphiteDebug.log(DebugCategory.MISCELLANEOUS, e);
			}
		});
	}

	public GraphiteFixedRateTask scheduleAtFixedRate(String name, Runnable run, long period) {
		GraphiteFixedRateTask task = new GraphiteFixedRateTask(name, run, period);
		scheduleTask(task);
		tasks.add(task);
		return task;
	}

	public GraphiteFixedDelayTask scheduleWithFixedDelay(String name, Runnable run, long delay) {
		GraphiteFixedDelayTask task = new GraphiteFixedDelayTask(name, run, delay);
		scheduleTask(task);
		tasks.add(task);
		return task;
	}

	public GraphiteAlwaysRepeatingTask scheduleAlwaysRepeating(String name, Runnable run) {
		GraphiteAlwaysRepeatingTask task = new GraphiteAlwaysRepeatingTask(name, run);
		scheduleTask(task);
		tasks.add(task);
		return task;
	}

	public synchronized void scheduleTask(GraphiteFixedRateTask task) {
		if(Graphite.isShutdown) return;
		ContextHandle h = GraphiteMultiplex.handle();
		ScheduledFuture<?> f = executorService.scheduleAtFixedRate(() -> executeTask(h, task), 10, task.getPeriod(), TimeUnit.MILLISECONDS);
		task.setTask(f);
	}

	public synchronized void scheduleTask(GraphiteFixedDelayTask task) {
		if(Graphite.isShutdown) return;
		ContextHandle h = GraphiteMultiplex.handle();
		ScheduledFuture<?> f = executorService.scheduleWithFixedDelay(() -> executeTask(h, task), 10, task.getDelay(), TimeUnit.MILLISECONDS);
		task.setTask(f);
	}

	public synchronized void scheduleTask(GraphiteAlwaysRepeatingTask task) {
		if(Graphite.isShutdown) return;
		ContextHandle h = GraphiteMultiplex.handle();
		Future<?> f = executorService.submit(() -> executeTask(h, task));
		task.setTask(f);
	}

	private void executeTask(ContextHandle handle, GraphiteTask task) {
		try {
			handle.reset();
			task.getCommand().run();
		}catch(Exception e) {
			Graphite.log("Task \"" + task.getName() + "\" raised an exception");
			GraphiteDebug.log(DebugCategory.TASK, e);
			task.stop(false);
		}
	}

	public ScheduledExecutorService getExecutorService() {
		return executorService;
	}

	public synchronized List<GraphiteTask> getTasks() {
		return tasks;
	}

	public GraphiteTask getTaskByName(String name) {
		return tasks.stream().filter(t -> t.getName().equals(name)).findFirst().orElse(null);
	}

	public boolean isShutdown() {
		return executorService.isShutdown() || executorService.isTerminated();
	}

	public void stop(int timeoutSeconds) {
		try {
			Graphite.log("Shutting down scheduler");
			executorService.shutdown();
			executorService.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
		}catch(InterruptedException e) {
			Graphite.log("Scheduler \"await shutdown\" interrupted");
		}finally {
			if(!executorService.isTerminated()) {
				Graphite.log("Currently running scheduler task(s) will be interrupted");
				executorService.shutdownNow();
			}
			Graphite.log("Scheduler shutdown finished");
		}
	}

}
