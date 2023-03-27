package me.eglp.gv2.util.stats;

import me.eglp.gv2.util.base.guild.GraphiteGuild;

public interface StatisticValueGetter {

	public int getCurrentValue(GraphiteGuild guild);
	
}
