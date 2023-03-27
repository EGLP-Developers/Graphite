package me.eglp.gv2.main.task;

import java.util.concurrent.TimeUnit;

public interface GraphiteScheduledTask extends GraphiteTask {

	public long getDelay(TimeUnit timeUnit);
	
}
