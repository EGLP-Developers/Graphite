package me.eglp.gv2.util.apis.patreon;

import com.patreon.resources.Pledge;

import me.eglp.gv2.util.premium.PremiumLevel;

public class PatreonPledge {
	
	private GraphitePatreon patreon;
	private GraphitePatron patron;
	private String id, rewardID;
	private PremiumLevel premiumLevel;
	private int amount;
	private String currency;

	public PatreonPledge(GraphitePatreon patreon, GraphitePatron patron, String pledgeID, String rewardID, PremiumLevel premiumLevel, int amount, String currency) {
		this.patreon = patreon;
		this.patron = patron;
		this.id = pledgeID;
		this.rewardID = rewardID;
		this.premiumLevel = premiumLevel;
		this.amount = amount;
		this.currency = currency;
	}
	
	public GraphitePatreon getPatreon() {
		return patreon;
	}
	
	public GraphitePatron getPatron() {
		return patron;
	}
	
	public String getID() {
		return id;
	}
	
	public String getRewardID() {
		return rewardID;
	}
	
	public PremiumLevel getPremiumLevel() {
		return premiumLevel;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public String getCurrency() {
		return currency;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof PatreonPledge)) return false;
		return ((PatreonPledge) other).patron.getPatreonID().equals(patron.getPatreonID());
	}
	
	public static PatreonPledge of(GraphitePatreon patreon, Pledge pledge) {
		return new PatreonPledge(patreon, new GraphitePatron(pledge.getPatron()), pledge.getId(), pledge.getReward().getId(), PremiumLevel.fromPatreonBenefitName(pledge.getReward().getTitle()), pledge.getAmountCents(), pledge.getCurrency());
	}
	
}
