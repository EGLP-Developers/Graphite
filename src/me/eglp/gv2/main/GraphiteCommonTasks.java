package me.eglp.gv2.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.guild.GuildChatMute;
import me.eglp.gv2.guild.GuildJail;
import me.eglp.gv2.guild.chatreport.GuildChatReport;
import me.eglp.gv2.guild.config.GuildBackupConfig;
import me.eglp.gv2.guild.config.GuildTemporaryActionsConfig;
import me.eglp.gv2.guild.recorder.recording.GuildAudioRecording;
import me.eglp.gv2.guild.temporary_actions.GuildTempBan;
import me.eglp.gv2.guild.temporary_actions.GuildTempVoiceMute;
import me.eglp.gv2.util.apis.reddit.GraphiteSubreddit;
import me.eglp.gv2.util.apis.twitch.GraphiteTwitchUser;
import me.eglp.gv2.util.apis.twitch.TwitchAnnouncementParameter;
import me.eglp.gv2.util.backup.GuildBackup;
import me.eglp.gv2.util.stats.GraphiteStatistic;
import me.eglp.gv2.util.stats.GraphiteStatistics;
import me.eglp.gv2.util.stats.element.GuildStatisticsElement;
import me.eglp.reddit.entity.SubredditSort;
import me.eglp.reddit.entity.data.Link;
import me.eglp.reddit.util.ListingPaginator;
import me.eglp.twitch.entity.TwitchStream;
import me.mrletsplay.mrcore.misc.FriendlyException;
import net.dv8tion.jda.api.entities.Message;

public class GraphiteCommonTasks {

	public static void start() {
		Graphite.getScheduler().scheduleAtFixedRate("guilds-temporary-refresh", () -> {
			for(GraphiteGuild g : Graphite.getGuilds()) {
				GuildTemporaryActionsConfig c = g.getTemporaryActionsConfig();

				for(GuildTempBan b : c.getTempBans()) {
					if(b.isExpired()) b.remove(g.getSelfMember(), "Ban expired");
				}

				for(GuildTempVoiceMute m : c.getTempMutes()) {
					if(m.isExpired()) m.remove(g.getSelfMember(), "Mute expired");
				}

				for(GuildJail j : g.getModerationConfig().getJails()) {
					if(j.isExpired()) j.remove(g.getSelfMember(), "Jail expired");
				}

				for(GuildAudioRecording r : g.getRecordingsConfig().getRecordings()) {
					if(r.isExpired()) r.remove();
				}

				for(GuildChatMute r : g.getModerationConfig().getChatMutes()) {
					if(r.isExpired()) r.remove(g.getSelfMember(), "Chatmute expired");
				}
			}
		}, 60 * 1000L);

		if(Graphite.getBotInfo().getTwitch().isEnabled()) Graphite.getScheduler().scheduleAtFixedRate("guilds-twitch-refresh", () -> {
			for(GraphiteGuild g : Graphite.getGuilds()) {
				if(!g.getConfig().hasModuleEnabled(GraphiteModule.TWITCH)) continue;

				try {
					List<GraphiteTwitchUser> twitchUsers = g.getTwitchConfig().getTwitchUsers();
					String[] ids = twitchUsers.stream()
							.filter(Objects::nonNull)
							.map(tw -> tw.getTwitchUser().getID())
							.toArray(String[]::new);

					if(ids.length != 0) {
						try {
							List<TwitchStream> streams = Graphite.getTwitch().getTwitchAPI().getStreamsByUserIDs(ids);

							for(GraphiteTwitchUser u : twitchUsers) {
								boolean isLive = streams.stream().anyMatch(s -> s.getUserID().equals(u.getID()));
								boolean wasLive = g.getTwitchConfig().getWasLive(u);
								if(isLive && !wasLive) {
									g.getTwitchConfig().setWasLive(u, true);

									Message m = u.sendNotificationMessage(g);
									if(u.getParameters().contains(TwitchAnnouncementParameter.REMOVE_ON_END) && m != null) {
										g.getTwitchConfig().setMessageID(u, m.getId());
									}
								}else if(!isLive && wasLive) {
									g.getTwitchConfig().setWasLive(u, false);

									GraphiteGuildMessageChannel tc = u.getNotificationChannel(g);
									String msgID = g.getTwitchConfig().getMessageID(u);
									if(tc != null && msgID != null) {
										tc.getJDAChannel().deleteMessageById(msgID).queue(null, e -> {});
									}
									g.getTwitchConfig().setMessageID(u, msgID);
								}
							}
						}catch(Exception e) {
							GraphiteDebug.log(DebugCategory.TWITCH, "Twitch is unreachable. Stack trace:");
							GraphiteDebug.log(DebugCategory.TWITCH, e);
							continue;
						}
					}
				}catch(Exception e) {
					GraphiteDebug.log(DebugCategory.TWITCH, new FriendlyException("Failed to load Twitch users for guild " + g.getID(), e));
					continue;
				}
			}
		}, 5 * 60 * 1000);

		if(Graphite.getBotInfo().getReddit().isEnabled()) Graphite.getScheduler().scheduleAtFixedRate("guilds-reddit-refresh", () -> {
			for(GraphiteGuild g : Graphite.getGuilds()) {
				if(!g.getConfig().hasModuleEnabled(GraphiteModule.REDDIT)) continue;

				List<GraphiteSubreddit> subreddits = g.getRedditConfig().getSubreddits();
				String[] ids = subreddits.stream()
						.filter(Objects::nonNull)
						.map(tw -> tw.getSubreddit())
						.toArray(String[]::new);

				if(ids.length != 0) {
					try {
						for(GraphiteSubreddit r : subreddits) {
							String lastID = g.getRedditConfig().getLastPostID(r);
							if(lastID == null) {
								List<Link> p = Graphite.getReddit().getRedditAPI().getPosts(r.getSubreddit(), SubredditSort.NEW, 1).collect(1);
								if(p.isEmpty()) {
									g.getRedditConfig().setLastPostID(r, "none");
								}else {
									g.getRedditConfig().setLastPostID(r, p.get(0).getID());
								}
							}else {
								List<Link> posts;
								if(lastID.equals("none")) {
									posts = Graphite.getReddit().getRedditAPI().getPosts(r.getSubreddit(), SubredditSort.NEW, 10).collect(10);
								}else {
									ListingPaginator<Link> pag = Graphite.getReddit().getRedditAPI().getPosts(r.getSubreddit(), SubredditSort.NEW, 10);
									posts = new ArrayList<>(pag.getCurrentListing().getData().getChildren());

//									while(!posts.stream().anyMatch(p -> p.getID().equals(lastID)) && posts.size() < 10) { TODO: currently unused? Only used if max posts at once > 10
//										if(!pag.next()) break;
//										posts.addAll(pag.getCurrentListing().getData().getChildren());
//									}

									if(posts.stream().anyMatch(p -> p.getID().equals(lastID))) {
										while(!posts.isEmpty() && !posts.get(posts.size() - 1).getID().equals(lastID)) posts.remove(posts.size() - 1);
										if(!posts.isEmpty()) posts.remove(posts.size() - 1);
									}
								}

								if(posts.isEmpty()) continue;
								Collections.reverse(posts);
								for(Link p : posts) {
									r.sendNotificationMessage(g, p);
								}
								g.getRedditConfig().setLastPostID(r, posts.get(posts.size() - 1).getID());
							}
						}
					}catch(Exception e) {
						e.printStackTrace();
						GraphiteDebug.log(DebugCategory.REDDIT, "Reddit is unreachable");
						continue;
					}
				}
			}
		}, 5 * 60 * 1000);

		Graphite.getScheduler().scheduleAtFixedRate("guilds-backup-refresh", () -> {
			for(GraphiteGuild g : Graphite.getGuilds()) {
				for(GuildBackup b : g.getBackups()) {
					long age = System.currentTimeMillis() - b.getTimestamp();
					if(age >= 30L * 24L * 60L * 60L * 1000L)
						b.removeMessagesData();
				}

				GuildBackupConfig c = g.getBackupConfig();
				if(c.getBackupInterval() == -1) continue;
				if(System.currentTimeMillis() - c.getLastAutoBackup() >= g.getBackupConfig().getBackupInterval() * 24 * 60 * 60 * 1000) {
					c.setLastAutoBackupNow();
					if(!g.canCreateAutoBackup()) g.deleteLastAutoBackup();
					g.createBackup(null, 0, true); // Autobackups don't have messages
				}
			}
		}, 60 * 60 * 1000);

		Graphite.getScheduler().scheduleAtFixedRate("chatreport-refresh", () -> {
			for(GraphiteGuild g : Graphite.getGuilds()) {
				for(GuildChatReport r : g.getReportsConfig().getChatReports()) {
					long age = System.currentTimeMillis() - r.getTimestamp();
					if(age >= 30L * 24L * 60L * 60L * 1000L)
						g.getReportsConfig().removeChatReport(r);
				}
			}
		}, 60 * 60 * 1000);

		Graphite.getScheduler().scheduleAtFixedRate("webinterface/purge-sessions", () -> {
			Graphite.getWebinterface().getSessionStorage().purgeSessions();
		}, 60 * 60 * 1000);

		Graphite.getScheduler().scheduleAtFixedRate("purge-guilds", () -> {
			Graphite.getDataManager().updateGuilds();
			Graphite.getDataManager().purgeGuilds();
		}, 60 * 60 * 1000);

		Graphite.getScheduler().scheduleAtFixedRate("refresh-image-cache", () -> {
			Graphite.getWebsiteEndpoint().getImageCache().refreshCache();
		}, 5 * 60 * 1000);

		Graphite.getScheduler().scheduleAtFixedRate("save-statistics", () -> {
			for(GraphiteGuild g : Graphite.getGuilds()) {
				List<GuildStatisticsElement> els = g.getStatisticsConfig().getStatisticsElements();
				EnumSet<GraphiteStatistic> stats = els.stream()
						.flatMap(el -> el.getSettings().getStatistics().stream())
						.distinct()
						.collect(Collectors.toCollection(() -> EnumSet.noneOf(GraphiteStatistic.class)));

				Graphite.getStatistics().saveStatisticsToMySQL(g, stats);

				for(GuildStatisticsElement el : els) {
					el.updateMessageIfExists();
				}
			}
		}, GraphiteStatistics.STATISTICS_INTERVAL);
	}

}
