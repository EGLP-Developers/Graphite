package me.eglp.gv2.util.event;

import java.util.function.Consumer;

public abstract class SingleEventHandler<T> implements GenericEventHandler {
	
	private Class<T> eventClass;
	
	public SingleEventHandler(Class<T> eventClass) {
		this.eventClass = eventClass;
	}
	
	@Override
	public void onGenericEvent(Object event) {
		if(!eventClass.isInstance(event)) return;
		onEvent(eventClass.cast(event));
	}
	
	public abstract void onEvent(T event);
	
	public static <T> SingleEventHandler<T> of(Class<T> eventClass, Consumer<T> callback) {
		return new SingleEventHandler<>(eventClass) {

			@Override
			public void onEvent(T event) {
				callback.accept(event);
			}
		};
	}
	
}
