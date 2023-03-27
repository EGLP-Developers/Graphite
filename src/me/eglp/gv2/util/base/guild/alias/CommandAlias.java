package me.eglp.gv2.util.base.guild.alias;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.text.CommandHandler;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;

@JavaScriptClass
public class CommandAlias {
	
	@JavaScriptValue(getter = "getAlias", setter = "setAlias")
	private String alias;
	
	@JavaScriptValue(getter = "getForCommand", setter = "setForCommand")
	private String forCommand;
	
	private String slashCommandID;
	
	@JSONConstructor
	public CommandAlias() {}
	
	public CommandAlias(String alias, String forCommand, String slashCommandID) {
		this.alias = alias;
		this.forCommand = forCommand;
		this.slashCommandID = slashCommandID;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public String getForCommand() {
		return forCommand;
	}
	
	public String getSlashCommandID() {
		return slashCommandID;
	}
	
	public void createOrUpdateSlashCommand(GraphiteGuild guild) {
		Command graphiteCommand = CommandHandler.getGlobalCommandExact(forCommand);
		if(graphiteCommand == null) return;
		slashCommandID = guild.getJDAGuild().upsertCommand(alias, "Alias for " + forCommand)
			.addOptions(graphiteCommand.getOptions())
			.complete().getId();
	}
	
	public void deleteSlashCommand(GraphiteGuild guild) {
		if(slashCommandID != null) guild.getJDAGuild().deleteCommandById(slashCommandID).queue(null, e -> {});
	}

}
