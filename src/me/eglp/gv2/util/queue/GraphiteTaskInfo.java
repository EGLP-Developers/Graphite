package me.eglp.gv2.util.queue;

public class GraphiteTaskInfo {
	
	private String id, name;
	private QueueTask<?> task;
	private boolean running;

	public GraphiteTaskInfo(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getID() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	void setTask(QueueTask<?> task) {
		this.task = task;
	}
	
	public QueueTask<?> getTask() {
		return task;
	}
	
	void setRunning(boolean running) {
		this.running = running;
	}
	
	public boolean isRunning() {
		return running;
	}
	
}
