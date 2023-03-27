package me.eglp.gv2.util.premium;

import java.util.Arrays;

import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.LocalizedString;

public enum PremiumLevel {
	
	NONE(null, "None", DefaultLocaleString.PREMIUM_LEVEL_NONE, 0, 5, 5),
	DONATOR("donator", "Donator", DefaultLocaleString.PREMIUM_LEVEL_DONATOR, 1, -1, 15);

	private final String id, benefitName;
	private final LocalizedString friendlyName;
	private final int
		patreonUserKeys,
		maxBackups,
		maxAutoBackups;
	
	private PremiumLevel(String id, String benefitName, LocalizedString friendlyName, int patreonUserKeys, int maxBackups, int maxAutoBackups) {
		this.id = id;
		this.benefitName = benefitName;
		this.friendlyName = friendlyName;
		this.patreonUserKeys = patreonUserKeys;
		this.maxBackups = maxBackups;
		this.maxAutoBackups = maxAutoBackups;
	}
	
	public static PremiumLevel fromPatreonBenefitName(String benefitName) {
		return Arrays.stream(values())
				.filter(p -> p.benefitName != null && p.benefitName.equalsIgnoreCase(benefitName))
				.findFirst().orElse(NONE);
	}
	
	public static PremiumLevel ofID(String id) {
		return Arrays.stream(values())
				.filter(p -> p.id != null && p.id.equalsIgnoreCase(id))
				.findFirst().orElse(null);
	}
	
	public String getID() {
		return id;
	}
	
	public String getPatreonBenefitName() {
		return benefitName;
	}
	
	public LocalizedString getFriendlyName() {
		return friendlyName;
	}
	
	public int getPatreonUserKeysAmount() {
		return patreonUserKeys;
	}
	
	public int getMaxBackupAmount() {
		return maxBackups;
	}
	
	public int getMaxAutoBackupAmount() {
		return maxAutoBackups;
	}
	
}
