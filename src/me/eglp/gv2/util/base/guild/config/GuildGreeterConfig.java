package me.eglp.gv2.util.base.guild.config;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.mysql.SQLTable;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "guilds_farewell",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"IsEnabled bool NOT NULL DEFAULT 0",
		"ChannelId varchar(255) DEFAULT NULL",
		"Message longtext DEFAULT NULL",
		"PRIMARY KEY (GuildId)"
	},
	guildReference = "GuildId"
)
@SQLTable(
	name = "guilds_greeting",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"IsEnabled bool NOT NULL DEFAULT 0",
		"ChannelId varchar(255) DEFAULT NULL",
		"Message longtext DEFAULT NULL",
		"PRIMARY KEY (GuildId)"
	},
	guildReference = "GuildId"
)
@JavaScriptClass(name = "Greeter")
public class GuildGreeterConfig implements WebinterfaceObject, IGuildConfig {
	
	private GraphiteGuild guild;
	
	@JavaScriptConstructor
	public GuildGreeterConfig() {}
	
	public GuildGreeterConfig(GraphiteGuild guild) {
		this.guild = guild;
	}
	
	public GraphiteGuild getGuild() {
		return guild;
	}
	
	@ChannelRemoveListener
	public void removeChannel(GraphiteGuildMessageChannel channel) {
		Graphite.getMySQL().query("UPDATE guilds_greeting SET ChannelId = ? WHERE GuildId = ? AND ChannelId = ?", null, guild.getID(), channel.getID());
		Graphite.getMySQL().query("UPDATE guilds_farewell SET ChannelId = ? WHERE GuildId = ? AND ChannelId = ?", null, guild.getID(), channel.getID());
	}
	
	public void setGreetingMessage(String message) {
		Graphite.getMySQL().query("INSERT INTO guilds_greeting(GuildId, Message) VALUES(?, ?) ON DUPLICATE KEY UPDATE Message = VALUES(Message)", guild.getID(), message);
	}
	
	public void setGreetingChannel(GraphiteGuildMessageChannel channel) {
		if(channel == null) {
			unsetGreetingChannel();
			return;
		}
		
		Graphite.getMySQL().query("INSERT INTO guilds_greeting(GuildId, ChannelId) VALUES(?, ?) ON DUPLICATE KEY UPDATE ChannelId = VALUES(ChannelId)", guild.getID(), channel.getID());
	}
	
	@JavaScriptGetter(name = "getGreetingMessage", returning = "greetingMessage")
	public String getGreetingMessage() {
		String g = Graphite.getMySQL().query(String.class, null, "SELECT Message FROM guilds_greeting WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load message from MySQL", e));
		if(g == null) return DefaultLocaleString.COMMAND_GREETING_DEFAULT_MESSAGE.getFallback();
		return g;
	}

	@JavaScriptGetter(name = "getGreetingChannel", returning = "greetingChannel")
	public GraphiteGuildMessageChannel getGreetingChannel() {
		String id = Graphite.getMySQL().query(String.class, null, "SELECT ChannelId FROM guilds_greeting WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load greeting channel from MySQL", e));
		if(id == null) return null;
		return guild.getGuildMessageChannelByID(id);
	}

	public void unsetGreetingChannel() {
		Graphite.getMySQL().query("UPDATE guilds_greeting SET ChannelId = ? WHERE GuildId = ?", null, guild.getID());
	}
	
	public void enableGreeting(boolean enable) {
		Graphite.getMySQL().query("INSERT INTO guilds_greeting(GuildId, IsEnabled) VALUES(?, ?) ON DUPLICATE KEY UPDATE IsEnabled = VALUES(IsEnabled)", guild.getID(), enable);
	}

	@JavaScriptGetter(name = "isGreetingEnabled", returning = "greetingEnabled")
	public boolean isGreetingEnabled() {
		return Graphite.getMySQL().query(Boolean.class, false, "SELECT IsEnabled FROM guilds_greeting WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load greeting from MySQL", e));
	}
	
	//Farewell
	
	public void setFarewellMessage(String message) {
		Graphite.getMySQL().query("INSERT INTO guilds_farewell(GuildId, Message) VALUES(?, ?) ON DUPLICATE KEY UPDATE Message = VALUES(Message)", guild.getID(), message);
	}
	
	public void setFarewellChannel(GraphiteGuildMessageChannel channel) {
		if(channel == null) {
			unsetFarewellChannel();
			return;
		}
		
		Graphite.getMySQL().query("INSERT INTO guilds_farewell(GuildId, ChannelId) VALUES(?, ?) ON DUPLICATE KEY UPDATE ChannelId = VALUES(ChannelId)", guild.getID(), channel.getID());
	}

	@JavaScriptGetter(name = "getFarewellMessage", returning = "farewellMessage")
	public String getFarewellMessage() {
		String g = Graphite.getMySQL().query(String.class, null, "SELECT Message FROM guilds_farewell WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load message from MySQL", e));
		if(g == null) return DefaultLocaleString.COMMAND_FAREWELL_DEFAULT_MESSAGE.getFallback();
		return g;
	}

	@JavaScriptGetter(name = "getFarewellChannel", returning = "farewellChannel")
	public GraphiteGuildMessageChannel getFarewellChannel() {
		String id = Graphite.getMySQL().query(String.class, null, "SELECT ChannelId FROM guilds_farewell WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load farewell channel from MySQL", e));
		if(id == null) return null;
		return guild.getGuildMessageChannelByID(id);
	}

	@JavaScriptFunction(calling = "unsetFarewellChannel", withGuild = true)
	public void unsetFarewellChannel() {
		Graphite.getMySQL().query("UPDATE guilds_farewell SET ChannelId = ? WHERE GuildId = ?", null, guild.getID());
	}
	
	public void enableFarewell(boolean enable) {
		Graphite.getMySQL().query("INSERT INTO guilds_farewell(GuildId, IsEnabled) VALUES(?, ?) ON DUPLICATE KEY UPDATE IsEnabled = VALUES(IsEnabled)", guild.getID(), enable);
	}

	@JavaScriptGetter(name = "isFarewellEnabled", returning = "farewellEnabled")
	public boolean isFarewellEnabled() {
		return Graphite.getMySQL().query(Boolean.class, false, "SELECT IsEnabled FROM guilds_farewell WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load greeting from MySQL", e));
	}
	
	@Override
	public void preSerializeWI(JSONObject object) {
		GraphiteGuildMessageChannel gc = getGreetingChannel();
		object.put("greetingChannel", gc == null ? null : gc.getID());
		object.put("greetingMessage", getGreetingMessage());
		object.put("greetingEnabled", isGreetingEnabled());

		GraphiteGuildMessageChannel fc = getGreetingChannel();
		object.put("farewellChannel", fc == null ? null : fc.getID());
		object.put("farewellMessage", getFarewellMessage());
		object.put("farewellEnabled", isFarewellEnabled());
	}
	
	@JavaScriptFunction(calling = "getGreeterInfo", returning = "info", withGuild = true)
	public static void getGreeterInfo() {};
	
	@JavaScriptFunction(calling = "setGreeterInfo", withGuild = true)
	public static void setGreeterInfo(@JavaScriptParameter(name = "data") JSONObject data) {};
	
	@JavaScriptFunction(calling = "unsetGreetingChannel", withGuild = true)
	public static void unsetGreetingChannelJS() {};
	
	@JavaScriptFunction(calling = "unsetFarewellChannel", withGuild = true)
	public static void unsetFarewellChannelJS() {};

}
