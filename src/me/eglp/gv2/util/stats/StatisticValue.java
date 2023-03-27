package me.eglp.gv2.util.stats;

public class StatisticValue {
	
	private GraphiteStatistic statistic;
	private String category;
	private long timestamp;
	private int value;
	
	public StatisticValue(GraphiteStatistic statistic, String category, long timestamp, int value) {
		this.statistic = statistic;
		this.category = category;
		this.timestamp = timestamp;
		this.value = value;
	}
	
	
	public StatisticValue(GraphiteStatistic statistic, long timestamp, int value) {
		this(statistic, null, timestamp, value);
	}
	
	public GraphiteStatistic getStatistic() {
		return statistic;
	}
	
	public String getCategory() {
		return category;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public int getValue() {
		return value;
	}
	
}
