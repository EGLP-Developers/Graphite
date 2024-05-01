package me.eglp.gv2.util.backup;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.webinterface.js.JavaScriptEnum;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

@JavaScriptEnum
public enum RestoreSelector implements WebinterfaceObject {

	// Ignore: jails, tempbans/-mutes/jails, userchannels

	DISCORD_ICON("Server Icon", JDAEmote.PARK),
	DISCORD_OVERVIEW_SETTINGS("Server Settings", JDAEmote.GEAR),
	DISCORD_BANS("Server Bans", JDAEmote.NO_ENTRY_SIGN),
	DISCORD_ROLES("Server Roles", JDAEmote.CLOSED_LOCK_WITH_KEY),
	DISCORD_ROLE_ASSIGNMENTS("Role assignments", JDAEmote.LOCK_WITH_INK_PEN, DISCORD_ROLES),
	DISCORD_CHANNELS("Server Channels", JDAEmote.HASH),
	DISCORD_CHAT_HISTORY("Chat history", JDAEmote.ENVELOPE_WITH_ARROW, DISCORD_CHANNELS),
	DISCORD_THREAD_CHAT_HISTORY("Thread chat history", JDAEmote.INCOMING_ENVELOPE, DISCORD_CHAT_HISTORY),


	SUPPORT("Support", JDAEmote.BUSTS_IN_SILHOUETTE, DISCORD_CHANNELS, DISCORD_ROLES), // Support queues, supporter roles etc.
	CHANNEL_MANAGEMENT("Channel Management", JDAEmote.HASH, DISCORD_CHANNELS), // autochannels
	GREETER("Greeter", JDAEmote.WAVE, DISCORD_CHANNELS), // greeting/farewell
	MODERATION_AUTOMOD("Automod", JDAEmote.ROBOT, DISCORD_ROLES, DISCORD_CHANNELS), // Mod roles, automod, mod log channel
	ROLE_MANAGEMENT("Role Settings", JDAEmote.SHIELD, DISCORD_ROLES), // accessroles, auto-/botroles
	PERMISSIONS("Permissions", JDAEmote.SCROLL, DISCORD_ROLES),
	CUSTOM_COMMANDS("Custom Commands", JDAEmote.EXCLAMATION),
	COMMAND_ALIASES("Command Aliases", JDAEmote.PENCIL2),
	STATISTICS("Statistics", JDAEmote.CHART_WITH_UPWARDS_TREND, DISCORD_CHANNELS),
	REDDIT("Reddit", JDAEmote.PENCIL, DISCORD_CHANNELS),
	TWITCH("Twitch", JDAEmote.PENCIL, DISCORD_CHANNELS),
	TWITTER("Twitter", JDAEmote.PENCIL, DISCORD_CHANNELS);

	@JavaScriptValue(getter = "getFriendlyName")
	private String friendlyName;

	private JDAEmote emote;
	private RestoreSelector[] requires;

	private RestoreSelector(String friendlyName, JDAEmote emote, RestoreSelector... requires) {
		this.friendlyName = friendlyName;
		this.emote = emote;
		this.requires = requires;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public JDAEmote getEmote() {
		return emote;
	}

	@JavaScriptGetter(name = "getRequires", returning = "requires")
	public RestoreSelector[] getRequires() {
		return requires;
	}

	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("requires", new JSONArray(Arrays.stream(requires)
				.map(r -> r.toWebinterfaceObject())
				.toList()));
	}

	public boolean appliesTo(EnumSet<RestoreSelector> selectors) {
		return selectors.contains(this) && requirementsApplyTo(selectors);
	}

	public boolean requirementsApplyTo(EnumSet<RestoreSelector> selectors) {
		return Arrays.stream(requires).allMatch(s -> s.appliesTo(selectors));
	}

	public EnumSet<RestoreSelector> getMissingRequirements(EnumSet<RestoreSelector> selectors) {
		return Arrays.stream(requires)
				.filter(s -> !s.appliesTo(selectors))
				.collect(Collectors.toCollection(() -> EnumSet.noneOf(RestoreSelector.class)));
	}

}
