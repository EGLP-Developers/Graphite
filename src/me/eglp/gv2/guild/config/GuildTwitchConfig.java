package me.eglp.gv2.guild.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.apis.twitch.GraphiteTwitchUser;
import me.eglp.gv2.util.apis.twitch.TwitchAnnouncementParameter;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.mysql.SQLTable;
import me.eglp.twitch.entity.TwitchUser;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "guilds_twitch_users",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"TwitchUserId varchar(255) NOT NULL",
		"NotificationChannel varchar(255) DEFAULT NULL",
		"NotificationMessage text DEFAULT NULL",
		"Parameters text DEFAULT NULL",
		"Color int DEFAULT NULL",
		"WasLive bool DEFAULT 0",
		"MessageId varchar(255) DEFAULT NULL",
		"PRIMARY KEY (GuildId, TwitchUserId)"
	},
	guildReference = "GuildId"
)
public class GuildTwitchConfig implements IGuildConfig {

	private GraphiteGuild guild;

	public GuildTwitchConfig(GraphiteGuild guild) {
		this.guild = guild;
	}

	@ChannelRemoveListener
	private void removeChannel(GraphiteGuildMessageChannel channel) {
		removeTwitchUsersByChannel(channel.getID());
	}

	public void setWasLive(GraphiteTwitchUser u, boolean wasLive) {
		Graphite.getMySQL().query("UPDATE guilds_twitch_users SET WasLive = ? WHERE GuildId = ? AND TwitchUserId = ?", wasLive, guild.getID(), u.getID());
	}

	public boolean getWasLive(GraphiteTwitchUser u) {
		return Graphite.getMySQL().query(Boolean.class, true, "SELECT WasLive FROM guilds_twitch_users WHERE GuildId = ? AND TwitchUserId = ?", guild.getID(), u.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load was live from MySQL", e));
	}

	public void setMessageID(GraphiteTwitchUser u, String messageID) {
		Graphite.getMySQL().query("UPDATE guilds_twitch_users SET MessageId = ? WHERE GuildId = ? AND TwitchUserId = ?", messageID, guild.getID(), u.getID());
	}

	public String getMessageID(GraphiteTwitchUser u) {
		return Graphite.getMySQL().query(String.class, null, "SELECT MessageId FROM guilds_twitch_users WHERE GuildId = ? AND TwitchUserId = ?", guild.getID(), u.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load twitch message id from MySQL", e));
	}

	public GraphiteGuild getGuild() {
		return guild;
	}

	public void setTwitchUsers(List<GraphiteTwitchUser> users) {
		Graphite.getMySQL().query("DELETE FROM guilds_twitch_users WHERE GuildId = ?", guild.getID());

		Graphite.getMySQL().run(c -> {
			try(PreparedStatement st = c.prepareStatement("INSERT INTO guilds_twitch_users(GuildId, TwitchUserId, NotificationChannel, NotificationMessage, Parameters, Color) VALUES(?, ?, ?, ?, ?, ?)")) {
				for(GraphiteTwitchUser u : users) {
					st.setString(1, guild.getID());
					st.setString(2, u.getID());
					st.setString(3, u.getNotificationChannelID());
					st.setString(4, u.getNotificationMessage());
					st.setString(5, new JSONArray(u.getParameters().stream().map(TwitchAnnouncementParameter::name).collect(Collectors.toList())).toString());
					st.setInt(6, u.getColor());
					st.addBatch();
				}
				st.executeBatch();
			}
		});
	}

	public GraphiteTwitchUser createTwitchUser(TwitchUser twitchUser, GraphiteGuildMessageChannel notificationChannel) {
		return createTwitchUser(twitchUser, notificationChannel, DefaultLocaleString.TWITCH_NOTIFICATION_DEFAULT_MESSAGE.getFallback());
	}

	public GraphiteTwitchUser createTwitchUser(TwitchUser twitchUser, GraphiteGuildMessageChannel notificationChannel, String notificationMessage) {
		GraphiteTwitchUser u = new GraphiteTwitchUser(twitchUser.getID(), twitchUser, notificationChannel.getID(), notificationMessage, new ArrayList<>(), 16711680);
		addTwitchUser(u);
		return u;
	}

	public void addTwitchUser(GraphiteTwitchUser twitchUser) {
		Graphite.getMySQL().query("INSERT INTO guilds_twitch_users(GuildId, TwitchUserId, NotificationChannel, NotificationMessage, Parameters, Color) VALUES(?, ?, ?, ?, ?, ?)",
				guild.getID(),
				twitchUser.getTwitchUser().getID(),
				twitchUser.getNotificationChannelID(),
				twitchUser.getNotificationMessage(),
				new JSONArray(twitchUser.getParameters().stream().map(TwitchAnnouncementParameter::name).collect(Collectors.toList())).toString(),
				twitchUser.getColor());
	}

	public void updateTwitchUser(GraphiteTwitchUser twitchUser) {
		Graphite.getMySQL().query("UPDATE guilds_twitch_users SET NotificationChannel = ?, NotificationMessage = ?, Parameters = ?, Color = ? WHERE GuildId = ? AND TwitchUserId = ?",
				twitchUser.getNotificationChannelID(),
				twitchUser.getNotificationMessage(),
				new JSONArray(twitchUser.getParameters().stream().map(TwitchAnnouncementParameter::name).toList()).toString(),
				twitchUser.getColor(),
				guild.getID(),
				twitchUser.getTwitchUser().getID());
	}

	public void removeTwitchUser(GraphiteTwitchUser twitchUser) {
		removeTwitchUserRaw(twitchUser.getID());
	}

	public void removeTwitchUserRaw(String twitchID) {
		Graphite.getMySQL().query("DELETE FROM guilds_twitch_users WHERE GuildId = ? AND TwitchUserId = ?", guild.getID(), twitchID);
	}

	public void removeTwitchUsersByChannel(String channelID) {
		Graphite.getMySQL().query("DELETE FROM guilds_twitch_users WHERE GuildId = ? AND NotificationChannel = ?", guild.getID(), channelID);
	}

	public List<GraphiteTwitchUser> getTwitchUsers() {
		List<String> userIds = Graphite.getMySQL().queryArray(String.class, "SELECT TwitchUserId FROM guilds_twitch_users WHERE GuildId = ?", guild.getID()).get();

		List<TwitchUser> usersCache = new ArrayList<>();
		while(!userIds.isEmpty()) {
			List<String> subList = userIds.subList(0, Math.min(100, userIds.size()));
			usersCache.addAll(Graphite.getTwitch().getTwitchAPI().getUsersByIDs(subList.toArray(String[]::new)));
			subList.clear();
		}

		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement p = con.prepareStatement("SELECT TwitchUserId, NotificationChannel, NotificationMessage, Parameters, Color FROM guilds_twitch_users WHERE GuildId = ?")) {
				p.setString(1, guild.getID());
				try(ResultSet r = p.executeQuery()) {
					List<GraphiteTwitchUser> users = new ArrayList<>();
					while(r.next()) {
						String twitchUserID = r.getString("TwitchUserId");
						GraphiteTwitchUser tu = new GraphiteTwitchUser(
								twitchUserID,
								usersCache.stream().filter(us -> us.getID().equals(twitchUserID)).findFirst().orElse(null),
								r.getString("NotificationChannel"),
								r.getString("NotificationMessage"),
								new JSONArray(r.getString("Parameters")).stream()
									.map(v -> {
										try {
											return TwitchAnnouncementParameter.valueOf((String) v);
										}catch(IllegalArgumentException e) {
											return null;
										}
									})
									.filter(Objects::nonNull)
									.collect(Collectors.toList()),
									r.getInt("Color"));

						if(!tu.isValid()) {
							removeTwitchUser(tu);
							continue;
						}

						users.add(tu);
					}
					return users;
				}
			}
		}).orElse(Collections.emptyList());
	}

	public GraphiteTwitchUser getTwitchUserByName(String twitchName) {
		return getTwitchUsers().stream().filter(tu -> tu.getTwitchUser().getLogin().equalsIgnoreCase(twitchName)).findFirst().orElse(null);
	}

	public GraphiteTwitchUser getTwitchUserByID(String twitchID) {
		return getTwitchUsers().stream().filter(tu -> tu.getTwitchUser().getID().equalsIgnoreCase(twitchID)).findFirst().orElse(null);
	}

	public void removeTwitchUserByName(String twitchName) {
		removeTwitchUser(getTwitchUserByName(twitchName));
	}

	public void removeTwitchUserByID(String twitchID) {
		removeTwitchUser(getTwitchUserByID(twitchID));
	}

}
