package me.eglp.gv2.util.lang;

import java.awt.Color;
import java.util.Arrays;
import java.util.stream.Collectors;

import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.mention.MentionType;

public enum DefaultMessage implements MessageIdentifier {

	COMMAND_INVITE_MESSAGE("command.invite.message", Color.WHITE, "[**Invite**]({invite_url}) me to your server :hearts:"),
	
	COMMAND_PERMISSION_ADD_ALREADY_HAS_PERMISSION("command.permission.add.already-has-permission", Color.RED, "{entity} already has the specified permission"),
	COMMAND_PERMISSION_ADD_PERMISSION_ADDED("command.permission.add.permission-added", Color.GREEN, "Added permission `{permission}` to {entity}"),
	COMMAND_PERMISSION_REMOVE_PERMISSION_REMOVED("command.permission.remove.permission-removed", Color.GREEN, "Removed permission `{permission}` from {entity}"),
	COMMAND_PERMISSION_REMOVE_DOESNT_HAVE_PERMISSION("command.permission.remove.doesnt-have-permission", Color.RED, "{entity} doesn't have the specified permission"),
	COMMAND_PERMISSION_LIST_NO_PERMISSIONS("command.permission.list.no-permissions", Color.WHITE, "{entity} doesn't have any permissions"),
	COMMAND_PERMISSION_CHECK_HAS_PERMISSION("command.permission.check.has-permission", Color.GREEN, "{entity} has the permission `{permission}`"),
	COMMAND_PERMISSION_CHECK_NO_PERMISSION("command.permission.check.no-permission", Color.RED, "{entity} doesn't have the permission `{permission}`"),
	COMMAND_PERMISSION_ALLOWED_MENTION_TYPES("command.permission.allowed-mention-types", Color.ORANGE, "Allowed mention types: USER, ROLE, EVERYONE"),

	COMMAND_VCRANDOM_PICKED("command.vcrandom.picked", Color.GREEN, "The random member who I picked is:\n> `{member_nick}` (`{member_full}`)"),
	COMMAND_TCRANDOM_PICKED("command.tcrandom.picked", Color.GREEN, "The random member who I picked is:\n> `{member_nick}` (`{member_full}`)"),
	
	COMMAND_POLL_CREATE_DURATION_TOO_SHORT("command.poll.create.duration-too-short", Color.RED, "The duration of the poll needs to be at least 5 minutes"),
	COMMAND_POLL_CREATE_INVALID_EMOJI("command.poll.create.invalid-emoji", Color.RED, "Invalid emoji: {emoji}"),
	COMMAND_POLL_LIST_NO_POLLS("command.poll.list.no-polls", Color.RED, "Currently there are no polls running"),
	COMMAND_POLL_STOP_INVALID_POLL("command.poll.stop.invalid-poll", Color.RED, "A poll with that ID doesn't exist"),
	COMMAND_POLL_STOP_SUCCESS("command.poll.stop.success", Color.GREEN, "The poll has been stopped"),
	
	COMMAND_REMINDER_CREATE_DURATION_TOO_SHORT("command.reminder.create.duration-too-short", Color.RED, "The reminder can only repeat at a minimum of every 5 minutes"),
	COMMAND_REMINDER_CREATE_SUCCESS("command.reminder.create.success", Color.GREEN, "Your reminder got successfully enqueued!"),
	COMMAND_REMINDER_CREATE_EVENT_IS_IN_THE_PAST("command.reminder.create.event-is-in-the-past", Color.RED, "The reminder you tried to enqueue is in the past!"),
	//COMMAND_REMINDER_CREATE_INVALID_EMOJI("command.reminder.create.invalid-emoji", Color.RED, "Invalid emoji: {emoji}"),
	COMMAND_REMINDER_LIST_NO_REMINDERS("command.reminder.list.no-reminder", Color.RED, "Currently there are no reminders active"),
	COMMAND_REMINDER_REMOVE_INVALID_REMINDER("command.reminder.remove.invalid-poll", Color.RED, "A reminder with that ID doesn't exist"),
	COMMAND_REMINDER_REMOVE_SUCCESS("command.reminder.remove.success", Color.GREEN, "The reminder has been removed"),
	
	COMMAND_TEMPBAN_SUCCESS("command.tempban.success", Color.GREEN, "Banned {user} for {duration}: `{reason}`"),
	COMMAND_TEMPBAN_ALREADY_BANNED("command.tempban.already-banned", Color.GREEN, "You have already banned this user"),
	
	COMMAND_TEMPJAIL_SUCCESS("command.tempjail.success", Color.GREEN, ":cloud_lightning: You, the god of this server, have jailed the lowly user {user} for `{duration}` in `{channel}`"),
	COMMAND_TEMPJAIL_ALREADY_JAILED("command.tempjail.already-jailed", Color.RED, "You have already jailed this user"),

	COMMAND_JAIL_JAILED("command.jail.jailed", Color.RED, "You have been jailed and can't move between voice channels, so please don't move! (You'll get voice kicked if you still try to. Attempts: {attempts}/{max_attempts})"),
	COMMAND_JAIL_KICKED("command.jail.kicked", Color.RED, "You have been kicked from your voice channel and can only join the channel you've been jailed in"),
	COMMAND_JAIL_ERROR_AUTOCHANNEL("command.jail.error.autochannel", Color.RED, "You cannot jail a member here because it's an autochannel which will be deleted if nobody is inside"),
	COMMAND_JAIL_ERROR_USERCHANNEL("command.jail.error.userchannel", Color.RED, "You cannot jail a member here because it's an userchannel which will be deleted if nobody is inside"),
	COMMAND_JAIL_ERROR_ALREADY_JAILED("command.jail.error.already-jailed", Color.RED, "User is already jailed"),
	COMMAND_JAIL_SUCCESS("command.jail.success", Color.GREEN, "The user is now jailed in `{channel}` and waiting for their release"),
	
	COMMAND_TEMPVOICEMUTE_SUCCESS("command.tempvoicemute.success", Color.GREEN, "Temporary muted {user} for {duration}: `{reason}`"),

	COMMAND_CHATMUTE_SUCCESS("command.chatmute.success", Color.GREEN, "Muted {user}: `{reason}`"),

	COMMAND_TEMPCHATMUTE_SUCCESS("command.tempchatmute.success", Color.GREEN, "Muted {user} for {duration}: `{reason}`"),
	
	COMMAND_CLEAR_INVALID_AMOUNT("command.clear.invalid-amount", Color.RED, "Can't delete less than one or more than 99 messages"),
	COMMAND_CLEAR_SUCCESS("command.clear.success", Color.GREEN, "Wooosh! Deleted the last `{amount}` messages"),

	COMMAND_PURGE_SUCCESS("command.purge.success", Color.GREEN, "Wooosh! Purged your discord"),
	
	COMMAND_BACKUP_INVALID_BACKUP("command.backup.invalid-backup", Color.RED, "Can't find a backup with the given name"),
	COMMAND_BACKUP_RENAME_ALREADY_EXISTS("command.backup.rename.already-exists", Color.RED, "A backup with the given name already exists"),
	COMMAND_BACKUP_RENAME_MESSAGE("command.backup.rename.message", Color.GRAY, "Renamed backup `{name}` to `{new_name}`"),
	COMMAND_BACKUP_RENAME_INVALID_NAME("command.backup.rename.invalid-name", Color.RED, "The backup name may only contain alphanumerical symbols as well as `-`, `_` or ` `"),
	COMMAND_BACKUP_RESTORED("command.backup.restored", Color.GREEN, "The backup has been successfully restored. Time taken: `{time_taken}`"),
	
	COMMAND_TEMPLATE_CREATE_COOLDOWN("command.template.create.cooldown", Color.RED, "Please wait another {time} before creating another template"),
	
	COMMAND_CLEARALL_SUCCESS("command.clearall.success", Color.GREEN, "Wooosh! Cleared all messages"),

	COMMAND_UNJAIL_NOT_JAILED("command.unjail.not-jailed", Color.RED, "User is not jailed"),

	COMMAND_UNBAN_NOT_BANNED("command.unban.not-banned", Color.RED, "User is not banned"),

	COMMAND_UNMUTE_NOT_MUTED("command.unmute.not-muted", Color.RED, "User is not muted"),

	COMMAND_PREFIX_INVALID("command.prefix.invalid", Color.RED, "The prefix you entered is invalid (up to 16 alphanumeric characters, `_-~.!?` allowed)"),
	COMMAND_PREFIX_SUCCESS("command.prefix.success", Color.DARK_GRAY, "Successfully changed the server prefix to `{prefix}`"),

	COMMAND_REPORT_ALREADY_REPORTED("command.report.already-reported", Color.RED, "You've reported that user already"),
	COMMAND_REPORT_CANT_REPORT_SELF("command.report.cant-report-self", Color.RED, "Do you want to turn yourself in?"),
	COMMAND_REPORT_SUCCESS("command.report.success", Color.GREEN, "Successfully reported {user} with reason: `{reason}`"),

	COMMAND_CHATREPORT_NOT_ENABLED("command.chatreport.not-enabled", Color.RED, "You need to enable chatreports on our [webinterface]({webinterface}) first. You'll receive a decryption key which you need to view all future chat reports"),
	COMMAND_CHATREPORT_SUCCESS("command.chatreport.success", Color.GREEN, "Successfully reported the chat history of this channel. Chat history can be found on our [webinterface]({webinterface})"),
	
	COMMAND_HELP_INVALID_COMMAND("command.help.invalid-command", Color.RED, "I see you're searching for an invalid command? Do you like cookies? [:cookie:](http://orteil.dashnet.org/cookieclicker/)"),
	COMMAND_HELP_SENT("command.help.sent", Color.WHITE, ":see_no_evil: Pssst. Take a look at your DMs."),
	
	COMMAND_TWITCH_USER_ADDED("command.twitch.user.added", Color.GREEN, "Streamer added"),
	COMMAND_TWITCH_USER_REMOVED("command.twitch.user.removed", Color.GREEN, "Streamer removed"),
	COMMAND_TWITCH_ALREADY_ADDED("command.twitch.already-added", Color.RED, "Already added this streamer"),
	COMMAND_TWITCH_SET_MESSAGE("command.twitch.set-message", Color.GREEN, "Changed the message to `{message}`"),
	COMMAND_TWITCH_SET_CHANNEL("command.twitch.set-channel", Color.GREEN, "Changed the notification channel to `{channel}`"),
	COMMAND_TWITCH_STREAMER_NOT_FOUND("command.twitch.streamer-not-found", Color.RED, "I can't find this streamer on your streamer list"),
	COMMAND_TWITCH_INVALID_STREAMER("command.twitch.invalid-streamer", Color.RED, "I can't find this streamer on Twitch"),
	
	COMMAND_TWITTER_USER_ADDED("command.twitter.user.added", Color.GREEN, "Twitter user added"),
	COMMAND_TWITTER_USER_REMOVED("command.twitter.user.removed", Color.GREEN, "Twitter user removed"),
	COMMAND_TWITTER_ALREADY_ADDED("command.twitter.already-added", Color.RED, "Already added this user"),
	COMMAND_TWITTER_SET_CHANNEL("command.twitter.set-channel", Color.GREEN, "Changed the notification channel to `{channel}`"),
	COMMAND_TWITTER_USER_NOT_FOUND("command.twitter.user-not-found", Color.RED, "I can't find this user on your Twitter list"),
	COMMAND_TWITTER_INVALID_USER("command.twitter.invalid-user", Color.RED, "I can't find this user on Twitter"),
	
	COMMAND_REDDIT_SUBREDDIT_ADDED("command.reddit.subreddit.added", Color.GREEN, "Subreddit added"),
	COMMAND_REDDIT_SUBREDDIT_REMOVED("command.reddit.subreddit.removed", Color.GREEN, "Subreddit removed"),
	COMMAND_REDDIT_SUBREDDIT_ALREADY_ADDED("command.reddit.subreddit.already-added", Color.RED, "Already added this subreddit"),
	COMMAND_REDDIT_SET_CHANNEL("command.reddit.set-channel", Color.GREEN, "Changed the notification channel to `{channel}`"),
	COMMAND_REDDIT_INVALID_SUBREDDIT("command.reddit.invalid-subreddit", Color.RED, "I can't find this subreddit on Reddit"),
	
	COMMAND_CHATREPORTS_LIST_NO_REPORTS("command.chatreports.list.no-reports", Color.RED, "Currenty there are no chatreports. Everything is fine"),
	
	COMMAND_CHATREPORTS_REMOVE_SUCCESS("command.chatreports.remove.success", Color.GREEN, "Removed chatreport with index `{index}`"),
	
	COMMAND_REPORTS_NO_REPORTS("command.reports.no-reports", Color.RED, "Currenty there are no reports. Everything is fine"),
	
	COMMAND_REPORTS_REMOVE_SUCCESS("command.reports-remove.success", Color.GREEN, "Removed report with index `{index}`"),
	
	COMMAND_PREMIUM_ADDKEY_ADDED("command.premium.addkey.added", Color.GREEN, "Successfully added key `{key}` to your account"),
	COMMAND_PREMIUM_ADDKEY_KEY_ALREADY_REDEEMED("command.premium.addkey.key-already-redeemed", Color.RED, "Key already redeemed"),
	COMMAND_PREMIUM_ADDKEY_INVALID_KEY("command.premium.addkey.invalid-key", Color.RED, "That's a very nice key but it's too bad that I don't seem to know about it"),

	COMMAND_PREMIUM_REDEEM_INVALID_KEY("command.premium.redeem.invalid-key", Color.RED, "That's a very nice key but it's too bad that I don't seem to know about it"),
	COMMAND_PREMIUM_REDEEM_KEY_MATCH("command.premium.redeem.key-match", Color.RED, "You already redeemed a key with this premium level"),
	COMMAND_PREMIUM_REDEEM_KEY_IN_USE("command.premium.redeem.key-in-use", Color.RED, "That key is already in use"),
	COMMAND_PREMIUM_REDEEM_KEY_REDEEMED("command.premium.redeem.key-redeemed", Color.GREEN, "Successfully redeemed key for: `{level}`"),
	
	COMMAND_KEYS_NO_KEYS("command.keys.no-keys", Color.RED, "Currently you don't have any keys"),
	
	COMMAND_ACCESSROLE_ADDED_ACCESSIBLE_ROLE("command.accessrole.added-accessible-role", Color.GREEN, "Made the role {role} accessible to {everyone}"),
	COMMAND_ACCESSROLE_ALREADY_ACCESSIBLE("command.accessrole.already-accessible", Color.RED, "Role already accessible to other weird people"),
	COMMAND_ACCESSROLE_REMOVED_ACCESSIBLE_ROLE("command.accessrole.removed-accessible-role", Color.GREEN, "Removed the role {role} from the list of accessible roles"),
	COMMAND_ACCESSROLE_ALREADY_REMOVED("command.accessrole.already-removed", Color.RED, "There is no accessrole like this on my list"),
	COMMAND_ACCESSROLE_NO_ACCESSROLES("command.accessrole.no-accessroles", Color.RED, "Currently there are no accessible roles"),
	
	COMMAND_MODROLE_ADDED("command.modrole.added", Color.GREEN, "Successfully added {role} as a moderator role"),
	COMMAND_MODROLE_REMOVED("command.modrole.removed", Color.GREEN, "Removed {role} from the moderator roles"),
	COMMAND_MODROLE_NO_ROLES("command.modrole.no-roles", Color.RED, "There are no more roles to remove"),
	COMMAND_MODROLE_ALREADY_ADDED("command.modrole.already-added", Color.RED, "Already added {role} as moderator role"),
	COMMAND_MODROLE_NOT_LISTED("command.modrole.not-listed", Color.RED, "That role is currently not listed as a moderator role"),
	COMMAND_MODROLE_LIST_NO_ROLES("command.modrole.list.no-roles", Color.GREEN, "There currently aren't any moderator roles"),
	
	COMMAND_SUPPORT_QUEUE_MESSAGE("command.support.queue.message", Color.GREEN, "Set the support queue to `{channel}`"),
	COMMAND_SUPPORT_UNSETQUEUE_MESSAGE("command.support.unsetqueue.message", Color.GREEN, "Removed the support queue"),
	
	COMMAND_GETROLE_ROLE_NOT_ACCESSIBLE("command.getrole.role-not-accessible", Color.RED, "Role is not accessible!"),
	COMMAND_GETROLE_SUCCESS("command.getrole.success", Color.GREEN, "I've given you the {role} role"),
	
	COMMAND_AUTOROLE_ADDED_AUTOROLE("command.autorole.added-autorole", Color.GREEN, "Added the role {role} as autorole"),
	COMMAND_AUTOROLE_ALREADY_ADDED("command.autorole.already-added", Color.RED, "Role is already on the list of autoroles"),
	COMMAND_AUTOROLE_REMOVED_AUTOROLE("command.autorole.removed-autorole", Color.GREEN, "Removed the role {role} from the list of autoroles"),
	COMMAND_AUTOROLE_ALREADY_REMOVED("command.autorole.already-removed", Color.RED, "It seems that this role is not on the list"),
	COMMAND_AUTOROLE_NO_AUTOROLES("command.autorole.no-autoroles", Color.RED, "Currently there are no autoroles"),
	
	COMMAND_BOTROLE_ADDED_BOTROLE("command.botrole.added-botrole", Color.GREEN, "Added the role {role} as botrole"),
	COMMAND_BOTROLE_ALREADY_ADDED("command.botrole.already-added", Color.RED, "Role is already on the list of botroles"),
	COMMAND_BOTROLE_REMOVED_BOTROLE("command.botrole.removed-botrole", Color.GREEN, "Removed the role {role} from the list of botroles"),
	COMMAND_BOTROLE_ALREADY_REMOVED("command.botrole.already-removed", Color.RED, "It seems that this role is not on the list"),
	COMMAND_BOTROLE_NO_BOTROLES("command.botrole.no-botroles", Color.RED, "Currently there are no botroles"),
	
	COMMAND_USERCHANNEL_CREATED("command.userchannel.created", Color.GREEN, "You have created your own user channel. By default your have all permissions of this channel :thinking:"),
	COMMAND_USERCHANNEL_DELETED("command.userchannel.deleted", Color.GREEN, "You have deleted your user channel"),
	COMMAND_USERCHANNEL_ONE_PER_MEMBER("command.userchannel.one-per-member", Color.RED, "You can only have one userchannel"),
	COMMAND_USERCHANNEL_DOESNT_EXIST("command.userchannel.doesnt-exist", Color.RED, "You don't have a user channel yet"),
	
	COMMAND_MUSIC_PLAY_CANNOT_PLAY("command.music.play.cannot-play", Color.RED, "Cannot play song: `{error_message}`"),
	COMMAND_MUSIC_NOT_PLAYING("command.music.not-playing", Color.RED, "Nothing is currently playing"),
	COMMAND_MUSIC_SET_ENDLESS_TRUE("command.music.set-endless-true", Color.GREEN, "Enabled endless mode"),
	COMMAND_MUSIC_SET_ENDLESS_FALSE("command.music.set-endless-false", Color.GREEN, "Disabled endless mode"),
	COMMAND_MUSIC_PAUSE_MESSAGE("command.music.pause.message", Color.GRAY, "Track paused, use `{prefix}music resume` to resume the paused track."),
	COMMAND_MUSIC_UNLOOP_MESSAGE("command.music.unloop.message", Color.GRAY, "Unlooped the current track"),
	COMMAND_MUSIC_VOLUME_MESSAGE("command.music.volume.message", Color.GRAY, "Set volume to {volume}"),
	COMMAND_MUSIC_RESUME_MESSAGE("command.music.resume.message", Color.GRAY, "Resumed the current paused track"),
	COMMAND_MUSIC_SHUFFLE_MESSAGE("command.music.shuffle.message", Color.GRAY, "Shook the queue"),
	COMMAND_MUSIC_SKIP_MESSAGE("command.music.skip.message", Color.GRAY, "Skipped `{amount}` track(s)"),
	COMMAND_MUSIC_EMPTY_QUEUE("command.music.empty-queue", Color.GRAY, "There are currently no tracks in the queue"),
	COMMAND_MUSIC_REMOVE_REMOVED("command.music.remove.removed", Color.GRAY, "Removed `{track}` from the queue"),
	COMMAND_MUSIC_REMOVE_INVALID_INDEX("command.music.remove.invalid-index", Color.GRAY, "Invalid index, index needs to be a number between `{min_index}` and `{max_index}`"),
	COMMAND_MUSIC_QUEUE_INVALID_PAGE("command.music.queue.invalid-page", Color.RED, "Invalid page. Maximum page is `{max_page}`"),
	COMMAND_MUSIC_QUEUE_MESSAGE("command.music.queue.message", Color.GRAY, "**[+]---[Current Queue: {tracks} track(s) | Page {page} / {max_page}]---[+]**\n\n{queue}"),
	COMMAND_MUSIC_SKIP_INVALID_AMOUNT("command.music.skip.invalid-amount", Color.RED, "Invalid amount"),
	COMMAND_MUSIC_VOLUME_INVALID_VOLUME("command.music.volume.invalid-volume", Color.GRAY, "Invalid volume. The volume must be in the range from 0 to 100"),
	COMMAND_MUSIC_PLAYLIST_INVALID_PLAYLIST("command.music.playlist.invalid-playlist", Color.GRAY, "Can't find a playlist with the given id"),
	COMMAND_MUSIC_PLAYLIST_DUPLICATE_NAME("command.music.playlist.duplicate-name", Color.RED, "A playlist with that name already exists"),
	COMMAND_MUSIC_PLAYLIST_RENAME_MESSAGE("command.music.playlist.rename.message", Color.GRAY, "Renamed playlist `{name}` to `{new_name}`"),
	COMMAND_MUSIC_PLAYLIST_RENAME_INVALID_NAME("command.music.playlist.rename.invalid-name", Color.RED, "The playlist name may only contain alphanumerical symbols as well as `-`, `_` or ` `"),
	COMMAND_MUSIC_PLAYLISTS_NO_PLAYLISTS("command.music.playlists.no-playlists", Color.GRAY, "You don't have any playlists :cry:"),
	COMMAND_MUSIC_PLAYLIST_DELETE_MESSAGE("command.music.playlist.delete.message", Color.GRAY, "Deleted the playlist with the name `{name}`"),
	COMMAND_MUSIC_PLAYLIST_SAVE_MESSAGE("command.music.playlist.save.message", Color.GRAY, "Saved your playlist with the name `{name}`"),
	COMMAND_MUSIC_PLAYLIST_SAVE_NO_SAVEABLE_TRACKS("command.music.playlist.save.no-saveable-tracks", Color.RED, "This playlist can't be saved because none of the tracks in the queue can be saved"),
	COMMAND_MUSIC_PLAYLIST_SAVE_UNSAVEABLE_TRACKS("command.music.playlist.save.unsaveable-tracks", Color.YELLOW, "Some tracks of the current queue might be missing in the playlist, because they cannot be saved (saved {saved} / {total})"),
	COMMAND_MUSIC_PLAYLIST_INFO_MESSAGE("command.music.playlist.info.message", Color.GRAY, "Tracks in playlist `{playlist}`:```\n{tracks}\n```"),
	COMMAND_MUSIC_JUMP_MESSAGE("command.music.jump.message", Color.GRAY, "Jumped around the world and to the track number {index}"),
	COMMAND_MUSIC_JUMP_INVALID_VALUE("command.music.jump.invalid-value", Color.GRAY, "Please enter a valid value"),
	COMMAND_MUSIC_PLAY_TRACK_NOT_FOUND("command.music.play.track-not-found", Color.GRAY, "Track not found"),
	COMMAND_MUSIC_SEEK_MESSAGE("command.music.seek.message", Color.GRAY, "Sought track to the specified position"),
	COMMAND_MUSIC_SEEK_CANNOT_SEEK("command.music.seek.cannot-seek", Color.GRAY, "Cannot seek to the specified position"),
	COMMAND_MUSIC_FASTFORWARD_MESSAGE("command.music.fastforward.message", Color.GRAY, "Fast-forwarded the track"),
	COMMAND_MUSIC_FASTFORWARD_CANNOT_FAST_FORWARD("command.music.fastforward.cannot-fast-forward", Color.GRAY, "Cannot fast-forward to the specified position"),
	COMMAND_MUSIC_REWIND_MESSAGE("command.music.rewind.message", Color.GRAY, "Rewound the track"),
	COMMAND_MUSIC_REWIND_CANNOT_REWIND("command.music.rewind.cannot-rewind", Color.GRAY, "Cannot rewind to the specified position"),
	COMMAND_MUSIC_LYRICS_NO_RESULTS("command.music.lyrics.no-results", Color.RED, "Couldn't find any lyrics for the current track"),
	COMMAND_MUSIC_BASSBOOST_INVALID_LEVEL("command.music.bassboost.invalid-level", Color.RED, "The level needs to be a number between 0 and {max_level}"),
	COMMAND_MUSIC_BASSBOOST_ENABLED("command.music.bassboost.enabled", Color.GREEN, "Pain multiplier x{level}"),
	COMMAND_MUSIC_BASSBOOST_DISABLED("command.music.bassboost.disabled", Color.GREEN, "Bass boost disabled"),
	COMMAND_MUSIC_SPEED_INVALID_SPEED("command.music.speed.invalid-speed", Color.RED, "The speed needs to be a value between {min_speed} and {max_speed}"),
	COMMAND_MUSIC_SPEED_MESSAGE("command.music.speed.message", Color.GREEN, "Speed set to {speed}x"),
	COMMAND_MUSIC_PITCH_INVALID_PITCH("command.music.pitch.invalid-pitch", Color.RED, "The pitch needs to be a value between {min_pitch} and {max_pitch}"),
	COMMAND_MUSIC_PITCH_MESSAGE("command.music.pitch.message", Color.GREEN, "Pitch set to {pitch}x"),
	COMMAND_MUSIC_NIGHTCORE_MESSAGE("command.music.nightcore.message", Color.GREEN, "Enabled nightcore mode \uFF3C(\u2267\u25BD\u2266)\uFF0F"),
	COMMAND_MUSIC_RESET_MESSAGE("command.music.reset.message", Color.GREEN, "Reset all playback settings to their default values"),
	
	COMMAND_MEME_AVAILABLE("command.meme.available", Color.GREEN, "Available meme categories: `{meme_categories}`"),
	
	COMMAND_LOCALE_INVALID_LOCALE("command.locale.invalid-locale", Color.RED, "Invalid locale!"),
	COMMAND_LOCALE_SET_MESSAGE("command.locale.set.message", Color.GREEN, "Set locale to `{locale}`"),
	
	COMMAND_DONATORS_EMPTY("command.donators.empty", Color.RED, "Currently there are no donators. You wan't to support our work? (Patreon)[{patreon}]"),
	
	COMMAND_PREMIUM_LEVEL_MESSAGE("command.premium.level.message", Color.GRAY, "Premium Level: `{level}`"),

	COMMAND_LOCALE_DELETE_SUCCESS("command.locale.delete.success", Color.GREEN, "Successfully deleted locale `{locale}`"),
	COMMAND_LOCALE_DELETE_INVALID_LOCALE("command.locale.delete.invalid-locale", Color.RED, "The locale you're trying to delete doesn't exist or is a default locale"),
	COMMAND_LOCALE_UPLOAD_SUCCESS("command.locale.upload.success", Color.GREEN, "The locale has been uploaded to your server's locales. You can select it using `{prefix}locale set {locale}`"),
	COMMAND_LOCALE_UPLOAD_INVALID_FILE("command.locale.upload.invalid-file", Color.RED, "Something went wrong... You tried to upload a file that is broken"),
	COMMAND_LOCALE_UPLOAD_INVALID_SHORT("command.locale.upload.invalid-short", Color.RED, "Your locale short doesn't match our requirements (up to 16 alphanumeric characters, underscores allowed)"),
	
	COMMAND_MODULE_INVALID_MODULE("command.module.invalid-module", Color.RED, "Invalid module name. Valid names are `{modules}`"),
	COMMAND_MODULE_ENABLE_ALREADY_ENABLED("command.module.enable.already-enabled", Color.RED, "That module is already enabled"),
	COMMAND_MODULE_ENABLE_SUCCESS("command.module.enable.success", Color.GREEN, "Successfully enabled the module `{module}`"),
	
	COMMAND_MODULE_DISABLE_NOT_ENABLED("command.module.disable.not-enabled", Color.RED, "That module is not enabled"),
	COMMAND_MODULE_DISABLE_SUCCESS("command.module.disable.success", Color.GREEN, "Successfully disabled the module `{module}`"),

	COMMAND_MODULE_LIST_MESSAGE("command.module.list.message", Color.GRAY, "Available modules: ```\n{modules}\n```"),
	
	COMMAND_MINIGAME_PLAY_GAME_STARTED("command.minigame.play.game-started", Color.GREEN, "You started the game `{minigame}`. {multiplayer}"),
	COMMAND_MINIGAME_JOIN_SHARED_GAME_STARTED("command.minigame.join.shared-game-started", Color.GREEN, "You joined the game `{minigame}`"),
	COMMAND_MINIGAME_REMATCH_INVITED("command.minigame.rematch-invited", Color.GREEN, "Successfully invited `{user}` to a rematch in `{minigame}`. (Use `minigame leave` to leave)"),
	COMMAND_MINIGAME_PLAY_INVALID_GAME("command.minigame.play.invalid-game", Color.RED, "Invalid minigame. Valid options: `{minigames}`"),
	COMMAND_MINIGAME_INVITE_SELF("command.minigame.invite.self", Color.RED, "You can't invite yourself"),
	COMMAND_MINIGAME_INVITE_BOTS("command.minigame.invite.bots", Color.RED, "You can't invite bots"),
	COMMAND_MINIGAME_INVITE_NOT_MULTIPLAYER("command.minigame.invite.not-multiplayer", Color.RED, "The game you're currently playing is not a multiplayer game"),
	COMMAND_MINIGAME_INVITE_MESSAGE("command.minigame.invite.message", Color.GREEN, "`{inviter}` invited you to play `{minigame}` with them.\nAccept the invitation?"),
	COMMAND_MINIGAME_INVITE_SUCCESS("command.minigame.invite.success", Color.GREEN, "Successfully invited {user} to play `{minigame}` with you"),
	COMMAND_MINIGAME_INVITE_FAILED("command.minigame.invite.failed", Color.RED, "Can't invite {user} cause the user have disabled DM's"),
	COMMAND_MINIGAME_PLAY_ALREADY_PLAYING("command.minigame.play.already-playing", Color.RED, "You are already playing another game. Use `{prefix}minigame leave` to leave it"),
	COMMAND_MINIGAME_NOT_PLAYING("command.minigame.not-playing", Color.RED, "You aren't currently playing a game"),
	COMMAND_MINIGAME_LEAVE_SUCCESS("command.minigame.leave.success", Color.GREEN, "Successfully left your game"),
	COMMAND_MINIGAME_LIST_MESSAGE("command.minigame.list.message", Color.GREEN, "Playable minigames: `{minigames}`"),
	COMMAND_MINIGAME_LEAVE_MULTIPLAYER_AUTOLEAVE("command.minigame.leave.multiplayer-autoleave", Color.RED, "{user} left the game. All players were kicked automatically"),
	COMMAND_MINIGAME_CANT_JOIN("command.minigame.cant-join", Color.RED, "You can't join because the game is full, running or stopped"),
	COMMAND_MINIGAME_SHARE_GLOBAL_GAME("command.minigame.share.global-game", Color.RED, "You can't share a global minigame"),
	COMMAND_MINIGAME_SHARE_SHARED("command.minigame.share.shared", Color.GREEN, "The minigame was made public. Waiting for other players..."),
	COMMAND_MINIGAME_SHARE_CANT_SHARE("command.minigame.share.cant-share", Color.RED, "You can't share this minigame because the game is full or already running"),
	COMMAND_MINIGAME_JOIN_NO_GAMES("command.minigame.join.no-games", Color.RED, "There are no minigames that you can join"),
	COMMAND_MINIGAME_JOIN_NOT_MULTIPLAYER("command.minigame.join.not-multiplayer", Color.RED, "The game you're trying to join is not a multiplayer game"),
	COMMAND_MINIGAME_STOPPED("command.minigame.stopped", Color.RED, "You were kicked from the minigame because it has been idle for too long"),

	COMMAND_SCRIPT_UPLOAD_SUCCESS("command.script.upload.success", Color.GREEN, "Uploaded script to your scripts folder"),
	COMMAND_SCRIPT_UPLOAD_INVALID_FILE("command.script.upload.invalid-file", Color.RED, "Failed to load script:\n```\n{error}```"),
	COMMAND_SCRIPT_UPLOAD_INVALID_SHORT("command.script.upload.invalid-short", Color.RED, "Your script name doesn't match our requirements (up to 16 alphanumeric characters, underscores and dashes allowed)"),
	COMMAND_SCRIPT_UPLOAD_TOO_LARGE("command.script.upload.too-large", Color.RED, "The script can not be larger than 8 KiB"),
	COMMAND_SCRIPT_LIST_MESSAGE("command.script.list.message", Color.GREEN, "Your scripts: `{scripts}`"),
	COMMAND_SCRIPT_DELETE_INVALID_SCRIPT("command.script.delete.invalid-script", Color.RED, "Script doesn't exist"),
	COMMAND_SCRIPT_DELETE_SUCCESS("command.script.delete.success", Color.GREEN, "Successfully deleted script"),

	COMMAND_AUTOCHANNEL_AUTOCHANNEL_ERROR("command.autochannel.autochannel-error", Color.RED, "You can't create an autochannel of an autochannel!"),
	COMMAND_AUTOCHANNEL_USERCHANNEL_ERROR("command.autochannel.userchannel-error", Color.RED, "You can't create an autochannel of an userchannel!"),
	COMMAND_AUTOCHANNEL_ENABLED("command.autochannel.enabled", Color.GREEN, "Graphite will now automatically duplicate `{channel_name}` when someone joins"),
	COMMAND_AUTOCHANNEL_DISABLED("command.autochannel.disabled", Color.GREEN, "`{channel_name}` is now no longer an AutoChannel"),
	
	COMMAND_EMOTEINFO_UNICODE("command.emoteinfo.unicode", Color.GRAY, "EMOTE INFORMATION\n\nEscaped Java: `{escaped_java}`\nEscaped HTML: `{escaped_html}`"),
	COMMAND_EMOTEINFO_EMOTE("command.emoteinfo.emote", Color.GRAY, "Discord emote id: `{id}`\nRaw Discord emote: `{raw}`"),
	
	COMMAND_CATEGORYCOPY_SUCCESS("command.categorycopy.success", Color.GREEN, "Copied all permissions from `{from_category}` to `{to_category}`"),
	
	COMMAND_RECORD_NOT_IN_AUDIOCHANNEL("command.record.not-in-audiochannel", Color.RED, "You are currently not in an audio channel"),
	COMMAND_RECORD_ALREADY_RECORDING("command.record.already-recording", Color.RED, "You already recording"),
	COMMAND_RECORD_NOT_RECORDING("command.record.not-recording", Color.RED, "Oops should I record? Sorry but you don't start the recording"),
	COMMAND_RECORD_RECORDING("command.record.recording", Color.RED, "Started recording in `{voice_channel}`. Record started by {user}"),
	COMMAND_RECORD_STOPPED_RECORDING("command.record.stopped-recording", Color.ORANGE, "Stopped recording. Recording name: `{name}`. You can download it via our [webinterface]({webinterface})"),
	COMMAND_RECORD_INVALID_RECORDING("command.record.invalid-recording", Color.RED, "Can't find a recording with the given name"),
	COMMAND_RECORD_RECORDING_TOO_LARGE("command.record.recording-too-large", Color.RED, "The recording file is too large for Discord but you can download it via our [webinterface]({webinterface})"),
	COMMAND_RECORD_DELETED_RECORDING("command.record.deleted-recording", Color.GREEN, "Recording `{id}` deleted"),
	COMMAND_RECORD_RENAME_ALREADY_EXISTS("command.record.rename.already-exists", Color.RED, "A recording with the name already exists"),
	COMMAND_RECORD_RENAME_MESSAGE("command.record.rename.message", Color.GRAY, "Renamed recording `{name}` to `{new_name}`"),
	COMMAND_RECORD_RENAME_INVALID_NAME("command.record.rename.invalid-name", Color.RED, "The recording name may only contain alphanumerical symbols as well as `-`, `_` or ` `"),
	
	COMMAND_MONEY_NO_MONEY("command.money.no-money", Color.RED, "I don't think it's a good idea to talk about the amount of money you have (you're broke)"),
	COMMAND_MONEY_MONEY("command.money.money", Color.GREEN, "You currently have `{amount}`{emote_dollaronen}"),
	
	COMMAND_GREETING_MESSAGE_SET("command.greeting.message-set", Color.GREEN, "New greeting message: `{message}`"),
	COMMAND_GREETING_CURRENT_MESSAGE("command.greeting.current-message", Color.GREEN, "Current greeting message: `{message}`"),
	COMMAND_GREETING_ALREADY_ENABLED("command.greeting.already-enabled", Color.RED, "Greeting message already enabled"),
	COMMAND_GREETING_ENABLED("command.greeting.enabled", Color.GREEN, "Greeting message enabled"),
	COMMAND_GREETING_ALREADY_DISABLED("command.greeting.already-disabled", Color.RED, "Greeting message already disabled"),
	COMMAND_GREETING_DISABLED("command.greeting.disabled", Color.RED, "Greeting message disabled"),
	COMMAND_GREETING_CHANNEL_SET("command.greeting.channel-set", Color.GREEN, "Greeting channel set to: {channel}"),
	
	COMMAND_FAREWELL_MESSAGE_SET("command.farewell.message-set", Color.GREEN, "New farewell message: `{message}`"),
	COMMAND_FAREWELL_CURRENT_MESSAGE("command.farewell.current-message", Color.GREEN, "Current farewell message: `{message}`"),
	COMMAND_FAREWELL_ALREADY_ENABLED("command.farewell.already-enabled", Color.RED, "Farewell message already enabled"),
	COMMAND_FAREWELL_ENABLED("command.farewell.enabled", Color.GREEN, "Farewell message enabled"),
	COMMAND_FAREWELL_ALREADY_DISABLED("command.farewell.already-disabled", Color.RED, "Farewell message already disabled"),
	COMMAND_FAREWELL_DISABLED("command.farewell.disabled", Color.RED, "Farewell message disabled"),
	COMMAND_FAREWELL_CHANNEL_SET("command.farewell.channel-set", Color.GREEN, "Farewell channel set to: {channel}"),
	
	COMMAND_COINFLIP_SIDE("command.coinflip.side", Color.GREEN, "The coin landed on its side (That's a 1 in 6000 chance for an American nickel!)"),
	
	COMMAND_AMONGUS_CREATE_MESSAGE("command.amongus.create.message", Color.GREEN, "Check your DMs"),
	COMMAND_AMONGUS_CREATE_ALREADY_EXISTS("command.amongus.create.already-exists", Color.RED, "There's already an Among Us round running in your channel"),
	COMMAND_AMONGUS_CREATE_NOT_IN_VOICECHANNEL("command.amongus.create.not-in-voicechannel", Color.RED, "You need to be in a voice channel to create an Among Us game"),
	COMMAND_AMONGUS_NOT_IN_VALID_VOICECHANNEL("command.amongus.not-in-valid-voicechannel", Color.RED, "You need to be in a voice channel with an active Among Us game"),
	COMMAND_AMONGUS_STOP_NOT_CAPTURE_USER("command.amongus.stop.not-capture-user", Color.RED, "You aren't the creator of this Among Us game"),
	COMMAND_AMONGUS_LINK_INVALID_COLOR_OR_PLAYER("command.amongus.link.invalid-color-or-player", Color.RED, "Invalid color/player"),
	COMMAND_AMONGUS_LINK_NOT_A_MEMBER("command.amongus.link.not-a-member", Color.RED, "Not a member of this server"),
	COMMAND_AMONGUS_LINK_MEMBER_NOT_IN_VOICECHANNEL("command.amongus.link.member-not-in-voicechannel", Color.RED, "That member is not in the voice channel of the Among Us game"),
	
	COMMAND_TEXTCOMMANDS_ENABLE_MESSAGE("command.textcommands.enable.message", Color.GREEN, "Enabled text-based commands (Prefix: `{prefix}`)"),
	COMMAND_TEXTCOMMANDS_DISABLE_MESSAGE("command.textcommands.disable.message", Color.RED, "Disabled text-based commands"),
	
	MINIGAME_WON("minigame.won", Color.GREEN, "You won and earned `{money}`{emote_dollaronen} :tada:"),
	MINIGAME_LOST("minigame.lost", Color.RED, "You lost :cry:"),
	MINIGAME_TIED("minigame.tied", Color.ORANGE, "It's a tie :cry:"),
	MINIGAME_REMATCH_INVITE_DECLINED("minigame.rematch-invite-declined", Color.RED, "{user} has declined your rematch invitation"),
	MINIGAME_REPLAY("minigame.replay", Color.YELLOW, "Do you want to play again?"),
	
	MINIGAME_MINESWEEPER_HELP("minigame.minesweeper.help", Color.WHITE, "You have two coordinates: x and y. The first emote that you click is the x coordinate and the second emote is the y coordinate. If you hit a bomb the game is over."),
	MINIGAME_BATTLESHIPS_HELP("minigame.battleships.help", Color.WHITE, "Initially, you need to place your ships by first selecting the x (left-right)-coordinate, then the y (up-down)-coordinate, followed by the direction (right/down arrow).\nOnce both players have finished placing their ships, player one will start shooting, also by selecting the x and y coordinates, then it's player 2's turn, then player 1 again etc.\nThe top field provides information about the other player's field, the bottom one is your field. Once a ship is sunk, it will be displayed as :boom: emotes"),
	MINIGAME_RPG_HELP("minigame.rpg.help", Color.WHITE, "RPG is a text-based game that you can play by typing actions into the chat. Try using `help` to see what you can do"),
	
	MINIGAME_BLACKJACK_FIRST_HAND_WON("minigame.blackjack.first-hand.won", Color.GREEN, "You won `{money}`{emote_dollaronen} with your first hand"),
	MINIGAME_BLACKJACK_FIRST_HAND_LOST("minigame.blackjack.first-hand.lost", Color.RED, "You lost `{money}`{emote_dollaronen} with your first hand"),
	MINIGAME_BLACKJACK_FIRST_HAND_TIED("minigame.blackjack.first-hand.tied", Color.YELLOW, "Your first hand tied and you got your `{money}`{emote_dollaronen} back"),
	MINIGAME_BLACKJACK_SECOND_HAND_WON("minigame.blackjack.second-hand.won", Color.GREEN, "You won `{money}`{emote_dollaronen} with your second hand"),
	MINIGAME_BLACKJACK_SECOND_HAND_LOST("minigame.blackjack.second-hand.lost", Color.RED, "You lost `{money}`{emote_dollaronen} with your second hand"),
	MINIGAME_BLACKJACK_SECOND_HAND_TIED("minigame.blackjack.second-hand.tied", Color.YELLOW, "Your second hand tied and you got your `{money}`{emote_dollaronen} back"),
	MINIGAME_BLACKJACK_BET_LIMIT("minigame.blackjack.bet-limit", Color.RED, "Your bet has to be between `{min_bet}` and `{max_bet}`{emote_dollaronen}"),
	MINIGAME_BLACKJACK_NOT_ENOUGH_MONEY("minigame.blackjack.not-enough-money", Color.RED, "You don't have enough money"),
	
	PATREON_PLEDGE_ADDED("patreon.pledge-added", Color.GREEN, "Thanks for your patreon pledge. You have bought the `{rank}` rank for `{price} {currency}`. You get `{keys}` extra keys for your friends. Share the power of Graphite 2.0. :hearts:"),
	PATREON_PLEDGE_REMOVED("patreon.pledge-removed", Color.RED, "We're sad to see you removing your pledge from patreon. If you have any feedback for the bot, feel free to submit it on [our Discord]({discord_url})"),
	PATREON_PLEDGE_CHANGED("patreon.pledge-changed", Color.ORANGE, "You have changed your patreon pledge from `{old_pledge}` to `{new_pledge}`. New price: `{price} {currency}`. Your extra keys: `{keys}` type `{prefix}premium keys` to show a list of all your keys."),
	
	CUSTOMCOMMAND_INVALID_ARG_TYPE("customcommand.invalid-arg-type", Color.RED, "Invalid argument type for argument `{arg}`, should be of type `{type}`"),

	ERROR_NO_PERMISSION("error.no-permission", Color.RED, "{emote_error} You lack the required permission `{permission}`"),
	ERROR_COMMAND_SERVER_ONLY("error.command-server-only", Color.RED, "{emote_error} You can only use that command on servers"),
	ERROR_COMMAND_PRIVATE_ONLY("error.command-private-only", Color.RED, "{emote_error} You can only use that command by PMing the bot"),
	ERROR_INVALID_MENTION("error.invalid-mention", Color.RED, "{emote_error} Invalid mention.\nTake a look at the [documentation]({documentation_url}) to see how to format mentions"),
	ERROR_AMBIGUOUS_MENTION("error.ambiguous-mention", Color.RED, "{emote_error} That mention is ambiguous. Make sure to specify a unique entity.\nTake a look at the [documentation]({documentation_url}) to see how to format mentions"),
	ERROR_CANT_INTERACT_MEMBER("error.cant-interact-member", Color.RED, "{emote_error} Seems like I don't have permission to interact with that member... :cry:"),
	ERROR_CANT_INTERACT_ROLE("error.cant-interact-role", Color.RED, "{emote_error} Seems like I don't have permission to interact with that role... :cry:"),
	ERROR_NOT_IN_VOICECHANNEL("error.not-in-voicechannel", Color.RED, "{emote_error} For this action the specified user must be in a voice channel :cry:"),
	ERROR_NOT_IN_AUDIOCHANNEL("error.not-in-audiochannel", Color.RED, "{emote_error} For this action the specified user must be in an audio channel :cry:"),
	ERROR_NOT_A_MEMBER("error.not-a-member", Color.RED, "{emote_error} I can't seem to find that user here. Should I report them as missing?"),
	ERROR_INVALID_DURATION("error.invalid-duration", Color.RED, "{emote_error} Invalid duration. Format: `2h30m` or `1w2d1h10m`"),
	ERROR_ALLOWED_MENTION_TYPE_MESSAGE("error.allowed-mention-type.message", Color.RED, "{emote_error} Allowed mention type(s): {mention_types}"),
	ERROR_ALLOWED_MENTION_TYPE_VOICECHANNEL("error.allowed-mention-type.voicechannel", Color.RED, "`#!voiceChannel`"),
	ERROR_ALLOWED_MENTION_TYPE_AUDIOCHANNEL("error.allowed-mention-type.audiochannel", Color.RED, "`#!channel`"),
	ERROR_ALLOWED_MENTION_TYPE_CATEGORY("error.allowed-mention-type.category", Color.RED, "`:<category>`"),
	ERROR_ALLOWED_MENTION_TYPE_TEXTCHANNEL("error.allowed-mention-type.textchannel", Color.RED, "`#textchannel`"),
	ERROR_ALLOWED_MENTION_TYPE_EMOTE("error.allowed-mention-type.emote", Color.RED, "`:emote:`"),
	ERROR_ALLOWED_MENTION_TYPE_USER("error.allowed-mention-type.user", Color.RED, "`@User`"),
	ERROR_ALLOWED_MENTION_TYPE_ROLE("error.allowed-mention-type.role", Color.RED, "`@Role`"),
	ERROR_SERVER_BUSY("error.server-busy", Color.RED, ":sweat: Sorry, but i just can't keep up with you. Please wait until your previous action has finished :clock2:"),
	ERROR_INVALID_TIMESTAMP("error.invalid-timestamp", Color.RED, "{emote_error} Invalid timestamp format"),
	ERROR_GRAPHITE_ADDED("error.graphite-added", Color.RED, ":warning:\nYou've already added Graphite to your server, so some features of other Multiplex-Bots will be disabled.\nYou'll still be able to use the Multiplex bots for features that support it (e.g. Music)"),
	ERROR_EXCEPTION("error.exception", Color.RED, ":warning: An unexpected exception occured: `{error_message}`"),
	ERROR_LACKING_PERMISSION("error.lacking-permission", Color.RED, "{emote_error} I need the permission(s) `{permission}` to perform that action"),
	ERROR_CANT_WRITE("error.cant-write", Color.RED, "{emote_error} Sorry, but i can't write to that channel. Please grant the `Send Messages` permission to use commands"),
	ERROR_IS_BOT("error.is-bot", Color.RED, "{emote_error} You can't interact with bots"),
	ERROR_MINIMUM_TEMP_DURATION("error.minimum-temp-duration", Color.RED, "{emote_error} Please set a minimum duration of `1 minute` for a temporary action."),
	ERROR_ALREADY_MUTED("error.already-muted", Color.RED, "You have already muted this user"),
	ERROR_OUT_OF_BOUNDS("error.out-of-bounds", Color.RED, "Index out of bounds"),
	ERROR_BOT_CANNOT_WRITE("error.bot-cannot-write", Color.RED, "I don't seem to have permission to write in the cofigurated channel. The simplest way to unlock the full power of Graphite is to grant it `Administrator` permissions. Otherwise some features might be unusable."),
	ERROR_NO_SLASH_COMMAND_REPLY("error.no-slash-command-reply", Color.RED, "This slash command does not have a default reply. This should not be the case. Please report this as a bug on [our Discord server]({discord_url})"),
	ERROR_ARGUMENT_TYPE_BOOLEAN("error.argument-type.boolean", Color.RED, "The value for parameter `{parameter}` must be a valid boolean (true/false, or a configured Yes/No word)"),
	ERROR_ARGUMENT_TYPE_INTEGER("error.argument-type.integer", Color.RED, "The value for parameter `{parameter}` must be a valid integer"),
	ERROR_ARGUMENT_TYPE_NUMBER("error.argument-type.number", Color.RED, "The value for parameter `{parameter}` must be a valid number"),
	ERROR_CHOICE_INVALID("error.choice-invalid", Color.RED, "The value for parameter `{parameter}` must be one of the following: `{choices}`"),
	
	OTHER_FOUND_AN_EASTEREGG("other.found-an-easteregg", Color.GREEN, "You have found an easter egg. Added {money}{emote_dollaronen} to your account balance"),
	OTHER_JOINED_SUPPORT_QUEUE("other.joined-support-queue", Color.GRAY, "Hey, `{users}` needs support."),
	OTHER_HEAVY_BUSY("other.heavy-busy", Color.YELLOW, "Your action has been queued for execution. Depending on the current load, this might take up to a few minutes :clock2:. If you want a faster queue try donating on [Patreon]({patreon})"),
	OTHER_MULTIPLEX_DISABLED("other.multiplex-disabled", Color.YELLOW, "{emote_info}\nSince you've decided to add Graphite to your server, all other Multiplex bots have been disabled.\nDon't worry, all your settings have automatically been transferred to Graphite")
	;
	
	private final Color color;
	private final LocalizedString messageText;
	
	private DefaultMessage(String path, Color color, String fallback) {
		this.color = color;
		this.messageText = new LocalizedStringImpl(path, fallback);
	}

	@Override
	public Color getMessageColor() {
		return color;
	}

	@Override
	public LocalizedString getMessageText() {
		return messageText;
	}
	
	public static DefaultMessage getByPath(String path) {
		return Arrays.stream(values())
				.filter(v -> v.getMessagePath().equals(path))
				.findFirst().orElse(null);
	}
	
	public static String getMentionTypesString(GraphiteLocalizable localizable, MentionType... types) {
		return Arrays.stream(types)
			.map(t -> {
				switch(t) {
					case CATEGORY:
						return ERROR_ALLOWED_MENTION_TYPE_CATEGORY.getFor(localizable);
					case EMOTE:
						return ERROR_ALLOWED_MENTION_TYPE_EMOTE.getFor(localizable);
					case ROLE:
						return ERROR_ALLOWED_MENTION_TYPE_ROLE.getFor(localizable);
					case TEXT_CHANNEL:
						return ERROR_ALLOWED_MENTION_TYPE_TEXTCHANNEL.getFor(localizable);
					case USER:
						return ERROR_ALLOWED_MENTION_TYPE_USER.getFor(localizable);
					case VOICE_CHANNEL:
						return ERROR_ALLOWED_MENTION_TYPE_VOICECHANNEL.getFor(localizable);
					case EVERYONE:
					case HERE:
					default:
						throw new IllegalArgumentException("Mention type " + t + " has no message");
				}
			})
			.collect(Collectors.joining(", "));
	}
	
}
