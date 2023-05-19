package me.eglp.gv2.util.stats;

import java.util.Map;

import me.eglp.gv2.guild.GraphiteGuild;

public interface CategorizedStatisticValueGetter {

	public Map<String, Integer> getCurrentValues(GraphiteGuild guild);

}
