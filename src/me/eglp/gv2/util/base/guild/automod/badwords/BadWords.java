package me.eglp.gv2.util.base.guild.automod.badwords;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.automod.AutoModMessageActionHandler;
import me.eglp.gv2.util.base.guild.automod.GuildAutoModSettings;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class BadWords implements AutoModMessageActionHandler<BadWordsSettings> {

	@Override
	public BadWordsSettings getRelevantSettings(GuildAutoModSettings settings) {
		return settings.getBadWordsSettings();
	}

	@Override
	public void takeAction(MessageReceivedEvent event, BadWordsSettings settings) {
		List<String> badWords = new ArrayList<>(settings.getBadWords());
		
		String text = event.getMessage().getContentRaw().toLowerCase();
		if(settings.isNormalizeText()) {
			text = normalize(text);
			badWords = badWords.stream()
					.map(w -> normalize(w))
					.collect(Collectors.toList());
		}
		
		final String fText = text;
		
		boolean subWordMatches = false;
		if(settings.isSubwordMatches()) {
			subWordMatches = badWords.stream().anyMatch(w -> fText.contains(w));
		}else {
			subWordMatches = Arrays.stream(text.split(" ")).anyMatch(badWords::contains);
		}
		
		if(subWordMatches) {
			if(settings.getAction().isDelete()) event.getMessage().delete().queue();
			if(settings.getAction().isWarn()) {
				settings.sendWarningMessage(Graphite.getMessageChannel(event.getChannel()), Graphite.getMember(event.getMember()));
			}
			if(settings.isPunishable()) settings.addInfraction(Graphite.getMember(event.getMember()));
		}
	}
	
	private String normalize(String str) {
		return Normalizer.normalize(str, Normalizer.Form.NFKD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
	
}
