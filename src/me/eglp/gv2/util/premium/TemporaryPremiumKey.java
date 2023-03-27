package me.eglp.gv2.util.premium;

import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.lang.LocalizedTimeUnit;

public class TemporaryPremiumKey implements PremiumKey {

	private String id;
	private GraphiteUser owner;
	private PremiumLevel premiumLevel;
	private long expiresAt;
	private GraphiteGuild redeemedGuild;
	
	public TemporaryPremiumKey(String id, GraphiteUser owner, PremiumLevel premiumLevel, long expiresAt) {
		this.id = id;
		this.owner = owner;
		this.premiumLevel = premiumLevel;
		this.expiresAt = expiresAt;
	}
	
	@Override
	public void setOwner(GraphiteUser owner) {
		this.owner = owner;
		save();
	}

	@Override
	public GraphiteUser getOwner() {
		return owner;
	}

	@Override
	public PremiumLevel getPremiumLevel() {
		return premiumLevel;
	}
	
	@Override
	public PremiumKeyType getKeyType() {
		return PremiumKeyType.TEMPORARY;
	}
	
	public long getExpiresAt() {
		return expiresAt;
	}

	@Override
	public boolean hasExpired() {
		return System.currentTimeMillis() >= expiresAt;
	}
	
	@Override
	public void setRedeemedGuild(GraphiteGuild guild) {
		this.redeemedGuild = guild;
	}

	@Override
	public GraphiteGuild getRedeemedGuild() {
		return redeemedGuild;
	}
	
	@Override
	public String getID() {
		return id;
	}
	
	@Override
	public String getFormattedExpirationTime(GraphiteLocalizable localizable) {
		return LocalizedTimeUnit.formatTime(localizable, expiresAt - System.currentTimeMillis());
	}

}
