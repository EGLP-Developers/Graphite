package me.eglp.gv2.util.mention;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.Graphite;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

public class MentionFinder {

	public static final Pattern
		DISCORD_MENTION_PATTERN = Pattern.compile("(?<fullresult>(?<ctype>[:@])\\<(?:(?<cid>\\d++)|(?<cname>.+?))\\>|@?everyone|@here|(?:\\<(?<result>(?<type>(?:@|@&|@!|#|:(?<ename>[^:]+):))(?<id>\\d++))\\>))"),
		USER_HASH_PATTERN = Pattern.compile("(?<name>.+?)#(?<hash>(?:\\d){4})");

	public static SearchResult findFirstMention(@Nullable GraphiteGuild guild, String raw) {
		return findFirstMention(guild, raw, 0);
	}


	public static SearchResult findFirstMention(@Nullable GraphiteGuild guild, String raw, int startIndex) {
		String oRaw = raw;
		raw = raw.substring(startIndex);
		Matcher m;
		if((m = DISCORD_MENTION_PATTERN.matcher(raw)).find()) {
			String result = m.group("result");
			String fullResult = m.group("fullresult");
			String before = oRaw.substring(0, m.start() + startIndex);
			String after = oRaw.substring(m.end() + startIndex);
			int index = m.start() + startIndex;
			if(fullResult.equals("@everyone") || fullResult.equals("everyone")) {
				return new SearchResult(new GraphiteEveryoneMention(), fullResult, fullResult, index, before, after);
			}else if(fullResult.equals("@here")) {
				return new SearchResult(new GraphiteHereMention(), fullResult, fullResult, index, before, after);
			}else if(m.group("ctype") != null) {
				String cType = m.group("ctype");
				switch(cType) {
					case ":":
					{
						if(m.group("cname") != null) {
							String catName = m.group("cname");
							return new SearchResult(new GraphiteCategoryMention(guild == null ? null : guild.getCategoriesByName(catName, true)), catName, fullResult, index, before, after);
						}else {
							String catID = m.group("cid");
							return new SearchResult(new GraphiteCategoryMention(guild == null ? null : guild.getCategoryByID(catID)), catID, fullResult, index, before, after);
						}
					}
					case "@":
					{
						if(m.group("cname") != null) {
							String cName = m.group("cname");
							Matcher uM = USER_HASH_PATTERN.matcher(cName);
							if(uM.matches()) {
								return new SearchResult(new GraphiteUserMention(Graphite.getUser(uM.group("name"), uM.group("hash"))), result, fullResult, index, before, after);
							}else {
								return new SearchResult(new GraphiteUserMention(Graphite.getUsersByName(cName)), result, fullResult, index, before, after);
							}
						}else {
							String vID = m.group("cid");
							return new SearchResult(new GraphiteUserMention(Graphite.getUser(vID)), vID, fullResult, index, before, after);
						}
					}
				}
			}else {
				switch(m.group("type")) {
					case "@":
					case "@!":
						return new SearchResult(new GraphiteUserMention(Graphite.getUser(m.group("id"))), result, fullResult, index, before, after);
					case "@&":
						return new SearchResult(new GraphiteRoleMention(guild == null ? null : guild.getRoleByID(m.group("id"))), result, fullResult, index, before, after);
					case "#":
					{
						if(guild == null)
							return new SearchResult(new GraphiteTextChannelMention(null), result, fullResult, index, before, after);
						GuildChannel ch = guild.getJDAGuild().getGuildChannelById(m.group("id"));
						if(ch == null) return new SearchResult(new GraphiteTextChannelMention(null), result, fullResult, index, before, after);
						switch(ch.getType()) {
							case TEXT:
								return new SearchResult(new GraphiteTextChannelMention(guild.getTextChannel((TextChannel) ch)), result, fullResult, index, before, after);
							case VOICE:
								return new SearchResult(new GraphiteVoiceChannelMention(guild.getVoiceChannel((VoiceChannel) ch)), result, fullResult, index, before, after);
							case CATEGORY:
								return new SearchResult(new GraphiteCategoryMention(guild.getCategory((Category) ch)), result, fullResult, index, before, after);
							case NEWS:
								return new SearchResult(new GraphiteNewsChannelMention(guild.getNewsChannel((NewsChannel) ch)), result, fullResult, index, before, after);
							case STAGE:
								return new SearchResult(new GraphiteStageChannelMention(guild.getStageChannel((StageChannel) ch)), result, fullResult, index, before, after);
							default:
								return new SearchResult(new GraphiteTextChannelMention(null), result, fullResult, index, before, after);
						}
					}
				}
				if(m.group("type").startsWith(":")) {
					String eName = m.group("ename");
					return new SearchResult(new GraphiteEmoteMention(eName, m.group("id")), result, fullResult, index, before, after);
				}
			}
		}
		return null;
	}

	public static List<SearchResult> findAllMentions(@Nullable GraphiteGuild guild, String raw) {
		List<SearchResult> results = new ArrayList<>();
		SearchResult r;
		int sI = 0;
		while((r = findFirstMention(guild, raw, sI)) != null) {
			results.add(r);
			sI = r.getIndex() + r.getFullResult().length();
		}
		return results;
	}

	public static class SearchResult {

		private GraphiteMention mention;
		private int index;
		private String result, fullResult, before, after;

		public SearchResult(GraphiteMention mention, String result, String fullResult, int index, String before, String after) {
			this.mention = mention;
			this.result = result;
			this.fullResult = fullResult;
			this.index = index;
			this.before = before;
			this.after = after;
		}

		public GraphiteMention getMention() {
			return mention;
		}

		public int getIndex() {
			return index;
		}

		public String getResult() {
			return result;
		}

		public String getFullResult() {
			return fullResult;
		}

		public String getBefore() {
			return before;
		}

		public String getAfter() {
			return after;
		}

	}

}
