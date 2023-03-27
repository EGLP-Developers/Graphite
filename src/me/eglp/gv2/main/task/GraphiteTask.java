package me.eglp.gv2.main.task;

public interface GraphiteTask {

	public String getName();
	
	public Runnable getCommand();
	
	public boolean hasTerminated();
	
	public boolean hasTerminatedNormally();
	
	public void restart();
	
	public void stop(boolean allowInterrupt);
	
}
