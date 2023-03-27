package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.util.premium.PremiumLevel;

public class JSPremiumLevel {
	
	private PremiumLevel level;
	
	public JSPremiumLevel(PremiumLevel level) {
		this.level = level;
	}
	
	/**
	 * Returns the friendly name of this premium level (e.g. {@code Graphite Diamond}), localized for the provided guild
	 * @param guild The guild to localize for
	 * @return The friendly name of this premium level
	 */
	public String getName(JSGuild guild) {
		return level.getFriendlyName().getFor(guild.guild);
	}
	
	/**
	 * Returns the maximum amount of backups a guild can have with this premium level
	 * @return The maximum amount of backups a guild can have with this premium level
	 */
	public int getMaxBackups() {
		return level.getMaxBackupAmount();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof JSPremiumLevel)) return false;
		return level.equals(((JSPremiumLevel) obj).level);
	}
	
	@Override
	public String toString() {
		return "[JS Premium Level: " + level.name() + "]";
	}
	
}
