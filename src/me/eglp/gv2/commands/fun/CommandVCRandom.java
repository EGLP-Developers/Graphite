package me.eglp.gv2.commands.fun;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteAudioChannel;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandVCRandom extends Command{

	public CommandVCRandom() {
		super(GraphiteModule.FUN, CommandCategory.FUN, "vcrandom");
		setDescription(DefaultLocaleString.COMMAND_VCRANDOM_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_VCRANDOM_USAGE);
		setPermission(DefaultPermissions.FUN_VCRANDOM);
	}

	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteAudioChannel channel = event.getMember().getCurrentAudioChannel();
		if(channel == null) {
			DefaultMessage.ERROR_NOT_IN_AUDIOCHANNEL.reply(event);
			return;
		}
		List<Member> members = channel.getJDAChannel().getMembers().stream()
				.filter(m -> !m.getUser().isBot())
				.collect(Collectors.toList());
		int i = new Random().nextInt(members.size());
		Member m = members.get(i);
		GraphiteMember gM = Graphite.getMember(m);
		DefaultMessage.COMMAND_VCRANDOM_PICKED.reply(event, "member_nick", m.getEffectiveName(), "member_full", gM.getFullName());
	}

	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}

}
