package me.eglp.gv2.util.base;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.user.GraphiteUser;

public interface GraphiteUserOrGuild {

	public default boolean isGuild() {
		return this instanceof GraphiteGuild;
	}

	public default boolean isUser() {
		return this instanceof GraphiteUser;
	}

	public default GraphiteGuild asGuild() {
		return this instanceof GraphiteGuild ? (GraphiteGuild) this : null;
	}

	public default GraphiteUser asUser() {
		return this instanceof GraphiteUser ? (GraphiteUser) this : null;
	}

}
