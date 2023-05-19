package me.eglp.gv2.guild;

import java.util.Collection;
import java.util.EnumSet;

import me.eglp.gv2.util.base.GraphiteIdentifiable;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

public interface GraphiteGuildChannel extends GraphiteIdentifiable {

	public GuildChannel getJDAChannel();

	public GraphiteGuild getGuild();

	// NONBETA: remove because JDA removed it as well?
	public GraphiteCategory getCategory();

	public String getName();

	public default String getAsMention() {
		return getJDAChannel().getAsMention();
	}

	public default EnumSet<Permission> getSelfPermissions() {
		return getGuild().getSelfMember().getPermissions(this);
	}

	public default boolean hasPermissions(Permission... permissions) {
		return getGuild().hasPermissions(permissions);
	}

	public default boolean hasPermissions(Collection<Permission> permissions) {
		return getGuild().hasPermissions(permissions);
	}

}
