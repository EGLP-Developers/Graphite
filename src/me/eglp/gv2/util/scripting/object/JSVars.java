package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.util.backup.RestoreSelector;
import me.eglp.gv2.util.mention.MentionType;
import me.eglp.gv2.util.scripting.ScriptExecutionException;
import net.dv8tion.jda.api.entities.channel.ChannelType;

/**
 * Contains variables to be used in JavaScript using {@code vars.(VARIABLE_NAME)}
 */
public class JSVars {

	/**
	 * The JSVars instance passed to the script
	 */
	public static final JSVars INSTANCE = new JSVars();

	// Mention Types

	/**
	 * The USER mention type ({@code @User}, {@code @Member}, {@code @<Username#User hash>}, {@code @<Username>})
	 */
	public static final JSMentionType MENTION_TYPE_USER = new JSMentionType(MentionType.USER);

	/**
	 * The ROLE mention type ({@code @Role})
	 */
	public static final JSMentionType MENTION_TYPE_ROLE = new JSMentionType(MentionType.ROLE);

	/**
	 * The TEXT_CHANNEL mention type ({@code #text-channel})
	 */
	public static final JSMentionType MENTION_TYPE_TEXT_CHANNEL = new JSMentionType(MentionType.TEXT_CHANNEL);

	/**
	 * The EVERYONE mention type ({@code @everyone})
	 */
	public static final JSMentionType MENTION_TYPE_EVERYONE = new JSMentionType(MentionType.EVERYONE);

	/**
	 * The HERE mention type ({@code @here})
	 */
	public static final JSMentionType MENTION_TYPE_HERE = new JSMentionType(MentionType.HERE);

	/**
	 * The VOICE_CHANNEL mention type ({@code #!voice-channel})
	 */
	public static final JSMentionType MENTION_TYPE_VOICE_CHANNEL = new JSMentionType(MentionType.VOICE_CHANNEL);

	/**
	 * The DISCORD_ICON restore selector
	 */
	public static final JSRestoreSelector RESTORE_SELECTOR_DISCORD_ICON = new JSRestoreSelector(RestoreSelector.DISCORD_ICON);

	/**
	 * The DISCORD_OVERVIEW_SETTINGS restore selector
	 */
	public static final JSRestoreSelector RESTORE_SELECTOR_DISCORD_OVERVIEW_SETTINGS = new JSRestoreSelector(RestoreSelector.DISCORD_OVERVIEW_SETTINGS);

	/**
	 * The DISCORD_BANS restore selector
	 */
	public static final JSRestoreSelector RESTORE_SELECTOR_DISCORD_BANS = new JSRestoreSelector(RestoreSelector.DISCORD_BANS);

	/**
	 * The DISCORD_ROLES restore selector
	 */
	public static final JSRestoreSelector RESTORE_SELECTOR_DISCORD_ROLES = new JSRestoreSelector(RestoreSelector.DISCORD_ROLES);

	/**
	 * The DISCORD_ROLE_ASSIGNMENTS restore selector
	 */
	public static final JSRestoreSelector RESTORE_SELECTOR_DISCORD_ROLE_ASSIGNMENTS = new JSRestoreSelector(RestoreSelector.DISCORD_ROLE_ASSIGNMENTS);

	/**
	 * The DISCORD_CHANNELS restore selector
	 */
	public static final JSRestoreSelector RESTORE_SELECTOR_DISCORD_CHANNELS = new JSRestoreSelector(RestoreSelector.DISCORD_CHANNELS);

	/**
	 * The DISCORD_CHAT_HISTORY restore selector
	 */
	public static final JSRestoreSelector RESTORE_SELECTOR_DISCORD_CHAT_HISTORY = new JSRestoreSelector(RestoreSelector.DISCORD_CHAT_HISTORY);

	/**
	 * The SUPPORT restore selector
	 */
	public static final JSRestoreSelector RESTORE_SELECTOR_SUPPORT = new JSRestoreSelector(RestoreSelector.SUPPORT);

	/**
	 * The CHANNEL_MANAGEMENT restore selector
	 */
	public static final JSRestoreSelector RESTORE_SELECTOR_CHANNEL_MANAGEMENT = new JSRestoreSelector(RestoreSelector.CHANNEL_MANAGEMENT);

	/**
	 * The CHANNEL_MANAGEMENT restore selector
	 */
	public static final JSRestoreSelector RESTORE_SELECTOR_GREETER = new JSRestoreSelector(RestoreSelector.GREETER);

	/**
	 * The MODERATION_AUTOMOD restore selector
	 */
	public static final JSRestoreSelector RESTORE_SELECTOR_MODERATION_AUTOMOD = new JSRestoreSelector(RestoreSelector.MODERATION_AUTOMOD);

	/**
	 * The MODERATION_ROLES restore selector
	 */
	public static final JSRestoreSelector RESTORE_SELECTOR_MODERATION_ROLES = new JSRestoreSelector(RestoreSelector.ROLE_MANAGEMENT);

	/**
	 * The PERMISSIONS restore selector
	 */
	public static final JSRestoreSelector RESTORE_SELECTOR_PERMISSIONS = new JSRestoreSelector(RestoreSelector.PERMISSIONS);

	/**
	 * The CUSTOM_COMMANDS restore selector
	 */
	public static final JSRestoreSelector RESTORE_SELECTOR_CUSTOM_COMMANDS = new JSRestoreSelector(RestoreSelector.CUSTOM_COMMANDS);



	// Channel Types

	/**
	 * The TEXT channel type (guild text channel)
	 */
	public static final JSChannelType CHANNEL_TYPE_TEXT = new JSChannelType(ChannelType.TEXT);

	/**
	 * The VOICE channel type (guild voice channel)
	 */
	public static final JSChannelType CHANNEL_TYPE_VOICE = new JSChannelType(ChannelType.VOICE);

	/**
	 * The CATEGORY channel type (guild category)
	 */
	public static final JSChannelType CHANNEL_TYPE_CATEGORY = new JSChannelType(ChannelType.CATEGORY);

	/**
	 * The PRIVATE channel type (private chat with a user)
	 */
	public static final JSChannelType CHANNEL_TYPE_PRIVATE = new JSChannelType(ChannelType.PRIVATE);

	public void throwException(String message) throws ScriptExecutionException {
		throw new ScriptExecutionException(message);
	}

	@Override
	public String toString() {
		return "[JS Vars]";
	}

}
