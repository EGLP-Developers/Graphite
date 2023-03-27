package me.eglp.gv2.util.backup.data.channels;

import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;

public interface BackupChannel {
	
	public void restore(GraphiteGuild guild, Category parent, IDMappings mappings);
	
	public int getPosition();

}
