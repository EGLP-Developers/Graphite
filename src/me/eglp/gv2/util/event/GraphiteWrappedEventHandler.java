package me.eglp.gv2.util.event;

import java.util.concurrent.ScheduledFuture;

public class GraphiteWrappedEventHandler {

	private GenericEventHandler handler;
	private long removeAt;
	private ScheduledFuture<?> unregisterFuture;
	
	public GraphiteWrappedEventHandler(GenericEventHandler handler, long removeAt) {
		this.handler = handler;
		this.removeAt = removeAt;
	}
	
	public GraphiteWrappedEventHandler(GenericEventHandler handler) {
		this(handler, -1);
	}
	
	public GenericEventHandler getHandler() {
		return handler;
	}
	
	public long getRemoveAt() {
		return removeAt;
	}
	
	public boolean isExpired() {
		return removeAt != -1 && System.currentTimeMillis() >= removeAt;
	}
	
	public void setUnregisterFuture(ScheduledFuture<?> unregisterFuture) {
		this.unregisterFuture = unregisterFuture;
	}
	
	public ScheduledFuture<?> getUnregisterFuture() {
		return unregisterFuture;
	}
	
	public void onGenericEvent(Object event) {
		handler.onGenericEvent(event);
	}
	
}
