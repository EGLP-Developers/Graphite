package me.eglp.gv2.multiplex;

@FunctionalInterface
public interface ContextHandle {
	
	public void reset();
	
	public default void withContext(Runnable run) {
		ContextHandle other = GraphiteMultiplex.handle();
		reset();
		run.run();
		other.reset();
	}

}
