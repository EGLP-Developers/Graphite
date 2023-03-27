package me.eglp.gv2.commands.info;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.economy.GraphiteEconomy;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandMoney extends Command {
	
	public CommandMoney() {
		super(null, CommandCategory.INFO, "money");
		setDescription(DefaultLocaleString.COMMAND_MONEY_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_MONEY_USAGE);
		setAllowPrivate(true);
		setAllowServer(true);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteEconomy ec = Graphite.getEconomy();
		if(ec.getMoney(event.getAuthor()) <= 0) {
			DefaultMessage.COMMAND_MONEY_NO_MONEY.reply(event);
			return;
		}
		
		DefaultMessage.COMMAND_MONEY_MONEY.reply(event, "amount", String.valueOf(ec.getMoney(event.getAuthor())));
	}
	
	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}

}
