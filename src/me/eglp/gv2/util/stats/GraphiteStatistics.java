package me.eglp.gv2.util.stats;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.stats.element.StatisticsElementPointFrequency;
import me.eglp.gv2.util.stats.element.StatisticsElementTimeframe;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class GraphiteStatistics {

	public static final int STATISTICS_INTERVAL = 20 * 60 * 1000;

	private static final Comparator<StatisticValue> STATISTICS_SORT =
			Comparator.<StatisticValue>comparingInt(v -> v.getValue()).reversed() // First sort by highest value
			.thenComparing(Comparator.<StatisticValue>comparingLong(v -> v.getTimestamp()).reversed()); // Then, for equal values, use the newest timestamp

	private static final Comparator<PartialStatistics> PARTIAL_STATISTICS_SORT =
			Comparator.<PartialStatistics>comparingDouble(v -> v.sortValue()).reversed() // First sort by highest total/average value
			.thenComparing(Comparator.<PartialStatistics>comparingLong(v -> v.getNewestTimestamp()).reversed()); // Then, for equal values, use the one that contains the newest timestamp

	static {
		Graphite.getMySQL().createTable("guilds_statistics")
			.guildReference("GuildId")
			.charset("utf8mb4")
			.collation("utf8mb4_bin")
			.addColumns(
					"GuildId varchar(255) NOT NULL",
					"Statistic varchar(255) NOT NULL",
					"Category varchar(255) NOT NULL",
					"Timestamp bigint NOT NULL",
					"Value int NOT NULL",
					"PRIMARY KEY(GuildId, Statistic, Category, Timestamp)")
			.create();
	}

	private Map<GraphiteStatistic, CumulativeStatistics> cumulativeStatistics;

	public GraphiteStatistics() {
		this.cumulativeStatistics = new HashMap<>();
	}

	public void incrementCumulativeStatistic(GraphiteGuild guild, GraphiteStatistic statistic, String category) {
		CumulativeStatistics s = cumulativeStatistics.get(statistic);
		if(s == null) {
			s = new CumulativeStatistics();
			cumulativeStatistics.put(statistic, s);
		}
		s.incrementStatistic(guild, category);
	}

	public void decrementCumulativeStatistic(GraphiteGuild guild, GraphiteStatistic statistic, String category) {
		CumulativeStatistics s = cumulativeStatistics.get(statistic);
		if(s == null) {
			s = new CumulativeStatistics();
			cumulativeStatistics.put(statistic, s);
		}
		s.decrementStatistic(guild, category);
	}

	public void saveStatisticsToMySQL(GraphiteGuild g, EnumSet<GraphiteStatistic> statistics) {
		for(GraphiteStatistic s : statistics) {
			Map<String, Integer> v;
			if(s.isCumulative()) {
				var c = cumulativeStatistics.get(s);
				v = c == null ? Collections.emptyMap() : c.getAndZero(g);
			}else {
				v = s.getCurrentValue(g);
			}

			addStatisticValues(g, s, v);
		}
	}

	private void addStatisticValues(GraphiteGuild guild, GraphiteStatistic statistic, Map<String, Integer> values) {
		if(values.isEmpty()) return;
		Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("INSERT INTO guilds_statistics(GuildId, Statistic, Category, Timestamp, Value) VALUES(?, ?, ?, ?, ?)")) {
				for(String c : values.keySet()) {
					s.setString(1, guild.getID());
					s.setString(2, statistic.name());
					s.setString(3, c == null ? "default" : c);
					s.setLong(4, System.currentTimeMillis());
					s.setInt(5, values.get(c));
					s.addBatch();
				}
				s.executeBatch();
			}
		});
	}

	private List<StatisticValue> getStatisticValues(GraphiteGuild guild, GraphiteStatistic statistic, long timeframe) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT Timestamp, Value FROM guilds_statistics WHERE GuildId = ? AND Statistic = ? AND Timestamp > ? ORDER BY Timestamp ASC")) {
				s.setString(1, guild.getID());
				s.setString(2, statistic.name());
				s.setLong(3, System.currentTimeMillis() - timeframe);
				try(ResultSet r = s.executeQuery()) {
					List<StatisticValue> stats = new ArrayList<>();
					while(r.next()) {
						stats.add(new StatisticValue(statistic, r.getLong("Timestamp"), r.getInt("Value")));
					}
					return stats;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load statistic values from MySQL", e));
	}

	private List<StatisticValue> getStatisticValues(GraphiteGuild guild, GraphiteStatistic statistic, String category, long timeframe) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT Timestamp, Value FROM guilds_statistics WHERE GuildId = ? AND Statistic = ? AND Category = ? AND Timestamp > ? ORDER BY Timestamp ASC")) {
				s.setString(1, guild.getID());
				s.setString(2, statistic.name());
				s.setString(3, category);
				s.setLong(4, System.currentTimeMillis() - timeframe);
				try(ResultSet r = s.executeQuery()) {
					List<StatisticValue> stats = new ArrayList<>();
					while(r.next()) {
						stats.add(new StatisticValue(statistic, r.getLong("Timestamp"), r.getInt("Value")));
					}
					return stats;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load statistic values from MySQL", e));
	}

	private PartialStatistics getPartialStatistics(GraphiteGuild guild, GraphiteStatistic statistic, StatisticsElementTimeframe timeframe, StatisticsElementPointFrequency pointFrequency) {
		return new PartialStatistics(guild, statistic, getStatisticValues(guild, statistic, timeframe.getRawTimeframe() + pointFrequency.getRawTimeframe()), pointFrequency);
	}

	private List<PartialStatistics> getPartialStatistics2(GraphiteGuild guild, GraphiteStatistic statistic, StatisticsElementTimeframe timeframe, StatisticsElementPointFrequency pointFrequency) {
		List<PartialStatistics> s = new ArrayList<>();
		for(String c : getCategories(guild, statistic)) {
			s.add(new PartialStatistics(guild, statistic, c, getStatisticValues(guild, statistic, c, timeframe.getRawTimeframe() + pointFrequency.getRawTimeframe()), pointFrequency));
		}
		return s.stream()
				.filter(p -> !p.isEmpty())
				.sorted(PARTIAL_STATISTICS_SORT)
				.limit(5)
				.collect(Collectors.toList());
	}

	private List<StatisticValue> generatePreviewValues(GraphiteStatistic statistic, StatisticsElementTimeframe timeframe, StatisticsElementPointFrequency pointFrequency, double offset) {
		List<StatisticValue> values = new ArrayList<>();
		long t = System.currentTimeMillis();
		for(int i = 0; i < (timeframe.getRawTimeframe() + pointFrequency.getRawTimeframe()) / STATISTICS_INTERVAL; i++) {
			values.add(new StatisticValue(statistic, t - (long) i * STATISTICS_INTERVAL, (int) Math.floor((2 - Math.sin((i / ((double) timeframe.getRawTimeframe() / STATISTICS_INTERVAL) + offset) * Math.PI * 2)) * 365)));
		}
		return values;
	}

	private List<PartialStatistics> getPreviewStatistics2(GraphiteGuild guild, GraphiteStatistic statistic, StatisticsElementTimeframe timeframe, StatisticsElementPointFrequency pointFrequency, double offset) {
		if(!statistic.hasCategories()) return Collections.singletonList(new PartialStatistics(guild, statistic, generatePreviewValues(statistic, timeframe, pointFrequency, offset), pointFrequency));
		List<PartialStatistics> s = new ArrayList<>();
		for(int i = 0; i < 5; i++) {
			s.add(new PartialStatistics(guild, statistic, "category" + i, generatePreviewValues(statistic, timeframe, pointFrequency, offset), pointFrequency));
		}
		return s;
	}

	public List<PartialStatistics> getPartialStatistics(GraphiteGuild guild, GraphiteStatistic statistic, StatisticsElementTimeframe timeframe, StatisticsElementPointFrequency pointFrequency, double offset, boolean preview) {
		if(preview) return getPreviewStatistics2(guild, statistic, timeframe, pointFrequency, offset);
		if(!statistic.hasCategories()) return Collections.singletonList(getPartialStatistics(guild, statistic, timeframe, pointFrequency));
		return getPartialStatistics2(guild, statistic, timeframe, pointFrequency);
	}

	private StatisticValue getLastStatisticValue(GraphiteGuild guild, GraphiteStatistic statistic) {
		if(!statistic.isCumulative()) return new StatisticValue(statistic, System.currentTimeMillis(), statistic.getCurrentValue(guild).get("default"));
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT Timestamp, Value FROM guilds_statistics WHERE GuildId = ? AND Statistic = ? ORDER BY Timestamp DESC LIMIT 1")) {
				s.setString(1, guild.getID());
				s.setString(2, statistic.name());
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return new StatisticValue(statistic, System.currentTimeMillis(), 0);
					return new StatisticValue(statistic, r.getLong("Timestamp"), r.getInt("Value"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load statistic values from MySQL", e));
	}

	private List<String> getCategories(GraphiteGuild guild, GraphiteStatistic statistic) {
		if(!statistic.hasCategories()) throw new FriendlyException("Statistic doesn't have categories");
		return Graphite.getMySQL().queryArray(String.class, "SELECT DISTINCT Category FROM guilds_statistics WHERE GuildId = ? AND Statistic = ?", guild.getID(), statistic.name())
				.orElseThrowOther(e -> new FriendlyException("Failed to load statistic categories from MySQL", e));
	}

	private List<StatisticValue> getLastStatisticValues(GraphiteGuild guild, GraphiteStatistic statistic) {
		if(!statistic.hasCategories()) return Collections.singletonList(getLastStatisticValue(guild, statistic));
		List<StatisticValue> vals = new ArrayList<>();
		for(String cat : getCategories(guild, statistic)) {
			Graphite.getMySQL().run(con -> {
				try(PreparedStatement s = con.prepareStatement("SELECT Timestamp, Value FROM guilds_statistics WHERE GuildId = ? AND Statistic = ? AND Category = ? ORDER BY Timestamp DESC LIMIT 1")) {
					s.setString(1, guild.getID());
					s.setString(2, statistic.name());
					s.setString(3, cat);
					try(ResultSet r = s.executeQuery()) {
						if(!r.next()) vals.add(new StatisticValue(statistic, System.currentTimeMillis(), 0));
						vals.add(new StatisticValue(statistic, cat, r.getLong("Timestamp"), r.getInt("Value")));
					}
				}
			});
		}
		return vals.stream()
				.sorted(STATISTICS_SORT)
				.limit(5)
				.collect(Collectors.toList());
	}

	public List<StatisticValue> getLastStatisticValues(GraphiteGuild guild, GraphiteStatistic statistic, boolean preview, int index, int total) {
		if(preview) {
			if(!statistic.hasCategories()) return Collections.singletonList(new StatisticValue(statistic, System.currentTimeMillis(), (int) Math.ceil(100 * Math.pow(2/3d, index - 1))));
			List<StatisticValue> v = new ArrayList<>();
			for(int i = 0; i < 5; i++) {
				v.add(new StatisticValue(statistic, "Category " + (i + 1), System.currentTimeMillis(), (int) Math.ceil(100 * Math.pow(2/3d, index - 1))));
			}
			return v;
		}
		return getLastStatisticValues(guild, statistic);
	}

}
