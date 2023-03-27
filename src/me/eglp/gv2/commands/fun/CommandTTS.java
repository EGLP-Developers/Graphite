package me.eglp.gv2.commands.fun;

import java.util.Arrays;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.music.GraphiteTrack;
import me.eglp.gv2.util.versioning.Beta;
import me.mrletsplay.mrcore.http.HttpUtils;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

// NONBETA: poopy implementation, fix
public class CommandTTS extends Command {
	
	public CommandTTS() {
		super(GraphiteModule.FUN, CommandCategory.FUN, "tts");
		setDescription("amogus");
		setUsage("amogus");
	}

	@Beta
	@Override
	public void action(CommandInvokedEvent event) {
		String text = (String) event.getOption("text");
		String lang = event.hasOption("language") ? (String) event.getOption("language") : "en-US";
		System.out.println(text + "/" + lang);
//		try {
//			Process b = new ProcessBuilder("pico2wave", "-w=/var/local/pico2wave.wav", "-l", lang, text)
//					.redirectOutput(new File("/home/mr/Desktop/out.wav"))
//					.start();
//			b.waitFor();
			
//			HttpRequest r = HttpRequest.createGet("http://translate.google.com/translate_tts")
//				.addQueryParameter("ie", "UTF-8")
//				.addQueryParameter("client", "tw-ob")
//				.addQueryParameter("tl", "en")
//				.addQueryParameter("q", text);
//			
//			r.execute().transferTo(new File("/home/mr/Desktop/out.wav"));
			
			System.out.println("DONE");
			GraphiteGuild g = event.getGuild();
//			LocalAudioSourceManager man = new LocalAudioSourceManager();
			HttpAudioSourceManager man = new HttpAudioSourceManager();
			// https://translate.google.com/translate_tts?ie=UTF-8&tl=de-DE&client=tw-ob&q=Bats%C4%B1n+bu+d%C3%BCnya+bitsin+bu+r%C3%BCya
//			AudioTrack tr = (AudioTrack) man.loadItem(Graphite.getAudioPlayerManager(), new AudioReference("/home/mr/Desktop/out.wav", "Amogus"));
			String url = "https://translate.google.com/translate_tts?ie=UTF-8&tl=" + HttpUtils.urlEncode(lang) + "&client=tw-ob&q=" + HttpUtils.urlEncode(text);
			System.out.println(url);
			AudioTrack tr = (AudioTrack) man.loadItem(Graphite.getAudioPlayerManager(), new AudioReference(url, "Amogus"));
			System.out.println(tr);
			g.getMusic().queue(new GraphiteTrack(tr));
			g.getMusic().join(event.getMember().getCurrentAudioChannel());
			event.reply("wow");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.STRING, "text", "The text to speak", true),
				new OptionData(OptionType.STRING, "language", "The language of the text", false)
					.addChoice("English (US)", "en-US")
					.addChoice("English (GB)", "en-GB")
					.addChoice("German", "de-DE")
					.addChoice("Spanish", "es-ES")
					.addChoice("French", "fr-FR")
					.addChoice("Italian", "it-IT")
			);	
	}

}
