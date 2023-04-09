package me.eglp.gv2.multiplex;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.commands.admin.CommandLocale;
import me.eglp.gv2.commands.admin.CommandModule;
import me.eglp.gv2.commands.admin.CommandPermission;
import me.eglp.gv2.commands.admin.CommandPrefix;
import me.eglp.gv2.commands.admin.CommandPurge;
import me.eglp.gv2.commands.admin.CommandTextCommands;
import me.eglp.gv2.commands.admin.CommandUpdateSlashCommands;
import me.eglp.gv2.commands.backups.CommandBackup;
import me.eglp.gv2.commands.channel_management.CommandAutoChannel;
import me.eglp.gv2.commands.channel_management.CommandCategoryCopy;
import me.eglp.gv2.commands.channel_management.CommandUserChannel;
import me.eglp.gv2.commands.fun.CommandAmongUs;
import me.eglp.gv2.commands.fun.CommandChuckNorris;
import me.eglp.gv2.commands.fun.CommandCoinFlip;
import me.eglp.gv2.commands.fun.CommandDice;
import me.eglp.gv2.commands.fun.CommandEightBall;
import me.eglp.gv2.commands.fun.CommandMeme;
import me.eglp.gv2.commands.fun.CommandMinigame;
import me.eglp.gv2.commands.fun.CommandPoll;
import me.eglp.gv2.commands.fun.CommandReminder;
import me.eglp.gv2.commands.fun.CommandTCRandom;
import me.eglp.gv2.commands.fun.CommandTTS;
import me.eglp.gv2.commands.fun.CommandVCRandom;
import me.eglp.gv2.commands.greeter.CommandFarewell;
import me.eglp.gv2.commands.greeter.CommandGreeting;
import me.eglp.gv2.commands.info.CommandAbout;
import me.eglp.gv2.commands.info.CommandChannelInfo;
import me.eglp.gv2.commands.info.CommandEasterEggs;
import me.eglp.gv2.commands.info.CommandEmoteInfo;
import me.eglp.gv2.commands.info.CommandFAQ;
import me.eglp.gv2.commands.info.CommandHelp;
import me.eglp.gv2.commands.info.CommandInvite;
import me.eglp.gv2.commands.info.CommandMoney;
import me.eglp.gv2.commands.info.CommandUpvote;
import me.eglp.gv2.commands.info.CommandUserInfo;
import me.eglp.gv2.commands.info.CommandWhatAreYouDoing;
import me.eglp.gv2.commands.moderation.CommandChatMute;
import me.eglp.gv2.commands.moderation.CommandChatReport;
import me.eglp.gv2.commands.moderation.CommandChatReports;
import me.eglp.gv2.commands.moderation.CommandChatUnmute;
import me.eglp.gv2.commands.moderation.CommandClear;
import me.eglp.gv2.commands.moderation.CommandClearAll;
import me.eglp.gv2.commands.moderation.CommandJail;
import me.eglp.gv2.commands.moderation.CommandModRole;
import me.eglp.gv2.commands.moderation.CommandReport;
import me.eglp.gv2.commands.moderation.CommandReports;
import me.eglp.gv2.commands.moderation.CommandSupport;
import me.eglp.gv2.commands.moderation.CommandTempBan;
import me.eglp.gv2.commands.moderation.CommandTempChatMute;
import me.eglp.gv2.commands.moderation.CommandTempJail;
import me.eglp.gv2.commands.moderation.CommandTempVoiceMute;
import me.eglp.gv2.commands.moderation.CommandUnban;
import me.eglp.gv2.commands.moderation.CommandUnjail;
import me.eglp.gv2.commands.moderation.CommandVoiceUnmute;
import me.eglp.gv2.commands.music.CommandMusic;
import me.eglp.gv2.commands.premium.CommandDonators;
import me.eglp.gv2.commands.premium.CommandKeys;
import me.eglp.gv2.commands.premium.CommandPremium;
import me.eglp.gv2.commands.record.CommandRecord;
import me.eglp.gv2.commands.reddit.CommandReddit;
import me.eglp.gv2.commands.role_management.CommandAccessrole;
import me.eglp.gv2.commands.role_management.CommandAutorole;
import me.eglp.gv2.commands.role_management.CommandBotrole;
import me.eglp.gv2.commands.role_management.CommandGetrole;
import me.eglp.gv2.commands.scripting.CommandScript;
import me.eglp.gv2.commands.test.CommandTest;
import me.eglp.gv2.commands.twitch.CommandTwitch;
import me.eglp.gv2.commands.twitter.CommandTwitter;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptEnum;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;

@JavaScriptEnum
@JavaScriptClass(name = "Feature")
public enum GraphiteFeature implements WebinterfaceObject, JSONPrimitiveStringConvertible {
	
	DEFAULT(
		null,
		new CommandHelp(),
		new CommandAbout(),
		new CommandPermission(),
		new CommandPremium(),
		new CommandInvite(),
		new CommandPrefix(),
		new CommandLocale(),
		new CommandKeys(),
		new CommandDonators(),
		new CommandUpvote(),
		new CommandWhatAreYouDoing(),
		new CommandPurge(),
		new CommandTest(),
		new CommandTextCommands(),
		new CommandUpdateSlashCommands()
	),
	BACKUPS(
		DefaultPermissions.WEBINTERFACE_BACKUPS,
		new CommandBackup()
	),
	FUN(
		null,
		new CommandEightBall(),
		new CommandChuckNorris(),
		new CommandMinigame(),
		new CommandMeme(),
		new CommandCoinFlip(),
		new CommandDice(),
		new CommandAmongUs(),
		new CommandVCRandom(),
		new CommandTCRandom(),
		new CommandPoll(),
		new CommandTTS(),
		new CommandReminder()
	),
	MUSIC(
		DefaultPermissions.WEBINTERFACE_MUSIC,
		new CommandMusic()
	),
	MODERATION(
		DefaultPermissions.WEBINTERFACE_MODERATION,
		new CommandChatReports(),
		new CommandChatReport(),
		new CommandTempVoiceMute(),
		new CommandClearAll(),
		new CommandTempJail(),
		new CommandTempBan(),
		new CommandSupport(),
		new CommandReports(),
		new CommandUnjail(),
		new CommandVoiceUnmute(),
		new CommandUnban(),
		new CommandReport(),
		new CommandClear(),
		new CommandJail(),
		new CommandCategoryCopy(),
		new CommandModRole(),
		new CommandChatMute(),
		new CommandChatUnmute(),
		new CommandTempChatMute()
	),
	GREETER(
		DefaultPermissions.WEBINTERFACE_GREETER,
		new CommandFarewell(),
		new CommandGreeting()
	),
	ROLE_MANAGEMENT(
		DefaultPermissions.WEBINTERFACE_ROLE_MANAGEMENT,
		new CommandAccessrole(),
		new CommandAutorole(),
		new CommandGetrole(),
		new CommandBotrole()
	),
	CHANNEL_MANAGEMENT(
		DefaultPermissions.WEBINTERFACE_CHANNEL_MANAGEMENT,
		new CommandAutoChannel(),
		new CommandUserChannel()
	),
	UTILITIES(
		null,
		new CommandChannelInfo(),
		new CommandFAQ(),
		new CommandUserInfo(),
		new CommandEmoteInfo(),
		new CommandMoney(),
		new CommandEasterEggs()
	),
	RECORD(
		DefaultPermissions.WEBINTERFACE_RECORD,
		new CommandRecord()
	),
	TWITCH(
		DefaultPermissions.WEBINTERFACE_TWITCH,
		new CommandTwitch()
	),
	TWITTER(
		DefaultPermissions.WEBINTERFACE_TWITTER,
		new CommandTwitter()
	),
	REDDIT(
		DefaultPermissions.WEBINTERFACE_REDDIT,
		new CommandReddit()
	),
	SCRIPTING(
		DefaultPermissions.WEBINTERFACE_SCRIPTING,
		new CommandScript()
	),
	MODULES(
		null,
		new CommandModule()	
	),
	CUSTOM_COMMANDS(DefaultPermissions.WEBINTERFACE_CUSTOM_COMMANDS),
	STATISTICS(DefaultPermissions.WEBINTERFACE_STATISTICS),
	;
	
	private String webinterfacePermission;
	private final List<Command> commands;
	
	private GraphiteFeature(String webinterfacePermission, Command... commands) {
		this.webinterfacePermission = webinterfacePermission;
		this.commands = Arrays.asList(commands);
	}
	
	public String getWebinterfacePermission() {
		return webinterfacePermission;
	}
	
	public List<Command> getCommands() {
		return commands;
	}

	@Override
	public String toJSONPrimitive() {
		return name();
	}
	
	public static GraphiteFeature decodePrimitive(Object value) {
		return valueOf((String) value);
	}

	@JavaScriptFunction(calling = "getAvailableFeatures", returning = "available_features", withGuild = true)
	public static void getAvailableFeatures() {};

	@JavaScriptFunction(calling = "getPermittedFeatures", returning = "permittedFeatures", withGuild = true)
	public static void getPermittedFeatures() {};

}
