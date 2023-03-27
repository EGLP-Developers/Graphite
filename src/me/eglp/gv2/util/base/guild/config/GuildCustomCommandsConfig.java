package me.eglp.gv2.util.base.guild.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.alias.CommandAlias;
import me.eglp.gv2.util.base.guild.customcommand.CommandAction;
import me.eglp.gv2.util.base.guild.customcommand.GraphiteCustomCommand;
import me.eglp.gv2.util.command.text.CommandHandler;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.SerializationOption;
import me.mrletsplay.mrcore.misc.FriendlyException;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.ErrorResponse;

@SQLTable(
	name = "guilds_customcommands",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"CommandName varchar(255) NOT NULL",
		"Permission varchar(255) DEFAULT NULL",
		"Actions longtext DEFAULT NULL",
		"SlashCommandId varchar(255) DEFAULT NULL", // NONBETA: MySQL change
		"PRIMARY KEY (GuildId, CommandName)"
	},
	guildReference = "GuildId"
)
@SQLTable(
	name = "guilds_command_aliases",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"Alias varchar(255) NOT NULL",
		"ForCommand varchar(255) NOT NULL",
		"SlashCommandId varchar(255) DEFAULT NULL",
		"PRIMARY KEY (GuildId, Alias)"
	},
	guildReference = "GuildId"
)
public class GuildCustomCommandsConfig {

	private GraphiteGuild guild;
	
	public GuildCustomCommandsConfig(GraphiteGuild guild) {
		this.guild = guild;
	}
	
	public List<GraphiteCustomCommand> getCustomCommands() {
		return Graphite.getMySQL().queryArray(String.class, "SELECT CommandName FROM guilds_customcommands WHERE GuildId = ? ORDER BY CommandName", guild.getID()).orElse(Collections.emptyList()).stream()
				.map(c -> getCustomCommandByName(c))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
	
	public void setCustomCommands(List<GraphiteCustomCommand> customCommands) {
		removeAllCustomCommands();
		customCommands.forEach(c -> c.createOrUpdateSlashCommand(guild));
		Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("INSERT INTO guilds_customcommands(GuildId, CommandName, Permission, Actions, SlashCommandId) VALUES(?, ?, ?, ?, ?)")) {
				for(GraphiteCustomCommand c : customCommands) {
					s.setString(1, guild.getID());
					s.setString(2, c.getName());
					s.setString(3, c.getPermission());
					s.setString(4, c.getActions().stream()
							.map(a -> a.toJSON(SerializationOption.DONT_INCLUDE_CLASS))
							.collect(Collectors.toCollection(JSONArray::new)).toString());
					s.setString(5, c.getSlashCommandID());
					s.addBatch();
				}
				
				s.executeBatch();
			}
		});
	}
	
	public void removeAllCustomCommands() {
		getCustomCommands().forEach(cc -> cc.deleteSlashCommand(guild));
		Graphite.getMySQL().query("DELETE FROM guilds_customcommands WHERE GuildId = ?", guild.getID());
	}
	
	public GraphiteCustomCommand getCustomCommandByName(String name) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT Permission, Actions, SlashCommandId FROM guilds_customcommands WHERE GuildId = ? AND CommandName = ?")) {
				st.setString(1, guild.getID());
				st.setString(2, name);
				try(ResultSet r = st.executeQuery()) {
					if(!r.next()) return null;
					return new GraphiteCustomCommand(name, r.getString("Permission"), new JSONArray(r.getString("Actions")).stream()
							.map(a -> JSONConverter.decodeObject((JSONObject) a, CommandAction.class))
							.collect(Collectors.toList()),
							r.getString("SlashCommandId"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load CustomCommand from MySQL", e));
	}
	
	public void addOrUpdateCustomCommand(GraphiteCustomCommand command) {
		command.createOrUpdateSlashCommand(guild);
		Graphite.getMySQL().query("INSERT INTO guilds_customcommands(GuildId, CommandName, Permission, Actions, SlashCommandId) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Permission = VALUES(Permission), Actions = VALUES(Actions), SlashCommandId = VALUES(SlashCommandId)", guild.getID(), command.getName(), command.getPermission(), command.getActions().stream()
				.map(a -> a.toJSON(SerializationOption.DONT_INCLUDE_CLASS))
				.collect(Collectors.toCollection(JSONArray::new)).toString(),
				command.getSlashCommandID());
	}
	
	public void removeCustomCommand(GraphiteCustomCommand command) {
		command.deleteSlashCommand(guild);
		Graphite.getMySQL().query("DELETE FROM guilds_customcommands WHERE GuildId = ? AND CommandName = ?", guild.getID(), command.getName());
	}
	
	public GraphiteCustomCommand createCustomCommand(String name) {
		GraphiteCustomCommand cc = new GraphiteCustomCommand(name);
		addOrUpdateCustomCommand(cc);
		return cc;
	}
	
	public void updateSlashCommands() {
		try {
			List<GraphiteCustomCommand> ccs = getCustomCommands();
			List<CommandAlias> aliases = getCommandAliases();
			if(ccs.isEmpty() && aliases.isEmpty()) return;
			
			List<Command> scs = guild.getJDAGuild().retrieveCommands().complete();

			for(GraphiteCustomCommand cc : ccs) {
				Command c = scs.stream()
					.filter(cmd -> cmd.getName().equals(cc.getName()))
					.findFirst().orElse(null);
				
				if(c != null && c.getOptions().equals(cc.getOptions())) continue; // Command is already up to date
				
				GraphiteDebug.log(DebugCategory.CUSTOM_COMMAND, "Found command '" + cc.getName() + "' on guild " + guild.getID() + " that is not up-to-date. Updating");
				addOrUpdateCustomCommand(cc);
			}

			List<String> aliasesToRemove = new ArrayList<>();
			aliases.forEach(a -> {
				me.eglp.gv2.util.command.Command graphiteCommand = CommandHandler.getGlobalCommandExact(a.getForCommand());
				if(graphiteCommand == null) {
					GraphiteDebug.log(DebugCategory.CUSTOM_COMMAND, "Removing alias '" + a.getAlias() + "' for invalid command '" + a.getForCommand() + "' on guild " + guild.getID());
					aliasesToRemove.add(a.getAlias());
					return;
				}
				
				Command c = scs.stream()
					.filter(cmd -> cmd.getName().equals(a.getAlias()))
					.findFirst().orElse(null);
				
				if(c != null && c.getOptions().equals(graphiteCommand.getOptions())) return;
				
				GraphiteDebug.log(DebugCategory.CUSTOM_COMMAND, "Found alias '" + a.getAlias() + "' on guild " + guild.getID() + " that is not up-to-date. Updating");
				addOrUpdateCommandAlias(a);
			});
			
			if(!aliasesToRemove.isEmpty()) {
				Graphite.getMySQL().run(c -> {
					try(PreparedStatement st = c.prepareStatement("DELETE FROM guilds_command_aliases WHERE GuildId = ? AND Alias = ?")) {
						for(String a : aliasesToRemove) {
							st.setString(1, guild.getID());
							st.setString(2, a);
							st.addBatch();
						}
						
						st.executeBatch();
					}
				});
			}
		}catch(ErrorResponseException e) {
			if(e.getErrorResponse() == ErrorResponse.MISSING_ACCESS) {
				GraphiteDebug.log(DebugCategory.CUSTOM_COMMAND, "Missing access to update slash CustomCommands for guild " + guild.getID());
				return;
			}
			
			GraphiteDebug.log(DebugCategory.CUSTOM_COMMAND, "Failed to update slash CustomCommands for guild " + guild.getID(), e);
			return;
		}
	}
	
	public void addOrUpdateCommandAlias(String alias, String forCommand) {
		CommandAlias a = new CommandAlias(alias, forCommand, null);
		addOrUpdateCommandAlias(a);
	}
	
	public void addOrUpdateCommandAlias(CommandAlias alias) {
		alias.createOrUpdateSlashCommand(guild);
		Graphite.getMySQL().query("INSERT INTO guilds_command_aliases(GuildId, Alias, ForCommand, SlashCommandId) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE ForCommand = VALUES(ForCommand), SlashCommandId = VALUES(SlashCommandId)", guild.getID(), alias.getAlias(), alias.getForCommand(), alias.getSlashCommandID());
	}
	
	private void removeAllCommandAliases() {
		getCustomCommands().forEach(cc -> cc.deleteSlashCommand(guild));
		Graphite.getMySQL().query("DELETE FROM guilds_command_aliases WHERE GuildId = ?", guild.getID());
	}
	
	public void setCommandAliases(Map<String, String> aliases) {
		removeAllCommandAliases();
		
		List<CommandAlias> als = aliases.entrySet().stream()
			.map(e -> new CommandAlias(e.getKey(), e.getValue(), null))
			.collect(Collectors.toList());
		
		als.forEach(a -> a.createOrUpdateSlashCommand(guild));
		
		Graphite.getMySQL().run(c -> {
			try(PreparedStatement st = c.prepareStatement("INSERT INTO guilds_command_aliases(GuildId, Alias, ForCommand, SlashCommandId) VALUES(?, ?, ?, ?)")) {
				for(CommandAlias a : als) {
					st.setString(1, guild.getID());
					st.setString(2, a.getAlias());
					st.setString(3, a.getForCommand());
					st.setString(4, a.getSlashCommandID());
					st.addBatch();
				}
				
				st.executeBatch();
			}
		});
	}
	
	public void removeCommandAlias(String alias) {
		Graphite.getMySQL().query("DELETE FROM guilds_command_aliases WHERE GuildId = ? AND Alias = ?", guild.getID(), alias);
	}
	
	public List<CommandAlias> getCommandAliases() {
		return Graphite.getMySQL().run(c -> {
			try(PreparedStatement st = c.prepareStatement("SELECT * FROM guilds_command_aliases WHERE GuildId = ?")) {
				st.setString(1, guild.getID());
				try(ResultSet r = st.executeQuery()) {
					List<CommandAlias> aliases = new ArrayList<>();
					while(r.next()) {
						aliases.add(new CommandAlias(r.getString("Alias"), r.getString("ForCommand"), r.getString("SlashCommandId")));
					}
					return aliases;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load command aliases from MySQL", e));
	}
	
	public CommandAlias getCommandAlias(String alias) {
		return Graphite.getMySQL().run(c -> {
			try(PreparedStatement st = c.prepareStatement("SELECT * FROM guilds_command_aliases WHERE GuildId = ? AND Alias = ?")) {
				st.setString(1, guild.getID());
				st.setString(2, alias);
				try(ResultSet r = st.executeQuery()) {
					if(!r.next()) return null;
					return new CommandAlias(alias, r.getString("ForCommand"), r.getString("SlashCommandId"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load command alias from MySQL", e));
	}

}
