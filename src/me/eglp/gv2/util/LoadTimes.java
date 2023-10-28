package me.eglp.gv2.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class LoadTimes {

	private Map<String, Long> times = new HashMap<>();
	private long currentStartTime = -1;

	public void add(String category, long time) {
		times.put(category, times.getOrDefault(category, 0L) + time);
	}

	public void addTimeDiff(String category, long startTime) {
		add(category, System.currentTimeMillis() - startTime);
	}

	public void startTiming() {
		currentStartTime = System.currentTimeMillis();
	}

	public void stopTiming() {
		currentStartTime = -1;
	}

	public void stopTiming(String category) {
		if(currentStartTime == -1) throw new IllegalStateException("Not currently timing");
		addTimeDiff(category, currentStartTime);
		stopTiming();
	}

	public void stopTimingAndRestart(String category) {
		stopTiming(category);
		startTiming();
	}

	public void add(LoadTimes other) {
		other.times.forEach((c, t) -> add(c, t));
	}

	public void print() {
		System.out.println("--- Load times");
		times.entrySet().stream().sorted(Comparator.<Map.Entry<String, Long>, Long>comparing(e -> e.getValue()).reversed()).forEach(e -> {
			System.out.println(String.format("%-25s | %8d ms", e.getKey(), e.getValue()));
		});
		System.out.println("--- Load times end");
	}

}
