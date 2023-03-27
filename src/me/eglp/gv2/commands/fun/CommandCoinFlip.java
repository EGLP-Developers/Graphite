package me.eglp.gv2.commands.fun;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.user.EasterEgg;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandCoinFlip extends Command {
	
	private static final Random RANDOM = new Random();

	public CommandCoinFlip() {
		super(GraphiteModule.FUN, CommandCategory.FUN, "coinflip");
		addAlias("flip");
		setAllowPrivate(true);
		setDescription(DefaultLocaleString.COMMAND_COINFLIP_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_COINFLIP_USAGE);
	}
	
	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		int r = RANDOM.nextInt(6001);
		
		if(r < 3000) {
			event.reply(JDAEmote.COINFLIP_HEADS.getUnicode());
		}else if(r >= 3000 && r < 6000) {
			event.reply(JDAEmote.COINFLIP_TAILS.getUnicode());
		}else {
			if(!event.getAuthor().getConfig().hasFoundEasterEgg(EasterEgg.COIN_FLIP_SIDE)) {
				event.getAuthor().getConfig().addEasterEgg(EasterEgg.COIN_FLIP_SIDE, true);
			}
			DefaultMessage.COMMAND_COINFLIP_SIDE.reply(event);
		}
	}

	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}

}
