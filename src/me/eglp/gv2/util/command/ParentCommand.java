package me.eglp.gv2.util.command;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.util.base.guild.GraphiteModule;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public abstract class ParentCommand extends Command {
	
	public ParentCommand(GraphiteModule module, CommandCategory category, String name) {
		super(module, category, name);
	}
	
	public ParentCommand(Command parent, String name) {
		super(parent, name);
	}

	@Override
	public void action(CommandInvokedEvent event) {}
	
	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}
	
}
