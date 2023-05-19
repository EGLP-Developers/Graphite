package me.eglp.gv2.guild.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.poll.GuildPoll;
import me.eglp.gv2.guild.poll.PollOption;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.SerializationOption;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "guilds_polls",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"`Id` varchar(255) NOT NULL",
		"ChannelId varchar(255) NOT NULL",
		"MessageId varchar(255) NOT NULL",
		"Question text NOT NULL",
		"ExpiresAt bigint DEFAULT NULL",
		"AllowMultipleVotes bool DEFAULT 0",
		"Options text DEFAULT NULL",
		"PRIMARY KEY (GuildId, `Id`)"
	},
	guildReference = "GuildId"
)
@SQLTable(
	name = "guilds_poll_votes",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"PollId varchar(255) NOT NULL",
		"UserId varchar(255) NOT NULL",
		"Option int NOT NULL",
		"PRIMARY KEY (GuildId, PollId, UserId, Option)"
	},
	guildReference = "GuildId"
)
public class GuildPollsConfig {

	private GraphiteGuild guild;

	public GuildPollsConfig(GraphiteGuild guild) {
		this.guild = guild;
	}

	public void init() {
		getPolls().forEach(p -> p.load());
	}

	public void savePoll(GuildPoll poll) {
		JSONArray options = poll.getOptions().stream()
				.map(o -> o.toJSON(SerializationOption.DONT_INCLUDE_CLASS))
				.collect(Collectors.toCollection(JSONArray::new));
		Graphite.getMySQL().query("INSERT INTO guilds_polls(GuildId, `Id`, ChannelId, MessageId, Question, ExpiresAt, AllowMultipleVotes, Options) VALUES(?, ?, ?, ?, ?, ?, ?, ?)",
				guild.getID(),
				poll.getID(),
				poll.getChannelID(),
				poll.getMessageID(),
				poll.getQuestion(),
				poll.getExpiresAt(),
				poll.isAllowMultipleVotes(),
				options.toString());
	}

	public void removePoll(String pollID) {
		Graphite.getMySQL().query("DELETE FROM guilds_polls WHERE GuildId = ? AND `Id` = ?",
				guild.getID(),
				pollID);
		Graphite.getMySQL().query("DELETE FROM guilds_poll_votes WHERE GuildId = ? AND PollId = ?",
				guild.getID(),
				pollID);
	}

	public List<GuildPoll> getPolls() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_polls WHERE GuildId = ?")) {
				s.setString(1, guild.getID());
				try(ResultSet r = s.executeQuery()) {
					List<GuildPoll> polls = new ArrayList<>();
					while(r.next()) {
						polls.add(getPoll(r));
					}
					return polls;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve polls from MySQL", e));
	}

	public GuildPoll getPoll(String pollID) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_polls WHERE GuildId = ? AND `Id` = ?")) {
				s.setString(1, guild.getID());
				s.setString(2, pollID);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return null;
					return getPoll(r);
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve poll from MySQL", e));
	}

	private GuildPoll getPoll(ResultSet r) throws SQLException {
		return new GuildPoll(guild, r.getString("Id"), r.getString("ChannelId"), r.getString("MessageId"), r.getString("Question"), r.getLong("ExpiresAt"), r.getBoolean("AllowMultipleVotes"), new JSONArray(r.getString("Options")).stream()
				.map(o -> JSONConverter.decodeObject((JSONObject) o, PollOption.class))
				.collect(Collectors.toList()));
	}

	public void addPollVote(String pollID, String userID, int option) {
		Graphite.getMySQL().query("INSERT INTO guilds_poll_votes(GuildId, PollId, UserId, Option) VALUES(?, ?, ?, ?)",
				guild.getID(),
				pollID,
				userID,
				option);
	}

	public boolean hasVoted(String pollID, String userID, int option) {
		return Graphite.getMySQL().query(Integer.class, 0, "SELECT COUNT(UserId) FROM guilds_poll_votes WHERE GuildId = ? AND PollId = ? AND UserId = ? AND Option = ?",
				guild.getID(),
				pollID,
				userID,
				option).orElseThrowOther(e -> new FriendlyException("Failed to retrieve user poll votes from MySQL", e)) > 0;
	}

	public boolean hasVoted(String pollID, String userID) {
		return Graphite.getMySQL().query(Integer.class, 0, "SELECT COUNT(UserId) FROM guilds_poll_votes WHERE GuildId = ? AND PollId = ? AND UserId = ?",
				guild.getID(),
				pollID,
				userID).orElseThrowOther(e -> new FriendlyException("Failed to retrieve user poll votes from MySQL", e)) > 0;
	}

	public Map<Integer, Integer> getPollResults(String pollID) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT Option, Count(Option) AS Count FROM guilds_poll_votes WHERE GuildId = ? AND PollId = ? GROUP BY Option")) {
				s.setString(1, guild.getID());
				s.setString(2, pollID);
				try(ResultSet r = s.executeQuery()) {
					Map<Integer, Integer> results = new HashMap<>();
					while(r.next()) {
						results.put(r.getInt("Option"), r.getInt("Count"));
					}
					return results;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve poll results from MySQL", e));
	}

	public int getVoteCount(String pollID) {
		return Graphite.getMySQL().query(Integer.class, 0, "SELECT Count(Option) AS Count FROM guilds_poll_votes WHERE GuildId = ? AND PollId = ?",
				guild.getID(),
				pollID).orElseThrowOther(e -> new FriendlyException("Failed to retrieve poll vote count from MySQL", e));
	}

	public int getVoteUserCount(String pollID) {
		return Graphite.getMySQL().query(Integer.class, 0, "SELECT Count(DISTINCT UserId) AS Count FROM guilds_poll_votes WHERE GuildId = ? AND PollId = ?",
				guild.getID(),
				pollID).orElseThrowOther(e -> new FriendlyException("Failed to retrieve poll vote count from MySQL", e));
	}

}
