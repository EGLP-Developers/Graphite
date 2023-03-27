package me.eglp.gv2.util.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;

public class GraphiteListener {

	protected List<GraphiteWrappedEventHandler> handlers;
	
	public GraphiteListener() {
		handlers = new ArrayList<>();
	}
	
	public synchronized void registerHandler(GenericEventHandler handler) {
		handlers.add(new GraphiteWrappedEventHandler(handler));
	}
	
	public synchronized void registerTemporaryHandler(GenericEventHandler handler, Runnable onRemove, long amount, TimeUnit timeUnit) {
		long expiresAt = System.currentTimeMillis() + timeUnit.toMillis(amount);
		GraphiteWrappedEventHandler wHandler = new GraphiteWrappedEventHandler(handler, expiresAt);
		handlers.add(wHandler);
		ScheduledFuture<?> f = Graphite.getScheduler().getExecutorService().schedule(() -> {
			if(!handlers.contains(wHandler)) return; // Handler has already been unregistered
			unregisterHandler(wHandler);
			if(onRemove != null) onRemove.run();
		}, amount, timeUnit);
		wHandler.setUnregisterFuture(f);
	}
	
	public synchronized void registerTemporaryHandler(GenericEventHandler handler, long amount, TimeUnit timeUnit) {
		registerTemporaryHandler(handler, null, amount, timeUnit);
	}

	public void unregisterAll() {
		new ArrayList<>(handlers).forEach(this::unregisterHandler);
	}
	public void unregisterHandler(GenericEventHandler handler) {
		unregisterHandlersIf(h -> h.getHandler().equals(handler));
	}
	
	private synchronized void unregisterHandlersIf(Predicate<GraphiteWrappedEventHandler> predicate) {
		new ArrayList<>(handlers).stream()
			.filter(predicate)
			.forEach(this::unregisterHandler);
	}
	
	public synchronized void unregisterHandler(GraphiteWrappedEventHandler handler) {
		handlers.remove(handler);
		if(handler.getUnregisterFuture() != null) handler.getUnregisterFuture().cancel(false);
	}
	
	public List<GraphiteWrappedEventHandler> getHandlers() {
		return handlers;
	}

	public synchronized void fire(Object event) {
		for(GraphiteWrappedEventHandler h : new ArrayList<>(handlers)) {
			try {
				if(h.isExpired()) continue;
				h.onGenericEvent(event);
			}catch(Exception e) {
				GraphiteDebug.log(DebugCategory.MISCELLANEOUS, e);
			}
		}
	}
	
}
