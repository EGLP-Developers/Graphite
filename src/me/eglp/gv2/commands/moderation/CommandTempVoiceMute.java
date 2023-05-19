package me.eglp.gv2.commands.moderation;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.guild.config.GuildTemporaryActionsConfig;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.lang.GraphiteTimeParser;
import me.eglp.gv2.util.lang.LocalizedTimeUnit;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandTempVoiceMute extends Command{

	public CommandTempVoiceMute() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "tempvoicemute");
		addAlias("tvmute");
		setDescription(DefaultLocaleString.COMMAND_TEMPVOICEMUTE_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_TEMPVOICEMUTE_USAGE);
		setPermission(DefaultPermissions.MODERATION_TEMPVOICEMUTE);
		requirePermissions(Permission.VOICE_MUTE_OTHERS);
	}

	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteGuild g = event.getGuild();
		GraphiteUser user = (GraphiteUser) event.getOption("user");
		GraphiteMember mem = g.getMember(user);
		if(mem == null) {
			DefaultMessage.ERROR_NOT_A_MEMBER.reply(event);
			return;
		}

		if(mem.isBot()) {
			DefaultMessage.ERROR_IS_BOT.reply(event);
			return;
		}

		if(mem.getCurrentAudioChannel() == null) {
			DefaultMessage.ERROR_NOT_IN_VOICECHANNEL.reply(event);
			return;
		}

		GuildTemporaryActionsConfig c = g.getTemporaryActionsConfig();
		if(!g.getSelfMember().canInteract(mem)) {
			DefaultMessage.ERROR_CANT_INTERACT_MEMBER.reply(event);
			return;
		}

		if(c.isTempMuted(mem)) {
			DefaultMessage.ERROR_ALREADY_MUTED.reply(event);
			return;
		}

		long duration = GraphiteTimeParser.parseShortDuration((String) event.getOption("duration"));
		if(duration == -1) {
			DefaultMessage.ERROR_INVALID_DURATION.reply(event);
			return;
		}

		if(duration < 1000 * 60) {
			DefaultMessage.ERROR_MINIMUM_TEMP_DURATION.reply(event);
			return;
		}

		String r = (String) event.getOption("reason");
		g.getTemporaryActionsConfig().tempMuteMember(mem, duration, event.getMember(), r);

		DefaultMessage.COMMAND_TEMPVOICEMUTE_SUCCESS.reply(event,
				"user", user.getName(),
				"duration", LocalizedTimeUnit.formatTime(event.getGuild(), duration),
				"reason", r == null ? "No reason" : r);
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.USER, "user", "The user you want to temporarily voicemute", true),
				new OptionData(OptionType.STRING, "duration", "How long you want to temporarily voicemute the user", true),
				new OptionData(OptionType.STRING, "reason", "A short reason why you want to temporarily voicemute this user", false)
			);
	}

}
