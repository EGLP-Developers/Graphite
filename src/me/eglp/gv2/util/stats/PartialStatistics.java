package me.eglp.gv2.util.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.stats.element.StatisticsElementPointFrequency;

public class PartialStatistics {

	private GraphiteGuild guild;
	private GraphiteStatistic statistic;
	private String category;
	private Map<Long, Integer> valueMap;
	private StatisticsElementPointFrequency pointFrequency;

	public PartialStatistics(GraphiteGuild guild, GraphiteStatistic statistic, String category, List<StatisticValue> values, StatisticsElementPointFrequency pointFrequency) {
		this.guild = guild;
		this.statistic = statistic;
		this.category = category;
		this.valueMap = new HashMap<>();
		this.pointFrequency = pointFrequency;

		if(!values.isEmpty()) {
			long start = values.stream()
					.mapToLong(v -> v.getTimestamp())
					.min().getAsLong();
			long end = values.stream()
					.mapToLong(v -> v.getTimestamp())
					.max().getAsLong();

			List<Long> ts = pointFrequency.getUTCTimestampsBetween(guild, start, end);
			for(Long time : ts) {
				long nextBigger = ts.stream().filter(t -> t > time).mapToLong(t -> t).min().orElse(Long.MAX_VALUE);
				IntStream i = values.stream()
						.filter(v -> v.getTimestamp() >= time && v.getTimestamp() < nextBigger)
						.mapToInt(v -> v.getValue());
				valueMap.put(time, statistic.isCumulative() ? i.sum() : (int) (i.average().orElse(0)));
			}
		}
	}

	public PartialStatistics(GraphiteGuild guild, GraphiteStatistic statistic, List<StatisticValue> values, StatisticsElementPointFrequency pointFrequency) {
		this(guild, statistic, null, values, pointFrequency);
	}

	public GraphiteStatistic getStatistic() {
		return statistic;
	}

	public String getCategory() {
		return category;
	}

	public int getValueAt(long timestamp) {
		return valueMap.getOrDefault(pointFrequency.getCorrespondingUTCTimestamp(guild, timestamp), 0);
	}

	public int maxValue() {
		return valueMap.values().stream()
				.mapToInt(v -> v)
				.max().orElse(0);
	}

	public int minValue() {
		return valueMap.values().stream()
				.mapToInt(v -> v)
				.min().orElse(0);
	}

	public int maxValue(int sign) {
		return valueMap.values().stream()
				.mapToInt(v -> v)
				.filter(v -> GraphiteUtil.simpleSignum(v) == sign)
				.map(v -> v * sign)
				.max().orElse(0) * sign;
	}

	public int minValue(int sign) {
		return valueMap.values().stream()
				.mapToInt(v -> v)
				.filter(v -> GraphiteUtil.simpleSignum(v) == sign)
				.map(v -> v * sign)
				.min().orElse(0) * sign;
	}

	public boolean isEmpty() {
		return valueMap.entrySet().stream().allMatch(en -> en.getValue() == 0);
	}

	public double sortValue() {
		IntStream s = valueMap.values().stream().mapToInt(i -> i);
		return statistic.isCumulative() ? s.sum() : s.average().getAsDouble();
	}

	public long getNewestTimestamp() {
		return valueMap.keySet().stream().mapToLong(l -> l).max().orElse(0);
	}

}
