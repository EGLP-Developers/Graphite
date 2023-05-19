package me.eglp.gv2.guild.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.apis.twitter.GraphiteTwitterUser;
import me.eglp.gv2.util.mysql.SQLTable;
import me.eglp.twitter.entity.TwitterUser;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "guilds_twitter_users",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"TwitterUserId varchar(255) NOT NULL",
		"NotificationChannel varchar(255) DEFAULT NULL",
		"Color int DEFAULT NULL",
		"LastTweetId varchar(255) DEFAULT NULL",
		"PRIMARY KEY (GuildId, TwitterUserId)"
	},
	guildReference = "GuildId"
)
public class GuildTwitterConfig implements IGuildConfig {

	private GraphiteGuild guild;

	public GuildTwitterConfig(GraphiteGuild guild) {
		this.guild = guild;
	}

	@ChannelRemoveListener
	private void removeChannel(GraphiteGuildMessageChannel channel) {
		removeTwitterUsersByChannel(channel.getID());
	}

	public void setLastTweetID(GraphiteTwitterUser u, String tweetID) {
		Graphite.getMySQL().query("UPDATE guilds_twitter_users SET LastTweetId = ? WHERE GuildId = ? AND TwitterUserId = ?", tweetID, guild.getID(), u.getID());
	}

	public String getLastTweetID(GraphiteTwitterUser u) {
		return Graphite.getMySQL().query(String.class, null, "SELECT LastTweetId FROM guilds_twitter_users WHERE GuildId = ? AND TwitterUserId = ?", guild.getID(), u.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load last tweet id from MySQL", e));
	}

	public GraphiteGuild getGuild() {
		return guild;
	}

	public void setTwitterUsers(List<GraphiteTwitterUser> users) {
		Graphite.getMySQL().query("DELETE FROM guilds_twitter_users WHERE GuildId = ?", guild.getID());

		Graphite.getMySQL().run(c -> {
			try(PreparedStatement st = c.prepareStatement("INSERT INTO guilds_twitter_users(GuildId, TwitterUserId, NotificationChannel, Color) VALUES(?, ?, ?, ?)")) {
				for(GraphiteTwitterUser u : users) {
					st.setString(1, guild.getID());
					st.setString(2, u.getID());
					st.setString(3, u.getNotificationChannelID());
					st.setInt(4, u.getColor());
					st.addBatch();
				}
				st.executeBatch();
			}
		});
	}

	public GraphiteTwitterUser createTwitterUser(TwitterUser twitterUser, GraphiteGuildMessageChannel notificationChannel) {
		GraphiteTwitterUser u = new GraphiteTwitterUser(twitterUser.getID(), twitterUser, notificationChannel.getID(), 1940464);
		addTwitterUser(u);
		return u;
	}

	public void addTwitterUser(GraphiteTwitterUser twitterUser) {
		Graphite.getMySQL().query("INSERT INTO guilds_twitter_users(GuildId, TwitterUserId, NotificationChannel, Color) VALUES(?, ?, ?, ?)",
				guild.getID(),
				twitterUser.getID(),
				twitterUser.getNotificationChannelID(),
				twitterUser.getColor());
	}

	public void updateTwitterUser(GraphiteTwitterUser twitterUser) {
		Graphite.getMySQL().query("UPDATE guilds_twitter_users SET NotificationChannel = ?, Color = ? WHERE GuildId = ? AND TwitterUserId = ?",
				twitterUser.getNotificationChannelID(),
				twitterUser.getColor(),
				guild.getID(),
				twitterUser.getID());
	}

	public void removeTwitterUser(GraphiteTwitterUser twitterUser) {
		removeTwitterUserRaw(twitterUser.getID());
	}

	public void removeTwitterUserRaw(String twitterUser) {
		Graphite.getMySQL().query("DELETE FROM guilds_twitter_users WHERE GuildId = ? AND TwitterUserId = ?", guild.getID(), twitterUser);
	}

	public void removeTwitterUsersByChannel(String channelID) {
		Graphite.getMySQL().query("DELETE FROM guilds_twitter_users WHERE GuildId = ? AND NotificationChannel = ?", guild.getID(), channelID);
	}

	public List<GraphiteTwitterUser> getTwitterUsers() {
		List<String> userIds = Graphite.getMySQL().queryArray(String.class, "SELECT TwitterUserId FROM guilds_twitter_users WHERE GuildId = ?", guild.getID()).get();

		List<TwitterUser> usersCache = new ArrayList<>();
		while(!userIds.isEmpty()) {
			List<String> subList = userIds.subList(0, Math.min(100, userIds.size()));
			usersCache.addAll(Graphite.getTwitter().getTwitterAPI().getUsersByIDs(subList));
			subList.clear();
		}

		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement p = con.prepareStatement("SELECT TwitterUserId, NotificationChannel, Color FROM guilds_twitter_users WHERE GuildId = ?")) {
				p.setString(1, guild.getID());
				try(ResultSet r = p.executeQuery()) {
					List<GraphiteTwitterUser> users = new ArrayList<>();
					while(r.next()) {
						String twitterUserID = r.getString("TwitterUserId");
						GraphiteTwitterUser tu = new GraphiteTwitterUser(
								twitterUserID,
								usersCache.stream().filter(us -> us.getID().equals(twitterUserID)).findFirst().orElse(null),
								r.getString("NotificationChannel"),
								r.getInt("Color"));

						if(!tu.isValid()) {
							removeTwitterUser(tu);
							continue;
						}

						users.add(tu);
					}
					return users;
				}
			}
		}).orElse(Collections.emptyList());
	}

	public GraphiteTwitterUser getTwitterUserByName(String twitterName) {
		return getTwitterUsers().stream().filter(tu -> tu.getTwitterUser().getUsername().equalsIgnoreCase(twitterName)).findFirst().orElse(null);
	}

	public GraphiteTwitterUser getTwitterUserByID(String twitterID) {
		return getTwitterUsers().stream().filter(tu -> tu.getID().equalsIgnoreCase(twitterID)).findFirst().orElse(null);
	}

	public void removeTwitterUserByName(String twitterName) {
		removeTwitterUser(getTwitterUserByName(twitterName));
	}

	public void removeTwitterUserByID(String twitterID) {
		removeTwitterUser(getTwitterUserByID(twitterID));
	}

}
