package me.eglp.gv2.commands.info;

import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.config.GuildModerationConfig;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandUserInfo extends Command{

	public CommandUserInfo() {
		super(null, CommandCategory.INFO, "userinfo");
		setDescription(DefaultLocaleString.COMMAND_USERINFO_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_USERINFO_USAGE);
		addAlias("uinfo");
	}

	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteUser usr = (GraphiteUser) event.getOption("user");
		GraphiteGuild g = event.getGuild();
		GraphiteMember gMem = g.getMember(usr);
		User u = usr.getJDAUser();
		Member m = gMem.getMember();
		GuildModerationConfig c = g.getModerationConfig();

		EmbedBuilder eb = new EmbedBuilder();

		eb.setColor(Color.DARK_GRAY);
		eb.setTitle(DefaultLocaleString.COMMAND_USERINFO_TITLE.getFor(event.getSender(), "user", u.getName()));
		eb.setThumbnail(u.getAvatarUrl());
		eb.addField(DefaultLocaleString.COMMAND_USERINFO_NAME_TITLE.getFor(event.getSender()), usr.getFullName(), false);
		eb.addField(DefaultLocaleString.COMMAND_USERINFO_NICKNAME_TITLE.getFor(event.getSender()), m.getNickname() == null ? DefaultLocaleString.COMMAND_USERINFO_NICKNAME_NO_NICKNAME.getFor(event.getSender()) : m.getNickname(), false);
		eb.addField(DefaultLocaleString.COMMAND_USERINFO_ID_TITLE.getFor(event.getSender()), u.getId(), true);
		eb.addField(DefaultLocaleString.COMMAND_USERINFO_JOINED_TITLE.getFor(event.getSender()), m.getTimeJoined().format(DateTimeFormatter.RFC_1123_DATE_TIME), false);
		eb.addField(DefaultLocaleString.COMMAND_USERINFO_CURRENTLY_IN_AUDIOCHANNEL_TITLE.getFor(event.getSender()), !m.getVoiceState().inAudioChannel() ? DefaultLocaleString.COMMAND_USERINFO_CURRENTLY_IN_AUDIOCHANNEL_NONE.getFor(event.getSender()) : m.getVoiceState().getChannel().getName(), false);

		eb.addBlankField(false);

		eb.addField(DefaultLocaleString.COMMAND_USERINFO_SELF_MUTED_TITLE.getFor(event.getSender()), m.getVoiceState().isSelfMuted() ? ":white_check_mark:" : ":x:", true);
		eb.addField(DefaultLocaleString.COMMAND_USERINFO_SELF_DEAFENED_TITLE.getFor(event.getSender()), m.getVoiceState().isSelfDeafened() ? ":white_check_mark:" : ":x:", true);
		eb.addBlankField(true);

		eb.addBlankField(false);

		eb.addField(DefaultLocaleString.COMMAND_USERINFO_SERVER_MUTED_TITLE.getFor(event.getSender()), m.getVoiceState().isGuildMuted() ? ":white_check_mark:" : ":x:", true);
		eb.addField(DefaultLocaleString.COMMAND_USERINFO_SERVER_DEAFENED_TITLE.getFor(event.getSender()), m.getVoiceState().isGuildDeafened() ? ":white_check_mark:" : ":x:", true);
		eb.addBlankField(true);

		eb.addBlankField(false);

		eb.addField(DefaultLocaleString.COMMAND_USERINFO_TEMP_BANNED_TITLE.getFor(event.getSender()), gMem.isBanned() ? ":white_check_mark:" : ":x:", true);
		eb.addField(DefaultLocaleString.COMMAND_USERINFO_TEMP_MUTED_TITLE.getFor(event.getSender()), gMem.isMuted() ? ":white_check_mark:" : ":x:", true);
		eb.addField(DefaultLocaleString.COMMAND_USERINFO_TEMP_JAILED_TITLE.getFor(event.getSender()), c.isJailed(gMem) ? ":white_check_mark:" : ":x:", true);

		event.reply(eb.build());
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.USER, "user", "The user to view information about", true)
			);
	}

}
