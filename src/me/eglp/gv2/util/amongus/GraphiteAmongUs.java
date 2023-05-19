package me.eglp.gv2.util.amongus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.eglp.amongus4graphite.AmongUsWebSocketServer;
import me.eglp.amongus4graphite.auc.LobbyEvent;
import me.eglp.amongus4graphite.game.AmongUsCaptureUser;
import me.eglp.amongus4graphite.game.AmongUsListener;
import me.eglp.amongus4graphite.game.AmongUsPlayer;
import me.eglp.gv2.guild.GraphiteAudioChannel;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.GraphiteVoiceChannel;
import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.input.ButtonInput;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.misc.FriendlyException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

@SQLTable(
	name = "guilds_amongus_users",
	columns = {
		"GuildId varchar(255) DEFAULT NULL",
		"DiscordUserId varchar(255) DEFAULT NULL",
		"AmongUsName varchar(255) DEFAULT NULL",
		"PRIMARY KEY(GuildId, DiscordUserId)",
		"UNIQUE KEY(GuildId, AmongUsName)"
	},
	guildReference = "GuildId"
)
public class GraphiteAmongUs {

	private AmongUsWebSocketServer server;

	public GraphiteAmongUs() {
		this.server = new AmongUsWebSocketServer(Graphite.getMainBotInfo().getAmongUs().getPort());
		server.start();

		server.setListener(new AmongUsListener() {

			@Override
			public void mutePlayer(AmongUsCaptureUser captureUser, AmongUsPlayer player) {
				Graphite.withBot(Graphite.getGraphiteBot(), () -> {
					GraphiteMember d = (GraphiteMember) player.getData("discord");
					if(d != null) {
						boolean df = !player.isDead();
						d.getGuild().updateMember(d, true, df);
					}
				});
			}

			@Override
			public void unmutePlayer(AmongUsCaptureUser captureUser, AmongUsPlayer player) {
				Graphite.withBot(Graphite.getGraphiteBot(), () -> {
					GraphiteMember d = (GraphiteMember) player.getData("discord");
					if(d != null) d.getGuild().updateMember(d, false, false);
				});
			}

			@Override
			public void playersUpdated(AmongUsCaptureUser captureUser) {
				queueUpdateMessage(captureUser);
			}

			@Override
			public void playerLeft(AmongUsCaptureUser captureUser, AmongUsPlayer player) {
				queueUpdateMessage(captureUser);
			}

			@Override
			public void playerJoined(AmongUsCaptureUser captureUser, AmongUsPlayer player) {
				Graphite.withBot(Graphite.getGraphiteBot(), () -> {
					autoLinkUserIfExists(captureUser, player);
					queueUpdateMessage(captureUser);
				});
			}

			@Override
			public void lobbyChanged(AmongUsCaptureUser captureUser, LobbyEvent event) {
				queueUpdateMessage(captureUser);

				for(AmongUsPlayer pl : captureUser.getRoom().getPlayers()) playerJoined(captureUser, pl); // Make sure that all users currently in the room are linked
			}

			@Override
			public void connectCode(AmongUsCaptureUser captureUser, String code) {
				queueUpdateMessage(captureUser);
			}

			@Override
			public void disconnected(AmongUsCaptureUser captureUser) {
				stopRound(captureUser);
			}

		});

		Graphite.getScheduler().scheduleAtFixedRate("amongus/update-messages", () -> {
			for(AmongUsCaptureUser u : getServer().getCaptureUsers()) {
				if(!Objects.equals(u.getData("messageUpdate"), true)) continue;
				updateMessage(u);
			}
		}, 2000L);
	}

	public void queueUpdateMessage(AmongUsCaptureUser captureUser) {
		captureUser.setData("messageUpdate", true);
	}

	private void updateMessage(AmongUsCaptureUser captureUser) {
		Graphite.withBot(Graphite.getGraphiteBot(), () -> {
			GraphiteMember u = (GraphiteMember) captureUser.getData("discord");

			EmbedBuilder b = new EmbedBuilder();
			b.addField("Connected to", u.getAsMention(), false);
			if(captureUser.getRoom().getLobbyCode() != null && captureUser.getRoom().getRegion() != null) b.addField("Code", String.format("%s (%s)", captureUser.getRoom().getLobbyCode(), captureUser.getRoom().getRegion().getFriendlyName()), false);

			List<AmongUsPlayer> pls = new ArrayList<>(captureUser.getRoom().getPlayers());
			pls.sort((p1, p2) -> p1.getAmongUsColor().ordinal() - p2.getAmongUsColor().ordinal());
			for(AmongUsPlayer pl : pls) {
				GraphiteUser d = (GraphiteUser) pl.getData("discord");
				b.addField(pl.getAmongUsName(), (pl.isKnownDead() ? JDAEmote.getDeadCrewmateEmote(pl.getAmongUsColor()) : JDAEmote.getCrewmateEmote(pl.getAmongUsColor())).getUnicode() + " " + (d != null ? d.getAsMention() : "Not linked"), true);
			}

			Message m = (Message) captureUser.getData("message");
			m.editMessageEmbeds(b.build()).queue(null, t -> {
				GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Failed to edit message", t);
			});
		});
	}

	public String lookupDiscordUserID(GraphiteGuild guild, String amongUsName) {
		return Graphite.getMySQL().query(String.class, null, "SELECT DiscordUserId FROM guilds_amongus_users WHERE GuildId = ? AND AmongUsName = ?",
					guild.getID(),
					amongUsName)
				.orElseThrowOther(e -> new FriendlyException("Failed to retrieve discord user id from MySQL", e));
	}

	public String lookupAmongUsName(GraphiteGuild guild, GraphiteMember member) {
		return Graphite.getMySQL().query(String.class, null, "SELECT AmongUsName FROM guilds_amongus_users WHERE GuildId = ? AND DiscordUserId = ?", guild.getID(), member.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load Among Us name from MySQL", e));
	}

	public void saveDiscordUserID(GraphiteGuild guild, String amongUsName, GraphiteUser discordUser) {
		Graphite.getMySQL().query("INSERT INTO guilds_amongus_users(GuildId, DiscordUserId, AmongUsName) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE DiscordUserId = VALUES(DiscordUserId), AmongUsName = VALUES(AmongUsName)", guild.getID(), discordUser.getID(), amongUsName);
	}

	public AmongUsCaptureUser getCaptureUserInChannel(GraphiteVoiceChannel channel) {
		return server.getCaptureUsers().stream()
				.filter(c -> {
					if(c.getData("discord") == null) return false;
					GraphiteMember mem = (GraphiteMember) c.getData("discord");
					if(!mem.isAvailable()) return false;
					return channel.equals(mem.getCurrentAudioChannel());
				})
				.findFirst().orElse(null);
	}

	public AmongUsCaptureUser getCaptureUser(GraphiteMember member) {
		return server.getCaptureUsers().stream()
				.filter(c -> member.equals(c.getData("discord")))
				.findFirst().orElse(null);
	}

	public void autoLinkUserIfExists(AmongUsCaptureUser captureUser, AmongUsPlayer player) {
		GraphiteMember mem = (GraphiteMember) captureUser.getData("discord");
		if(mem == null) return;

		if(player.getData("discord") == null) {
			String discordID = lookupDiscordUserID(mem.getGuild(), player.getAmongUsName());

			GraphiteAudioChannel ac = mem.getCurrentAudioChannel();
			if(ac != null && ac instanceof GraphiteVoiceChannel && discordID != null) {
				GraphiteVoiceChannel vc = (GraphiteVoiceChannel) ac;
				Member m = vc.getJDAChannel().getMembers().stream()
						.filter(mb -> mb.getUser().getId().equals(discordID))
						.findFirst().orElse(null);

				if(m != null) {
					// The member that was previously linked to that Among Us user is inside the voice channel of the capture user
					// Let's automatically link them again, assuming that they're still the same person
					player.setData("discord", mem.getGuild().getMember(m));
				}
			}
		}
	}

	public void autoLinkUserIfExists(AmongUsCaptureUser captureUser, GraphiteMember member) {
		GraphiteMember mem = (GraphiteMember) captureUser.getData("discord");
		if(mem == null) return;

		String amongUsName = lookupAmongUsName(mem.getGuild(), member);

		AmongUsPlayer pl = captureUser.getRoom().getPlayer(amongUsName);
		if(pl != null && pl.getData("discord") == null) {
			pl.setData("discord", member);
			queueUpdateMessage(captureUser);
		}
	}

	public void linkUser(AmongUsCaptureUser captureUser, GraphiteMember member, AmongUsPlayer player) {
		captureUser.getRoom().getPlayers().stream()
			.filter(pl2 -> member.equals(pl2.getData("discord")))
			.forEach(pl2 -> pl2.setData("discord", null));

		player.setData("discord", member);
		saveDiscordUserID(member.getGuild(), player.getAmongUsName(), member);
		queueUpdateMessage(captureUser);
	}

	public void stopRound(AmongUsCaptureUser captureUser) {
		((ButtonInput<?>) captureUser.getData("select")).remove();
		((Message) captureUser.getData("message")).delete().queue();
		server.getCaptureUsers().remove(captureUser);
	}

	public AmongUsWebSocketServer getServer() {
		return server;
	}

	public void stop() {
		server.stop();
	}

}
