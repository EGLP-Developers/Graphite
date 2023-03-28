package me.eglp.gv2.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.emote.JDAEmote;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

public class GraphiteSetup {
	
	public static void run() {
		// Load and create emojis on emoji guilds
		List<GraphiteGuild> guilds = Graphite.getMainBotInfo().getMiscellaneous().getEmojiServerIDs().stream()
			.map(Graphite::getGlobalGuild)
			.collect(Collectors.toList());
		
		if(guilds.stream().anyMatch(g -> g == null)) {
			throw new GraphiteSetupException("Config contains invalid emoji guilds");
		}
		
		for(JDAEmote emote : JDAEmote.values()) {
			if(emote.isCustomEmoji()) {
				if(!emote.loadDefault()) {
					Emoji foundEmoji = guilds.stream()
						.flatMap(g -> g.getJDAGuild().getEmojisByName(emote.getCustomEmojiName(), false).stream())
						.filter(Objects::nonNull)
						.findFirst().orElse(null);
					
					if(foundEmoji != null) continue;
					
					GraphiteGuild guild = guilds.stream()
						.filter(g -> g.getJDAGuild().getEmojis().size() < g.getJDAGuild().getMaxEmojis())
						.findFirst().orElse(null);
					
					if(guild == null) throw new GraphiteSetupException("Failed to create emoji: No emoji guild with free slots available");
					
					try {
						Graphite.log("Creating emoji " + emote.getCustomEmojiName());
						String resPath = "/include/emoji/" + emote.getCustomEmojiName() + ".png";
						InputStream in = GraphiteSetup.class.getResourceAsStream(resPath);
						if(in == null) throw new GraphiteSetupException("Failed to find resource: " + resPath);
						byte[] imageBytes = in.readAllBytes();
						RichCustomEmoji emoji = guild.getJDAGuild().createEmoji(emote.getCustomEmojiName(), Icon.from(imageBytes)).complete();
						emote.load(emoji);
					} catch (IOException e) {
						throw new GraphiteSetupException(e);
					}
				}
			}
		}
	}

}
