package me.eglp.gv2.commands.moderation;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.GraphiteModule;
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

public class CommandTempBan extends Command {

	public CommandTempBan() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "tempban");
		setDescription(DefaultLocaleString.COMMAND_TEMPBAN_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_TEMPBAN_USAGE);
		setPermission(DefaultPermissions.MODERATION_TEMPBAN);
		requirePermissions(Permission.BAN_MEMBERS);
	}

	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteUser user = (GraphiteUser) event.getOption("user");
		GraphiteGuild g = event.getGuild();
		GraphiteMember mem = g.getMember(user);
		if(mem == null) {
			DefaultMessage.ERROR_NOT_A_MEMBER.reply(event);
			return;
		}

		if(mem.isBot()) {
			DefaultMessage.ERROR_IS_BOT.reply(event);
			return;
		}

		if(!g.getSelfMember().canInteract(mem)) {
			DefaultMessage.ERROR_CANT_INTERACT_MEMBER.reply(event);
			return;
		}

		if(g.isBanned(mem.getID())) {
			DefaultMessage.COMMAND_TEMPBAN_ALREADY_BANNED.reply(event);
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
		g.getTemporaryActionsConfig().tempBanMember(mem, duration, event.getMember(), r);

		DefaultMessage.COMMAND_TEMPBAN_SUCCESS.reply(event,
				"user", mem.getAsMention(),
				"duration", LocalizedTimeUnit.formatTime(g, duration),
				"reason", r == null ? "No reason" : r);
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.USER, "user", "The user you want to temporarily ban", true),
				new OptionData(OptionType.STRING, "duration", "How long do you want to temporarily ban the user", true),
				new OptionData(OptionType.STRING, "reason", "A short reason why you want to temporarily ban this user", false)
			);
	}

}
