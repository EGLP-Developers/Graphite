package me.eglp.gv2.commands.reddit;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.apis.reddit.GraphiteSubreddit;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.reddit.entity.data.Subreddit;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandReddit extends ParentCommand{
	
	public CommandReddit() {
		super(GraphiteModule.REDDIT, CommandCategory.REDDIT, "reddit");
		setDescription(DefaultLocaleString.COMMAND_REDDIT_DESCRIPTION);
		
		addSubCommand(new Command(this, "channel") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				String subreddit = (String) event.getOption("subreddit");
				GraphiteSubreddit sr = g.getRedditConfig().getSubredditByName(subreddit);
				if(sr == null) {
					DefaultMessage.COMMAND_REDDIT_INVALID_SUBREDDIT.reply(event);
					return;
				}
				
				GraphiteTextChannel tex = (GraphiteTextChannel) event.getOption("channel");
				if(!tex.canWrite()) {
					DefaultMessage.ERROR_BOT_CANNOT_WRITE.reply(event);
					return;
				}
				
				sr.setNotificationChannel(tex);
				g.getRedditConfig().updateSubreddit(sr);
				DefaultMessage.COMMAND_REDDIT_SET_CHANNEL.reply(event, 
						"channel", tex.getName());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "subreddit", "The subreddit you want to change the notifications channel", true),
						new OptionData(OptionType.CHANNEL, "channel", "The channel where the subreddits should to be posted", true)
					);
			}
			
		})
		.setDescription(DefaultLocaleString.COMMAND_REDDIT_CHANNEL_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_REDDIT_CHANNEL_USAGE)
		.setPermission(DefaultPermissions.REDDIT_CHANNEL);
		
		addSubCommand(new Command(this, "add") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				String subreddit = (String) event.getOption("subreddit");
				GraphiteSubreddit sr = g.getRedditConfig().getSubredditByName(subreddit);
				if(sr != null) {
					DefaultMessage.COMMAND_REDDIT_SUBREDDIT_ALREADY_ADDED.reply(event);
					return;
				}
				
				Subreddit r = Graphite.getReddit().getRedditAPI().getAbout(subreddit);
				if(r == null) {
					DefaultMessage.COMMAND_REDDIT_INVALID_SUBREDDIT.reply(event);
					return;
				}
				
				GraphiteTextChannel tc = (GraphiteTextChannel) event.getOption("channel");
				g.getRedditConfig().createSubreddit(subreddit, r, tc);
				DefaultMessage.COMMAND_REDDIT_SUBREDDIT_ADDED.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "subreddit", "The subreddit you want to receive notifications for", true),
						new OptionData(OptionType.CHANNEL, "channel", "The channel where the subreddits should to be posted", true).setChannelTypes(ChannelType.TEXT)
					);
			}
			
		})
		.setDescription(DefaultLocaleString.COMMAND_REDDIT_ADD_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_REDDIT_ADD_USAGE)
		.setPermission(DefaultPermissions.REDDIT_SUBREDDIT_ADD);
		
		addSubCommand(new Command(this, "remove") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				String subreddit = (String) event.getOption("subreddit");
				GraphiteSubreddit sr = g.getRedditConfig().getSubredditByName(subreddit);
				if(sr == null) {
					DefaultMessage.COMMAND_REDDIT_INVALID_SUBREDDIT.reply(event);
					return;
				}
				
				g.getRedditConfig().removeSubreddit(sr);
				DefaultMessage.COMMAND_REDDIT_SUBREDDIT_REMOVED.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "subreddit", "The subreddit you want to remove", true)
					);
			}
			
		})
		.setDescription(DefaultLocaleString.COMMAND_REDDIT_REMOVE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_REDDIT_REMOVE_USAGE)
		.setPermission(DefaultPermissions.REDDIT_SUBREDDIT_REMOVE);
	}

}
