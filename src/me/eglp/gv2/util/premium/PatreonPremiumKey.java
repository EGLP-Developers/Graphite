package me.eglp.gv2.util.premium;

import me.eglp.gv2.util.apis.patreon.GraphitePatron;
import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.lang.DefaultLocaleString;

public class PatreonPremiumKey implements PremiumKey {

	private String id;
	private GraphitePatron patreonUser;
	private PremiumLevel premiumLevel;
	private GraphiteGuild redeemedGuild;
	
	public PatreonPremiumKey(String id, GraphitePatron patreonUser, PremiumLevel premiumLevel) {
		this.id = id;
		this.patreonUser = patreonUser;
		this.premiumLevel = premiumLevel;
	}
	
	public GraphitePatron getPatreonUser() {
		return patreonUser;
	}

	@Override
	public void setOwner(GraphiteUser user) {
		throw new UnsupportedOperationException("Patreon keys are not transferrable");
	}

	@Override
	public GraphiteUser getOwner() {
		return patreonUser.getDiscordUser();
	}
	
	public void setPremiumLevel(PremiumLevel premiumLevel) {
		this.premiumLevel = premiumLevel;
	}

	@Override
	public PremiumLevel getPremiumLevel() {
		return premiumLevel;
	}
	
	@Override
	public PremiumKeyType getKeyType() {
		return PremiumKeyType.PATREON;
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
		return DefaultLocaleString.PREMIUM_KEY_TYPE_PATREON_EXPIRES.getFor(localizable);
	}
	
}
