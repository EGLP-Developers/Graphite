package me.eglp.gv2.commands.fun;


import java.util.Arrays;
import java.util.List;
import java.util.Random;

import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandEightBall extends Command {
	
	private static final double NEUTRAL_REPLY_CHANCE = 0.2;
	
	private static final String[] POSITIVE_RESPONSES = {
		"Most definitely",
		"As I see it, yes",
		"It is certain",
		"It is decidedly so",
		"Most likely",
		"Outlook good",
		"Signs point to yes",
		"Without a doubt",
		"Yes",
		"Yes, definitely",
		"You may rely on it",
		"All signs point to yes",
	};
	
	private static final String[] NEGATIVE_RESPONSES = {
		"Don't count on it",
		"My reply is no",
		"My sources say no",
		"Outlook not so good",
		"Very doubtful",
		"I don't think so",
		"All signs point to no",
	};
	
	private static final String[] NEUTRAL_RESPONSES = {
		"Ask again later",
		"Better not tell you now",
		"Cannot predict now",
		"Concentrate and ask again",
		"Reply hazy, try again",
	};
	
	private static final Random RANDOM = new Random();
	
	public CommandEightBall() {
		super(GraphiteModule.FUN, CommandCategory.FUN, "8ball");
		setDescription(DefaultLocaleString.COMMAND_8BALL_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_8BALL_USAGE);
	}
	
	private static final String pickReply() {
		if(RANDOM.nextDouble() < NEUTRAL_REPLY_CHANCE) return NEUTRAL_RESPONSES[RANDOM.nextInt(NEUTRAL_RESPONSES.length)];
		return RANDOM.nextBoolean() ?
			POSITIVE_RESPONSES[RANDOM.nextInt(POSITIVE_RESPONSES.length)] :
			NEGATIVE_RESPONSES[RANDOM.nextInt(NEGATIVE_RESPONSES.length)];
	}
	
	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		event.reply(pickReply());
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.STRING, "question", "Ask anything you want to the magic 8ball", true)
			);
	}

}
