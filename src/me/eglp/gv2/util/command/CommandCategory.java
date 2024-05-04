package me.eglp.gv2.util.command;

import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.LocalizedString;

public enum CommandCategory {

	ADMIN(DefaultLocaleString.COMMAND_CATEGORY_ADMIN_NAME),
	MUSIC(DefaultLocaleString.COMMAND_CATEGORY_MUSIC_NAME),
	BACKUPS(DefaultLocaleString.COMMAND_CATEGORY_BACKUPS_NAME),
	ROLE_MANAGEMENT(DefaultLocaleString.COMMAND_CATEGORY_ROLE_MANAGEMENT_NAME),
	REPORT(DefaultLocaleString.COMMAND_CATEGORY_REPORT_NAME),
	INFO(DefaultLocaleString.COMMAND_CATEGORY_INFO_NAME),
	FUN(DefaultLocaleString.COMMAND_CATEGORY_FUN_NAME),
	MODERATION(DefaultLocaleString.COMMAND_CATEGORY_MODERATION_NAME),
	TWITCH(DefaultLocaleString.COMMAND_CATEGORY_TWITCH_NAME),
	REDDIT(DefaultLocaleString.COMMAND_CATEGORY_REDDIT_NAME),
	GREETER(DefaultLocaleString.COMMAND_CATEGORY_GREETER_NAME),
	SCRIPTING(DefaultLocaleString.COMMAND_CATEGORY_SCRIPTING_NAME),
	RECORD(DefaultLocaleString.COMMAND_CATEGORY_RECORD_NAME),
	CHANNEL_MANAGEMENT(DefaultLocaleString.COMMAND_CATEGORY_CHANNEL_MANAGEMENT_NAME);

	private LocalizedString name;

	private CommandCategory(LocalizedString name) {
		this.name = name;
	}

	public LocalizedString getName() {
		return name;
	}

}
