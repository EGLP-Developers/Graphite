package me.eglp.gv2.multiplex;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public class GraphiteMultiplex {

	public static ThreadLocal<MultiplexBot> bot = new ThreadLocal<>();
	private static Map<JDA, MultiplexBot> botJDAs = new HashMap<>();

	public static void registerJDA(MultiplexBot bot, JDA jda) {
		botJDAs.put(jda, bot);
	}

	public static MultiplexBot getBot(JDA jda) {
		return botJDAs.get(jda);
	}

	public static boolean isFeatureEnabled(GraphiteFeature feature) {
		return getCurrentBot().getBotInfo().isFeatureEnabled(feature);
	}

	public static MultiplexBot getBotByIdentifier(String indentifier) {
		return Graphite.getMultiplexBots().stream()
				.filter(b -> b.getIdentifier().equals(indentifier))
				.findFirst().orElse(null);
	}

	public static List<MultiplexBot> getAvailableBots(GraphiteGuild guild) {
		String id = guild.getID();
		return Graphite.getMultiplexBots().stream()
				.filter(b -> Graphite.withBot(b, () -> Graphite.isOnGuild(id)))
				.collect(Collectors.toList());
	}


	public static boolean isHighestRelativeHierarchy(Guild guild) {
		return isHighestRelativeHierarchy(Graphite.getGuild(guild));
	}

	public static boolean isHighestRelativeHierarchy(GraphiteGuild guild) {
		int hIdx = Graphite.getBotInfo().getHierarchyIndex();
		int minIdx = getAvailableBots(guild).stream()
				.mapToInt(b -> Graphite.withBot(b, () -> Graphite.getBotInfo().getHierarchyIndex()))
				.min().orElse(hIdx);
		return hIdx == minIdx;
	}

	public static boolean isHighestRelativeHierarchy(Guild guild, GraphiteFeature feature) {
		return isHighestRelativeHierarchy(Graphite.getGuild(guild), feature);
	}

	public static boolean isHighestRelativeHierarchy(GraphiteGuild guild, GraphiteFeature feature) {
		if(!Graphite.getBotInfo().isFeatureEnabled(feature)) return false;
		int hIdx = Graphite.getBotInfo().getHierarchyIndex();
		int minIdx = getAvailableBots(guild).stream()
				.filter(b -> b.getBotInfo().isFeatureEnabled(feature))
				.mapToInt(b -> Graphite.withBot(b, () -> Graphite.getBotInfo().getHierarchyIndex()))
				.min().orElse(hIdx);
		return hIdx == minIdx;
	}

	public static MultiplexBot getHighestRelativeHierarchy(GraphiteGuild guild, GraphiteFeature feature) {
		MultiplexBot bot = getAvailableBots(guild).stream()
				.filter(b -> b.getBotInfo().isFeatureEnabled(feature))
				.min(Comparator.comparingInt(b -> Graphite.withBot(b, () -> Graphite.getBotInfo().getHierarchyIndex())))
				.orElse(null);
		return bot;
	}

	public static ContextHandle handle() {
		MultiplexBot oldBot = getCurrentBot();
		return () -> setCurrentBot(oldBot);
	}

	public static ContextHandle setCurrentBot(MultiplexBot bot) {
		MultiplexBot oldBot = getCurrentBot();
		GraphiteMultiplex.bot.set(bot);
		return () -> setCurrentBot(oldBot);
	}

	public static MultiplexBot getCurrentBot() {
		return bot.get();
	}

	public static void clearContext() {
		bot.remove();
	}

}
