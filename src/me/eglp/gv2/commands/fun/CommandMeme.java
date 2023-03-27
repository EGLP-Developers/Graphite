package me.eglp.gv2.commands.fun;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import me.eglp.reddit.entity.data.Link;
import me.mrletsplay.mrcore.misc.QuickMap;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandMeme extends Command{
	
	private Map<String, String> moreMemes = new QuickMap<String, String>()
			.put("dogs", "dogmemes")
			.put("cats", "catmemes")
			.put("programming", "programmerhumor")
			.makeHashMap();

	public CommandMeme() {
		super(GraphiteModule.FUN, CommandCategory.FUN, "meme");
		setDescription(DefaultLocaleString.COMMAND_MEME_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_MEME_USAGE);
	}

	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		String subreddit = "memes";
		
		if(event.hasOption("subreddit")) {
			String m = (String) event.getOption("subreddit");
			if(!moreMemes.containsKey(m)) {
				DefaultMessage.COMMAND_MEME_AVAILABLE.reply(event, "meme_categories", moreMemes.keySet().stream().collect(Collectors.joining(", ")));
				return;
			}
			
			subreddit = moreMemes.get(m);
		}
		
		Link s = Graphite.getReddit().getRedditAPI().getRandomPost(subreddit);
		EmbedBuilder b = new EmbedBuilder();
		b.setDescription(s.getTitle());
		b.setImage(s.getURL());
		event.reply(b.build());
	}

	@Override
	public List<OptionData> getOptions() {
		OptionData d = new OptionData(OptionType.STRING, "subreddit", "Choose a subreddit", false);
		moreMemes.keySet().stream().forEach(m -> d.addChoice(m, m));
		return Arrays.asList(d);
	}

}
