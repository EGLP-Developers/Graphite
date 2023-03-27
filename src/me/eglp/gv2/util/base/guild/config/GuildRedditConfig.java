package me.eglp.gv2.util.base.guild.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.apis.reddit.GraphiteSubreddit;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.util.mysql.SQLTable;
import me.eglp.reddit.entity.data.Subreddit;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "guilds_subreddits",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"Subreddit varchar(255) NOT NULL",
		"NotificationChannel varchar(255) DEFAULT NULL",
		"Color int DEFAULT NULL",
		"LastPostId varchar(255) DEFAULT NULL",
		"PRIMARY KEY (GuildId, Subreddit)"
	},
	guildReference = "GuildId"
)
public class GuildRedditConfig implements IGuildConfig {

	private GraphiteGuild guild;
	
	public GuildRedditConfig(GraphiteGuild guild) {
		this.guild = guild;
	}
	
	public GraphiteGuild getGuild() {
		return guild;
	}
	
	@ChannelRemoveListener
	private void removeChannel(GraphiteGuildMessageChannel channel) {
		removeSubredditsByChannel(channel.getID());
	}
	
	public void setSubreddits(List<GraphiteSubreddit> subreddits) {
		Graphite.getMySQL().query("DELETE FROM guilds_subreddits WHERE GuildId = ?", guild.getID());
		
		Graphite.getMySQL().run(c -> {
			try(PreparedStatement st = c.prepareStatement("INSERT INTO guilds_subreddits(GuildId, Subreddit, NotificationChannel, Color) VALUES(?, ?, ?, ?)")) {
				for(GraphiteSubreddit sr : subreddits) {
					st.setString(1, guild.getID());
					st.setString(2, sr.getSubreddit());
					st.setString(3, sr.getNotificationChannelID());
					st.setInt(4, sr.getColor());
					st.addBatch();
				}
				st.executeBatch();
			}
		});
	}
	
	public void setLastPostID(GraphiteSubreddit u, String postID) {
		Graphite.getMySQL().query("UPDATE guilds_subreddits SET LastPostId = ? WHERE GuildId = ? AND Subreddit = ?", postID, guild.getID(), u.getSubreddit());
	}
	
	public String getLastPostID(GraphiteSubreddit u) {
		return Graphite.getMySQL().query(String.class, null, "SELECT LastPostId FROM guilds_subreddits WHERE GuildId = ? AND Subreddit = ?", guild.getID(), u.getSubreddit())
				.orElseThrowOther(e -> new FriendlyException("Failed to load last post id from MySQL", e));
	}
	
	public GraphiteSubreddit createSubreddit(String subredditName, Subreddit subreddit, GraphiteGuildMessageChannel notificationChannel) {
		GraphiteSubreddit sr = new GraphiteSubreddit(subredditName, subreddit, notificationChannel.getID(), 16711680);
		addSubreddit(sr);
		return sr;
	}
	
	public void addSubreddit(GraphiteSubreddit subreddit) {
		Graphite.getMySQL().query("INSERT INTO guilds_subreddits(GuildId, Subreddit, NotificationChannel, Color) VALUES(?, ?, ?, ?)",
				guild.getID(),
				subreddit.getSubreddit(),
				subreddit.getNotificationChannelID(),
				subreddit.getColor());
	}
	
	public void updateSubreddit(GraphiteSubreddit subreddit) {
		Graphite.getMySQL().query("UPDATE guilds_subreddits SET NotificationChannel = ?, Color = ? WHERE GuildId = ? AND Subreddit = ?",
				subreddit.getNotificationChannelID(),
				subreddit.getColor(),
				guild.getID(),
				subreddit.getSubreddit());
	}
	
	public void removeSubreddit(GraphiteSubreddit subreddit) {
		removeSubredditRaw(subreddit.getSubreddit());
	}
	
	public void removeSubredditRaw(String subreddit) {
		Graphite.getMySQL().query("DELETE FROM guilds_subreddits WHERE GuildId = ? AND Subreddit = ?", guild.getID(), subreddit);
	}
	
	public void removeSubredditByName(String subreddit) {
		Graphite.getMySQL().query("DELETE FROM guilds_subreddits WHERE GuildId = ? AND Subreddit = ?", guild.getID(), subreddit);
	}
	
	public void removeSubredditsByChannel(String channelID) {
		Graphite.getMySQL().query("DELETE FROM guilds_subreddits WHERE GuildId = ? AND NotificationChannel = ?", guild.getID(), channelID);
	}
	
	public List<GraphiteSubreddit> getSubreddits() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement p = con.prepareStatement("SELECT Subreddit, NotificationChannel, Color FROM guilds_subreddits WHERE GuildId = ?")) {
				p.setString(1, guild.getID());
				try(ResultSet r = p.executeQuery()) {
					List<GraphiteSubreddit> subreddits = new ArrayList<>();
					while(r.next()) {
						GraphiteSubreddit sr = new GraphiteSubreddit(
								r.getString("Subreddit"),
								Graphite.getReddit().getRedditAPI().getAbout(r.getString("Subreddit")),
								r.getString("NotificationChannel"),
								r.getInt("Color"));
						
						if(!sr.isValid()) {
							removeSubreddit(sr);
							continue;
						}
						
						subreddits.add(sr);
					}
					return subreddits;
				}
			}
		}).orElse(Collections.emptyList());
	}
	
	public GraphiteSubreddit getSubredditByName(String subreddit) {
		return getSubreddits().stream().filter(tu -> tu.getSubreddit().equalsIgnoreCase(subreddit)).findFirst().orElse(null);
	}
	
}
