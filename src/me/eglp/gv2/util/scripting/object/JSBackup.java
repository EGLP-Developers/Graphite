package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.util.backup.GuildBackup;

public class JSBackup {

	protected GuildBackup backup;
	
	public JSBackup(GuildBackup backup) {
		this.backup = backup;
	}
	
	/**
	 * Returns the guild this backup was created on
	 * @return The guild this backup was created on
	 */
	public JSGuild getGuild() {
		return new JSGuild(backup.getGuild());
	}
	
	/**
	 * Returns the name of this backup
	 * @return The name of this backup
	 */
	public String getName() {
		return backup.getName();
	}
	
	@Override
	public String toString() {
		return "[JS Backup: " + getName() + "]";
	}
	
}
