package me.eglp.gv2.util.queue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.multiplex.ContextHandle;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.util.jdaobject.JDAObject;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class GraphiteTaskQueue {

	private String name;
	private int threadID;
	private int numRunnerThreads;
	private ExecutorService executor;
	private final Map<GraphiteGuild, GraphiteTaskInfo> block;
	private final List<QueueTask<?>> currentTasks;

	public GraphiteTaskQueue(String name, int numRunnerThreads) {
		this.name = name;
		this.numRunnerThreads = numRunnerThreads;
		this.block = Collections.synchronizedMap(new HashMap<>());
		this.currentTasks = new ArrayList<>();
		this.executor = Executors.newFixedThreadPool(numRunnerThreads + 1, this::createThread);
		executor.submit(checkTasks(currentTasks));
	}

	private Thread createThread(Runnable run) {
		return new Thread(run, "Queue-" + name + "-" + (threadID++));
	}

	private Runnable checkTasks(List<QueueTask<?>> taskList) {
		return () -> {
			while(!Thread.interrupted() && !executor.isShutdown()) {
				synchronized (taskList) {
					Iterator<QueueTask<?>> it = taskList.iterator();
					while(it.hasNext()) {
						QueueTask<?> f = it.next();
						if(f.check()) it.remove();
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					return;
				}
			}
		};
	}

	public String getName() {
		return name;
	}

	public synchronized boolean isBusy(GraphiteGuild guild) {
		return block.containsKey(guild);
	}

	public boolean isBusy() {
		return currentTasks.size() >= numRunnerThreads;
	}

	public GraphiteTaskInfo getTask(GraphiteGuild guild) {
		return block.get(guild);
	}

	public synchronized QueueTask<?> queue(GraphiteGuild guild, GraphiteTaskInfo taskInfo, Runnable run) {
		return queue(guild, taskInfo, () -> {
			run.run();
			return null;
		});
	}

	public synchronized <T> QueueTask<T> queue(GraphiteGuild guild, GraphiteTaskInfo taskInfo, Callable<T> callable) {
		ContextHandle handle = GraphiteMultiplex.handle();
		block.put(guild, taskInfo);
		return wrapAndSubmit(currentTasks, taskInfo, () -> {
			T t;
			try {
				ContextHandle handle2 = GraphiteMultiplex.handle();
				handle.reset(); // Set context to queue task context
				t = callable.call();
				handle2.reset(); // Reset to previous context, just to be sure
				JDAObject.clearCurrentCache();
			}catch(Exception e) {
				throw e;
			}finally {
				block.remove(guild);
			}
			return t;
		});
	}

	private <T> QueueTask<T> wrapAndSubmit(List<QueueTask<?>> taskList, GraphiteTaskInfo taskInfo, Callable<T> task) {
		QueueTask<T> f = new QueueTask<>();
		FriendlyException ex = new FriendlyException("Queue error");
		Future<T> future = executor.submit(() -> {
			try {
				if(taskInfo != null) taskInfo.setRunning(true);
				QueueTask.setRunningTask(f);
				return task.call();
			}catch(Exception e) {
				ex.initCause(e);
				GraphiteDebug.log(DebugCategory.TASK, e);
				throw e;
			}finally {
				QueueTask.setRunningTask(null);
			}
		});
		f.setWrappedFuture(future);
		if(taskInfo != null) taskInfo.setTask(f);
		synchronized (taskList) {
			taskList.add(f);
		}
		return f;
	}

	public synchronized void stop(int timeoutSeconds) {
		try {
			Graphite.log("Shutting down task queue " + name);
			executor.shutdown();
			executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
		}catch(InterruptedException e) {
			Graphite.log("Queue \"await shutdown\" interrupted @ task queue " + name);
		}finally {
			if(!executor.isTerminated()) {
				Graphite.log("Currently running queue task(s) will be interrupted @ task queue " + name);
				executor.shutdownNow();
			}
			Graphite.log("Queue shutdown finished @ task queue " + name);
		}
	}

}
