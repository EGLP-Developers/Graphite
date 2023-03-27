package me.eglp.gv2.util.selfcheck.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.task.GraphiteAlwaysRepeatingTask;
import me.eglp.gv2.main.task.GraphiteFixedDelayTask;
import me.eglp.gv2.main.task.GraphiteFixedRateTask;
import me.eglp.gv2.main.task.GraphiteTask;
import me.eglp.gv2.util.selfcheck.GraphiteCheck;
import me.eglp.gv2.util.selfcheck.GraphiteCheckException;

public class GraphiteTasksCheck implements GraphiteCheck {

	private static final int TASK_CRASH_THRESHOLD = 5;
	
	private Map<GraphiteTask, Long> lastCrash;
	private Map<GraphiteTask, Integer> taskCrashes;
	private List<GraphiteTask> failedTasks;
	
	public GraphiteTasksCheck() {
		this.lastCrash = new HashMap<>();
		this.taskCrashes = new HashMap<>();
		this.failedTasks = new ArrayList<>();
	}
	
	@Override
	public void runCheck() throws GraphiteCheckException {
		for(GraphiteTask task : new ArrayList<>(Graphite.getScheduler().getTasks())) {
			if(task.hasTerminated() && !task.hasTerminatedNormally()) {
				if(failedTasks.contains(task)) continue;
				Graphite.log("Crash @ " + task.getName());
				long l = System.currentTimeMillis() - lastCrash.getOrDefault(task, 0L);
				long period;
				if(task instanceof GraphiteFixedDelayTask) {
					period = ((GraphiteFixedDelayTask) task).getDelay();
				}else if(task instanceof GraphiteFixedRateTask) {
					period = ((GraphiteFixedRateTask) task).getPeriod();
				}else if(task instanceof GraphiteAlwaysRepeatingTask){
					period = 60000;
				}else continue;
				if(l < period) {
					int c = taskCrashes.getOrDefault(task, 0) + 1;
					if(c > TASK_CRASH_THRESHOLD) {
						Graphite.log("Task \"" + task.getName() + "\" crashed after " + TASK_CRASH_THRESHOLD + " tries");
						failedTasks.add(task);
					}
					taskCrashes.put(task, c);
				}else {
					taskCrashes.put(task, 0);
				}
				task.restart();
				lastCrash.put(task, System.currentTimeMillis());
			}
		}
	}

}
