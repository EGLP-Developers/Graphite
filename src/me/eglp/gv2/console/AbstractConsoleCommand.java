package me.eglp.gv2.console;

import me.mrletsplay.mrcore.command.AbstractCommand;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.properties.CommandProperties;

public abstract class AbstractConsoleCommand extends AbstractCommand<CommandProperties> {

	public AbstractConsoleCommand(String name) {
		super(name);
	}

	@Override
	public abstract void action(CommandInvokedEvent event);

}
