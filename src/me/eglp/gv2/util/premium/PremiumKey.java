package me.eglp.gv2.util.premium;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.user.GraphiteUser;

public interface PremiumKey {
	
	public String getID();
	
	public void setOwner(GraphiteUser owner);
	
	public GraphiteUser getOwner();
	
	public PremiumLevel getPremiumLevel();

	public PremiumKeyType getKeyType();
	
	public void setRedeemedGuild(GraphiteGuild guild);
	
	public GraphiteGuild getRedeemedGuild();
	
	public String getFormattedExpirationTime(GraphiteLocalizable localizable);
	
	public boolean hasExpired();
	
	public default boolean hasOwner() {
		return getOwner() != null;
	}
	
	public default boolean isInUse() {
		return getRedeemedGuild() != null;
	}
	
	public default boolean isPermanent() {
		return getKeyType().equals(PremiumKeyType.PERMANENT);
	}
	
	public default boolean isTemporary() {
		return getKeyType().equals(PremiumKeyType.TEMPORARY);
	}
	
	public default void save() {
		Graphite.getPremium().saveKey(this);
	}
	
}
