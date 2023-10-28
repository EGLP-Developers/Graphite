package me.eglp.gv2.guild;

import java.util.Arrays;

public enum GraphiteModule {

	// FIXME: hide unavailable modules when disabled in config
	MUSIC("Music"),
	FUN("Fun"),
	BACKUPS("Backups"),
	MODERATION("Moderation"),
	GREETER("Greeter"),
	ROLE_MANAGEMENT("Role management"),
	CHANNEL_MANAGEMENT("Channel management"),
	RECORD("Record"),
	SCRIPTING("Scripting"),
	TWITCH("Twitch"),
	TWITTER("Twitter"),
	REDDIT("Reddit")
	;

	public final String name;

	private GraphiteModule(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static GraphiteModule getByName(String name) {
		return Arrays.stream(values()).filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	public static GraphiteModule getByValue(String value) {
		return Arrays.stream(values()).filter(m -> m.name().equalsIgnoreCase(value.toUpperCase())).findFirst().orElse(null);
	}

}
