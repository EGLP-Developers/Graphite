package me.eglp.gv2.util.permission;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import me.mrletsplay.mrcore.misc.FriendlyException;

public class DefaultPermissions {
	
	public static final String
		ADMIN_LOCALE_DOWNLOAD = "admin.locale.download",
		ADMIN_LOCALE_UPLOAD = "admin.locale.upload",
		ADMIN_LOCALE_DELETE = "admin.locale.delete",
		ADMIN_LOCALE_LIST = "admin.locale.list",
		ADMIN_LOCALE_SET = "admin.locale.set",
		ADMIN_PERMISSION_ADD = "admin.permission.add",
		ADMIN_PERMISSION_REMOVE = "admin.permission.remove",
		ADMIN_PERMISSION_LIST = "admin.permission.list",
		ADMIN_PERMISSION_CHECK = "admin.permission.check",
		ADMIN_PREFIX = "admin.prefix",
		ADMIN_LOCALE = "admin.locale",
		ADMIN_MODULE_ENABLE = "admin.module.enable",
		ADMIN_MODULE_DISABLE = "admin.module.disable",
		ADMIN_MODULE_LIST = "admin.module.list",
		ADMIN_PURGE = "admin.purge",
		ADMIN_TEXTCOMMANDS = "admin.textcommands",
		ADMIN_UPDATESLASHCOMMANDS = "admin.updateslashcommands",
		ADMIN_TIMEZONE = "admin.timezone",
		
		PREMIUM_REDEEM = "premium.redeem",
		PREMIUM_LEVEL = "premium.level",
		
		BACKUP_CREATE = "backup.create",
		BACKUP_CANCEL = "backup.cancel",
		BACKUP_RESTORE = "backup.restore",
		BACKUP_CLONE = "backup.clone",
		BACKUP_COPY = "backup.copy",
		BACKUP_COPY_TO_OTHER = "backup.copy-to-other",
		BACKUP_DELETE = "backup.delete",
		BACKUP_INFO = "backup.info",
		BACKUP_LIST = "backup.list",
		BACKUP_PARAMETERS = "backup.parameters",
		BACKUP_INTERVAL = "backup.interval",
		BACKUP_TEMPLATE_CREATE = "backup.template.create",
		BACKUP_TEMPLATE_DELETE = "backup.template.delete",
		BACKUP_TEMPLATE_LOAD = "backup.template.load",
		BACKUP_TEMPLATE_SEARCH = "backup.template.search",
		BACKUP_RENAME = "backup.rename",
		
		MUSIC_PLAY = "music.play",
		MUSIC_PAUSE = "music.pause",
		MUSIC_STOP = "music.stop",
		MUSIC_LOOP = "music.loop",
		MUSIC_VOLUME = "music.volume",
		MUSIC_QUEUE = "music.queue",
		MUSIC_REMOVE = "music.remove",
		MUSIC_SEEK = "music.seek",
		MUSIC_FASTFORWARD = "music.fastforward",
		MUSIC_REWIND = "music.rewind",
		MUSIC_RESUME = "music.resume",
		MUSIC_ENDLESS = "music.endless",
		MUSIC_SHUFFLE = "music.shuffle",
		MUSIC_SKIP = "music.skip",
		MUSIC_JUMP = "music.jump",
		MUSIC_NOWPLAYING = "music.nowplaying",
		MUSIC_LYRICS = "music.lyrics",
		MUSIC_BASSBOOST = "music.bassboost",
		MUSIC_SPEED = "music.speed",
		MUSIC_PITCH = "music.pitch",
		MUSIC_NIGHTCORE = "music.nightcore",
		MUSIC_RESET = "music.reset",
		MUSIC_PLAYLIST_PLAY = "music.playlist.play",
		MUSIC_PLAYLIST_SAVE = "music.playlist.save",
		MUSIC_PLAYLIST_DELETE = "music.playlist.delete",
		MUSIC_PLAYLIST_RENAME = "music.playlist.rename",
		MUSIC_PLAYLIST_LIST = "music.playlist.list",
		MUSIC_PLAYLIST_INFO = "music.playlist.info",
		
		MODERATION_CHATREPORTS = "moderation.chatreports",
		MODERATION_CHATREPORTS_LIST = "moderation.chatreports.list",
		MODERATION_CHATREPORTS_REMOVE = "moderation.chatreports.remove",
		MODERATION_TEMPVOICEMUTE = "moderation.tempvoicemute",
		MODERATION_TEMPCHATMUTE = "moderation.tempchatmute",
		MODERATION_CHATMUTE = "moderation.chatmute",
		MODERATION_CLEARALL = "moderation.clearall",
		MODERATION_TEMPJAIL = "moderation.tempjail",
		MODERATION_TEMPBAN = "moderation.tempban",
		MODERATION_SUPPORT_QUEUE = "moderation.support.queue",
		MODERATION_SUPPORT_UNSETQUEUE = "moderation.support.unsetqueue",
		MODERATION_REPORTS = "moderation.reports",
		MODERATION_REPORTS_LIST = "moderation.reports.list",
		MODERATION_REPORTS_REMOVE = "moderation.reports.remove",
		MODERATION_UNJAIL = "moderation.unjail",
		MODERATION_UNBAN = "moderation.unban",
		MODERATION_VOICEUNMUTE = "moderation.voiceunmute",
		MODERATION_CHATUNMUTE = "moderation.chatunmute",
		MODERATION_CLEAR = "moderation.clear",
		MODERATION_JAIL = "moderation.jail",

		ROLE_ACCESSROLE_ADD = "role.accessrole.add",
		ROLE_ACCESSROLE_REMOVE = "role.accessrole.remove",
		ROLE_ACCESSROLE_LIST = "role.accessrole.list",
		ROLE_AUTOROLE_ADD = "role.autorole.add",
		ROLE_AUTOROLE_REMOVE = "role.autorole.remove",
		ROLE_AUTOROLE_LIST = "role.autorole.list",
		ROLE_BOTROLE_ADD = "role.botrole.add",
		ROLE_BOTROLE_REMOVE = "role.botrole.remove",
		ROLE_BOTROLE_LIST = "role.botrole.list",
		ROLE_MODROLE_ADD = "role.modrole.add",
		ROLE_MODROLE_REMOVE = "role.modrole.remove",
		ROLE_MODROLE_LIST = "role.modrole.list",
				
		GREETER_FAREWELL_CHANNEL  = "greeter.farewell.channel",
		GREETER_FAREWELL_MESSAGE  = "greeter.farewell.message",
		GREETER_FAREWELL_ENABLE = "greeter.farewell.enable",
		GREETER_FAREWELL_DISABLE = "greeter.farewell.disable",
		GREETER_GREETING_CHANNEL  = "greeter.greeting.channel",
		GREETER_GREETING_MESSAGE  = "greeter.greeting.message",
		GREETER_GREETING_ENABLE = "greeter.greeting.enable",
		GREETER_GREETING_DISABLE = "greeter.greeting.disable",
		
		CHANNEL_USERCHANNEL_CREATE = "channel.userchannel.create",
		CHANNEL_AUTOCHANNEL = "channel.autochannel",
		CHANNEL_CATEGORYCOPY = "channel.categorycopy",
		
		RECORD_START = "record.start",
		RECORD_STOP = "record.stop",
		RECORD_DOWNLOAD = "record.download",
		RECORD_DELETE = "record.delete",
		RECORD_LIST = "record.list",
		RECORD_RENAME = "record.rename",
		
		TWITCH_MESSAGE = "twitch.message",
		TWITCH_CHANNEL = "twitch.channel",
		TWITCH_ADD = "twitch.add",
		TWITCH_REMOVE = "twitch.remove",
				
		TWITTER_CHANNEL = "twitter.channel",
		TWITTER_ADD = "twitter.add",
		TWITTER_REMOVE = "twitter.remove",
				
		REDDIT_CHANNEL = "reddit.channel",
		REDDIT_SUBREDDIT_ADD = "reddit.subreddit.add",
		REDDIT_SUBREDDIT_REMOVE = "reddit.subreddit.remove",
		
		SCRIPT_UPLOAD = "script.upload",
		SCRIPT_DOWNLOAD = "script.download",
		SCRIPT_DELETE = "script.delete",
		SCRIPT_LIST = "script.list",
		
		FUN_AMONGUS_CREATE = "fun.amongus.create",
		FUN_AMONGUS_LINK = "fun.amongus.link",
		FUN_TCRANDOM = "fun.tcrandom",
		FUN_VCRANDOM = "fun.vcrandom",
		FUN_POLL_CREATE = "fun.poll.create",
		FUN_POLL_STOP = "fun.poll.stop",
		FUN_POLL_LIST = "fun.poll.list",
		FUN_REMINDER_CREATE = "fun.reminder.create",
		FUN_REMINDER_REMOVE = "fun.reminder.remove",
		FUN_REMINDER_LIST = "fun.reminder.list",
		FUN_REMINDER_INFO = "fun.reminder.info",
		
		WEBINTERFACE_BACKUPS = "webinterface.backups",
		WEBINTERFACE_MUSIC = "webinterface.music",
		WEBINTERFACE_MODERATION = "webinterface.moderation",
		WEBINTERFACE_GREETER = "webinterface.greeter",
		WEBINTERFACE_ROLE_MANAGEMENT = "webinterface.role-management",
		WEBINTERFACE_CHANNEL_MANAGEMENT = "webinterface.channel-management",
		WEBINTERFACE_SCRIPTING = "webinterface.scripting",
		WEBINTERFACE_TWITCH = "webinterface.twitch",
		WEBINTERFACE_TWITTER = "webinterface.twitter",
		WEBINTERFACE_REDDIT = "webinterface.reddit",
		WEBINTERFACE_RECORD = "webinterface.record",
		WEBINTERFACE_CUSTOM_COMMANDS = "webinterface.custom-commands",
		WEBINTERFACE_STATISTICS = "webinterface.statistics";
	
	public static List<String> getPermissions() {
		List<String> p = new ArrayList<>();
		for(Field f : DefaultPermissions.class.getDeclaredFields()) {
			if(f.getType().equals(String.class))
				try {
					p.add((String) f.get(null));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new FriendlyException("yay broken", e);
				}
		}
		return p;
	}
	
}
