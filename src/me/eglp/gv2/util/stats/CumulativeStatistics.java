package me.eglp.gv2.util.stats;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.eglp.gv2.util.base.guild.GraphiteGuild;

public class CumulativeStatistics {
	
	private Map<GraphiteGuild, Map<String, Integer>> cumulative;
	
	public CumulativeStatistics() {
		this.cumulative = new HashMap<>();
	}
	
	public void incrementStatistic(GraphiteGuild guild, String category) {
		Map<String, Integer> m = cumulative.getOrDefault(guild, new HashMap<>());
		m.put(category, m.getOrDefault(category, 0) + 1);
		cumulative.put(guild, m);
	}
	
	public void decrementStatistic(GraphiteGuild guild, String category) {
		Map<String, Integer> m = cumulative.getOrDefault(guild, new HashMap<>());
		m.put(category, m.getOrDefault(category, 0) - 1);
		cumulative.put(guild, m);
	}
	
	public Map<String, Integer> getAndZero(GraphiteGuild guild) {
		Map<String, Integer> v = cumulative.getOrDefault(guild, Collections.emptyMap());
		cumulative.remove(guild);
		return v;
	}

}
