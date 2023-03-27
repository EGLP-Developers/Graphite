package me.eglp.gv2.util.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import me.eglp.gv2.main.Graphite;
import me.mrletsplay.mrcore.misc.FriendlyException;

public interface AnnotationEventHandler extends GenericEventHandler {
	
	@Override
	public default void onGenericEvent(Object event) {
		for(Method m : getClass().getMethods()) {
			EventHandler h = m.getAnnotation(EventHandler.class);
			if(h == null) continue;
			
			Class<?>[] params = m.getParameterTypes();
			if(params.length != 1) {
				Graphite.log("Invalid event handler: " + getClass().getName() + ":" + m);
				continue;
			}
			
			if(params[0].isInstance(event)) {
				m.setAccessible(true);
				try {
					m.invoke(this, event);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}catch(InvocationTargetException e) {
					throw new FriendlyException("Error in event handler", e);
				}
			}
		}
	}
	
}
