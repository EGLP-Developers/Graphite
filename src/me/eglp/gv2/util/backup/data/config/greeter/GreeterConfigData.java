package me.eglp.gv2.util.backup.data.config.greeter;

import java.util.EnumSet;

import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.backup.RestoreSelector;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.base.guild.config.GuildGreeterConfig;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class GreeterConfigData implements JSONConvertible {
	
	@JSONValue
	private boolean greetingEnabled;

	@JSONValue
	private String greetingChannel;

	@JSONValue
	private String greetingMessage;
	
	@JSONValue
	private boolean farewellEnabled;

	@JSONValue
	private String farewellChannel;

	@JSONValue
	private String farewellMessage;
	
	@JSONConstructor
	private GreeterConfigData() {}
	
	public GreeterConfigData(GraphiteGuild guild) {
		GuildGreeterConfig c = guild.getGreeterConfig();
		this.greetingEnabled = c.isGreetingEnabled();
		this.greetingChannel = c.getGreetingChannel() != null ? c.getGreetingChannel().getID() : null;
		this.greetingMessage = c.getGreetingMessage();
		
		this.farewellEnabled = c.isFarewellEnabled();
		this.farewellChannel = c.getFarewellChannel() != null ? c.getFarewellChannel().getID() : null;
		this.farewellMessage = c.getFarewellMessage();
	}

	public boolean isGreetingEnabled() {
		return greetingEnabled;
	}

	public String getGreetingChannel() {
		return greetingChannel;
	}

	public String getGreetingMessage() {
		return greetingMessage;
	}

	public boolean isFarewellEnabled() {
		return farewellEnabled;
	}

	public String getFarewellChannel() {
		return farewellChannel;
	}

	public String getFarewellMessage() {
		return farewellMessage;
	}
	
	public void restore(GraphiteGuild guild, EnumSet<RestoreSelector> selectors, IDMappings mappings) {
		if(RestoreSelector.GREETER.appliesTo(selectors)) {
			GuildGreeterConfig c = guild.getGreeterConfig();
			
			String newGreetingChannel = mappings.getNewID(greetingChannel);
			GraphiteTextChannel gC = greetingChannel == null ? null : guild.getTextChannelByID(newGreetingChannel);
			c.setGreetingChannel(gC);
			c.enableGreeting(greetingEnabled);
			c.setGreetingMessage(greetingMessage);

			String newFarewellChannel = mappings.getNewID(farewellChannel);
			GraphiteTextChannel fC = farewellChannel == null ? null : guild.getTextChannelByID(newFarewellChannel);
			c.setFarewellChannel(fC);
			c.enableFarewell(farewellEnabled);
			c.setFarewellMessage(farewellMessage);
		}
	}
	
}
