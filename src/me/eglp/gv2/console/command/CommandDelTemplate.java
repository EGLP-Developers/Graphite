package me.eglp.gv2.console.command;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.util.backup.TemplateBackup;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.option.impl.DefaultCommandOption;

public class CommandDelTemplate extends AbstractConsoleCommand{

	public CommandDelTemplate() {
		super("deltemplate");
		setDescription("Delete a backup template");
		addOption(DefaultCommandOption.HELP);
	}

	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getParsedCommand().isOptionPresent(DefaultCommandOption.HELP)) {
			sendCommandInfo(event.getSender());
			return;
		}
		String[] args = event.getParsedCommand().getArguments();
		if(args.length != 1) {
			event.getSender().sendMessage("Usage: deltemplate <id>");
			return;
		}
		TemplateBackup t = TemplateBackup.getTemplateByID(args[0]);
		if(t == null) {
			event.getSender().sendMessage("Backup doesn't exist");
			return;
		}
		String author = t.getAuthor().getName();
		t.delete();
		event.getSender().sendMessage("Template (" + args[0] + " / " + author + ") deleted");
	}

}
