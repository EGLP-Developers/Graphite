package me.eglp.gv2.util.command;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.base.GraphiteOwning;

public interface CommandSender extends GraphiteOwning, GraphiteLocalizable {

	public default String getPrefix() {
		return isGuild() ? asGuild().getConfig().getPrefix() : Graphite.getBotInfo().getDefaultPrefix();
	}
	
}
