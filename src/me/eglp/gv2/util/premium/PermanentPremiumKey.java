package me.eglp.gv2.util.premium;

import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.lang.DefaultLocaleString;

public class PermanentPremiumKey implements PremiumKey {

	private String id;
	private GraphiteUser owner;
	private PremiumLevel premiumLevel;
	private GraphiteGuild redeemedGuild;
	
	public PermanentPremiumKey(String id, GraphiteUser owner, PremiumLevel premiumLevel) {
		this.id = id;
		this.owner = owner;
		this.premiumLevel = premiumLevel;
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
		return PremiumKeyType.PERMANENT;
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
	public boolean hasExpired() {
		return false;
	}
	
	@Override
	public String getFormattedExpirationTime(GraphiteLocalizable localizable) {
		return DefaultLocaleString.PREMIUM_KEY_TYPE_PERMANENT_EXPIRES.getFor(localizable);
	}

}
