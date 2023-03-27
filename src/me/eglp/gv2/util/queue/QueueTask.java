package me.eglp.gv2.util.queue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import me.eglp.gv2.multiplex.ContextHandle;
import me.eglp.gv2.multiplex.GraphiteMultiplex;

public class QueueTask<T> extends CompletableFuture<T> {
	
	private static ThreadLocal<QueueTask<?>> runningTask = new ThreadLocal<>();
	
	private Future<T> wrappedFuture;
	private ContextHandle handle;
	
	public QueueTask() {
		this.handle = GraphiteMultiplex.handle();
	}
	
	void setWrappedFuture(Future<T> wrap) {
		this.wrappedFuture = wrap;
	}
	
	public boolean check() {
		if(!wrappedFuture.isDone()) return false;
		handle.reset();
		try {
			complete(wrappedFuture.get());
		} catch (InterruptedException | ExecutionException e) {
			completeExceptionally(e.getCause());
		}
		return true;
	}
	
	public void cancel() {
		super.cancel(true);
	}
	
	public static void setRunningTask(QueueTask<?> task) {
		if(task == null) {
			runningTask.remove();
			return;
		}
		runningTask.set(task);
	}
	
	public static QueueTask<?> getRunningTask() {
		return runningTask.get();
	}
	
	public static boolean isCurrentCancelled() {
		QueueTask<?> t = getRunningTask();
		if(t == null) return false;
		return t.isCancelled();
	}
	
}
