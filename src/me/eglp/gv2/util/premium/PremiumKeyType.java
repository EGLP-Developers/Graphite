package me.eglp.gv2.util.premium;

import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.LocalizedString;

public enum PremiumKeyType {

	PATREON(DefaultLocaleString.PREMIUM_KEY_TYPE_PATREON_FRIENDLY_NAME, DefaultLocaleString.PREMIUM_KEY_TYPE_PATREON_DESCRIPTION),
	TEMPORARY(DefaultLocaleString.PREMIUM_KEY_TYPE_TEMPORARY_FRIENDLY_NAME, DefaultLocaleString.PREMIUM_KEY_TYPE_TEMPORARY_DESCRIPTION),
	PERMANENT(DefaultLocaleString.PREMIUM_KEY_TYPE_PERMANENT_FRIENDLY_NAME, DefaultLocaleString.PREMIUM_KEY_TYPE_PERMANENT_DESCRIPTION);
	
	private final LocalizedString friendlyTypeName, description;
	
	private PremiumKeyType(LocalizedString friendlyTypeName, LocalizedString description) {
		this.friendlyTypeName = friendlyTypeName;
		this.description = description;
	}
	
	public LocalizedString getFriendlyTypeName() {
		return friendlyTypeName;
	}
	
	public LocalizedString getDescription() {
		return description;
	}
	
}
