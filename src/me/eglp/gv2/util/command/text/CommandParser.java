package me.eglp.gv2.util.command.text;

import java.util.ArrayList;
import java.util.List;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.command.text.argument.CommandArgument;
import me.eglp.gv2.util.command.text.argument.MentionArgument;
import me.eglp.gv2.util.mention.MentionFinder;
import me.eglp.gv2.util.mention.MentionFinder.SearchResult;

public class CommandParser {

	public static ParsedCommand parse(GraphiteGuild guild, String prefix, String commandLine) {
		String tRaw = commandLine;
		List<CommandArgument> args = new ArrayList<>();
		List<SearchResult> mentions = MentionFinder.findAllMentions(guild, tRaw);
		int tS = 0;
		while(tRaw.length() > 0) {
			if(!mentions.isEmpty()) {
				SearchResult r = mentions.get(0);
				if(r.getIndex() == tS) {
					mentions.remove(0);
					args.add(new MentionArgument(r.getFullResult(), r));
					String sS = tRaw.substring(r.getFullResult().length());
					String tSS = sS.trim();
					int l = r.getFullResult().length() + (sS.length() - tSS.length());
					tS += l;
					tRaw = tRaw.substring(l);
				}else {
					String before = tRaw.substring(0, r.getIndex() - tS);
					for(String arg : before.split(" ")) {
						if(arg.isEmpty()) continue;
						args.add(new CommandArgument(arg));
					}
					tRaw = tRaw.substring(r.getIndex() - tS);
					tS = r.getIndex();
				}
			}else {
				String quotedString = null;
				for(String arg : tRaw.split(" ")) {
					if(arg.isEmpty()) continue;
					if(quotedString != null) {
						quotedString += " " + arg;
						if(arg.endsWith("\"")) {
							args.add(new CommandArgument(quotedString.substring(0, quotedString.length() - 1)));
							quotedString = null;
						}
					}else if(arg.startsWith("\"")) {
						if(arg.endsWith("\"")) {
							args.add(new CommandArgument(arg.substring(1, arg.length() - 1)));
						}else {
							quotedString = arg.substring(1);
						}
					}else {
						args.add(new CommandArgument(arg));
					}
				}
				
				if(quotedString != null) {
					args.add(new CommandArgument(quotedString));
				}
				break;
			}
		}
		return new ParsedCommand(prefix, commandLine, args.toArray(new CommandArgument[args.size()]));
	}
	
	public static class ParsedCommand {
		
		private String prefix;
		private String raw;
		private CommandArgument[] args;
		
		public ParsedCommand(String prefix, String raw, CommandArgument[] args) {
			this.prefix = prefix;
			this.raw = raw;
			this.args = args;
		}
		
		public String getPrefix() {
			return prefix;
		}
		
		public String getRaw() {
			return raw;
		}
		
		public CommandArgument[] getArgs() {
			return args;
		}
		
	}
	
}
