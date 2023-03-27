package me.eglp.gv2.commands.info;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandAbout extends Command{
	
	private static final Map<String, String>
			LIBRARIES = new LinkedHashMap<>(),
			CONTRIBUTORS = new LinkedHashMap<>();
	
	static {
		LIBRARIES.put("Lavaplayer", "https://github.com/sedmelluq/lavaplayer");
		LIBRARIES.put("lavadsp", "https://github.com/natanbc/lavadsp");
		LIBRARIES.put("Discord Webhooks", "https://github.com/MinnDevelopment/discord-webhooks");
		LIBRARIES.put("Mozilla Rhino", "https://github.com/mozilla/rhino");
		LIBRARIES.put("AmongUsCapture", "https://github.com/denverquane/amonguscapture");
		LIBRARIES.put("MariaDB", "https://mariadb.org/");
		LIBRARIES.put("JRAW", "https://github.com/mattbdean/JRAW");
		
		CONTRIBUTORS.put("JG-Cody", "https://jg-cody.de");
		CONTRIBUTORS.put("The Arrayser", null);
	}
	
	public CommandAbout() {
		super(null, CommandCategory.INFO, "about");
		setDescription(DefaultLocaleString.COMMAND_ABOUT_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_ABOUT_USAGE);
		addAlias("botinfo");
	}

	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.setColor(Color.GRAY);
		eb.setTitle(DefaultLocaleString.COMMAND_ABOUT_TITLE.getFor(event.getSender()));
		eb.setThumbnail(Graphite.getIconUrl());
		eb.addField(DefaultLocaleString.COMMAND_ABOUT_NAME_TITLE.getFor(event.getSender()), Graphite.getBotInfo().getName(), true);
		eb.addField(DefaultLocaleString.COMMAND_ABOUT_DEVELOPERS_TITLE.getFor(event.getSender()), "[Endergame15](https://github.com/Endergame15) and [MrLetsplay](https://github.com/MrLetsplay2003)", false);
		eb.addField(DefaultLocaleString.COMMAND_ABOUT_LIBRARIES_TITLE.getFor(event.getSender()), LIBRARIES.entrySet().stream().map(e -> "[" + e.getKey() + "](" + e.getValue() + ")").collect(Collectors.joining("\n")), true);
		eb.addField(DefaultLocaleString.COMMAND_ABOUT_DISCORD_LIBRARY_TITLE.getFor(event.getSender()), "[JDA](https://github.com/DV8FromTheWorld/JDA)", true);
		eb.addBlankField(true);
		eb.addField(DefaultLocaleString.COMMAND_ABOUT_SERVER_COUNT_TITLE.getFor(event.getSender()), String.valueOf(Graphite.getGuildCount()), true);
		eb.addField(DefaultLocaleString.COMMAND_ABOUT_SHARD_COUNT_TITLE.getFor(event.getSender()), String.valueOf(Graphite.getShards().size()), true);
		eb.addBlankField(true);
		eb.addField(DefaultLocaleString.COMMAND_ABOUT_WEBSITE_TITLE.getFor(event.getSender()), "https://graphite-official.com", true);
		eb.addField(DefaultLocaleString.COMMAND_ABOUT_PATREON_TITLE.getFor(event.getSender()), "https://www.patreon.com/graphite_official", true);
		eb.addField(DefaultLocaleString.COMMAND_ABOUT_TWITTER_TITLE.getFor(event.getSender()), "https://twitter.com/GraphiteSupport", true);
		eb.addField(DefaultLocaleString.COMMAND_ABOUT_CONTRIBUTORS_TITLE.getFor(event.getSender()), CONTRIBUTORS.entrySet().stream().map(e -> e.getValue() == null ? e.getKey() : "[" + e.getKey() + "](" + e.getValue() + ")").collect(Collectors.joining("\n")), false);
		
		event.reply(eb.build());
	}

	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}
	
}
