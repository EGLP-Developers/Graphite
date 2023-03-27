package me.eglp.gv2.commands.info;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.user.EasterEgg;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandEasterEggs extends Command{
	
	public CommandEasterEggs() {
		super(null, CommandCategory.INFO, "eastereggs");
		setDescription(DefaultLocaleString.COMMAND_EASTEREGGS_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_EASTEREGGS_USAGE);
		setAllowPrivate(true);
		setAllowServer(false);
	}

	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		EmbedBuilder b = new EmbedBuilder();
		for(EasterEgg e : EasterEgg.values()) {
			if(event.getAuthor().getConfig().hasFoundEasterEgg(e)) {
				b.addField(e.getName(), e.getDescription(), false);
			}else {
				b.addField(DefaultLocaleString.COMMAND_EASTEREGGS_MYSTERY_TITLE.getFor(event.getSender()), 
						DefaultLocaleString.COMMAND_EASTEREGGS_MYSTERY_VALUE.getFor(event.getSender()), false);
			}
		}
		b.addField("", DefaultLocaleString.COMMAND_EASTEREGGS_MYSTERY_FOOTER.getFor(event.getSender(), 
				"amount", String.valueOf(event.getAuthor().getConfig().getFoundEasterEggs().size()), 
				"total_amount", String.valueOf(EasterEgg.values().length),
				"multiplex_url", Graphite.getMainBotInfo().getWebsite().getMultiplexURL()), 
				false);
		
		event.reply(b.build());
	}

	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}

}
