package me.eglp.gv2.main;

import java.util.stream.Collectors;

import me.eglp.amongus4graphite.game.AmongUsCaptureUser;
import me.eglp.amongus4graphite.game.AmongUsPlayer;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.GraphiteRole;
import me.eglp.gv2.guild.GraphiteTextChannel;
import me.eglp.gv2.guild.GraphiteVoiceChannel;
import me.eglp.gv2.guild.GuildAutoChannel;
import me.eglp.gv2.guild.GuildJail;
import me.eglp.gv2.guild.GuildUserChannel;
import me.eglp.gv2.guild.config.GuildChannelsConfig;
import me.eglp.gv2.guild.config.GuildModerationConfig;
import me.eglp.gv2.guild.config.GuildRolesConfig;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.user.EasterEgg;
import me.eglp.gv2.util.event.SingleEventHandler;
import me.eglp.gv2.util.lang.DefaultMessage;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class GraphiteCommonListeners {

	public static void register() {
		Graphite.getJDAListener().registerHandler(SingleEventHandler.of(GuildVoiceUpdateEvent.class, event -> {
			GraphiteGuild guild = Graphite.getGuild(event.getGuild());
			GraphiteMember member = guild.getMember(event.getMember());
			GuildChannelsConfig channelsConfig = guild.getChannelsConfig();

			if(event.getChannelLeft() != null && event.getChannelLeft().getType() == ChannelType.VOICE) { // NONBETA: stage channels?
				GraphiteVoiceChannel channelLeft = guild.getVoiceChannel((VoiceChannel) event.getChannelLeft());

				GuildUserChannel uc = channelsConfig.getUserChannelByChannel(channelLeft);
				if(GraphiteMultiplex.isHighestRelativeHierarchy(guild, GraphiteFeature.CHANNEL_MANAGEMENT) && uc != null && event.getChannelLeft().getMembers().isEmpty()) {
					uc.delete();
				}

				if(GraphiteMultiplex.isHighestRelativeHierarchy(guild, GraphiteFeature.CHANNEL_MANAGEMENT) && channelsConfig.isAutoCreatedChannel(channelLeft) && event.getChannelLeft().getMembers().isEmpty()) {
					event.getChannelLeft().delete().queue(null, new ErrorHandler(e -> {
								GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Failed to delete channel", e);
							})
							.ignore(ErrorResponse.UNKNOWN_CHANNEL));
				}

				AmongUsCaptureUser u = Graphite.getAmongUs().getCaptureUserInChannel(channelLeft);
				if(u != null) {
					AmongUsPlayer pl = u.getRoom().getPlayers().stream()
							.filter(p -> member.equals(p.getData("discord")))
							.findFirst().orElse(null);

					if(pl != null) {
						pl.setData("discord", null);
						Graphite.getGuild(event.getGuild()).updateMember(member, false, false);
						Graphite.getAmongUs().queueUpdateMessage(u);
					}
				}

				AmongUsCaptureUser u2 = Graphite.getAmongUs().getCaptureUser(member);
				if(u2 != null) Graphite.getAmongUs().stopRound(u2);
			}

			if(event.getChannelJoined() != null && event.getChannelJoined().getType() == ChannelType.VOICE) { // NONBETA: stage channels?
				GraphiteVoiceChannel channelJoined = guild.getVoiceChannel((VoiceChannel) event.getChannelJoined());
				GuildModerationConfig moderationConfig = guild.getModerationConfig();
				GuildRolesConfig rolesConfig = guild.getRolesConfig();

				GuildJail jail = moderationConfig.getJail(member);
				if(GraphiteMultiplex.isHighestRelativeHierarchy(guild, GraphiteFeature.MODERATION) && jail != null && !jail.getChannel().getJDAChannel().equals(event.getChannelJoined())) {
					jail.addLeaveAttempt();
					if(jail.getLeaveAttempts() >= 3) {
						event.getGuild().kickVoiceMember(event.getMember()).queue(null, e -> {
							GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Failed to kick member from voice channel", e);
						});
						DefaultMessage.COMMAND_JAIL_KICKED.sendMessage(member.openPrivateChannel());
					}else {
						event.getGuild().moveVoiceMember(event.getMember(), jail.getChannel().getJDAChannel()).queue();
						DefaultMessage.COMMAND_JAIL_JAILED.sendMessage(member.openPrivateChannel(), "attempts", String.valueOf(jail.getLeaveAttempts()), "max_attempts", "3");
					}
				}

				if(GraphiteMultiplex.isHighestRelativeHierarchy(guild, GraphiteFeature.MODERATION)
						&& jail == null
						&& channelsConfig.getSupportQueue() != null
						&& (!rolesConfig.getModeratorRoles().isEmpty() || channelsConfig.getModLogChannel() != null)
						&& event.getChannelJoined().equals(channelsConfig.getSupportQueue().getJDAChannel())) {
					guild.getMembersWithAnyRole(rolesConfig.getModeratorRoles()).stream()
						.filter(m -> !m.isBot())
						.forEach(mem -> DefaultMessage.OTHER_JOINED_SUPPORT_QUEUE.sendMessage(mem.openPrivateChannel(), "users", channelsConfig.getSupportQueue().getJDAChannel().getMembers().stream().map(u -> u.getEffectiveName()).collect(Collectors.joining(", "))));

					GraphiteTextChannel modLogChannel = channelsConfig.getModLogChannel();
					if(modLogChannel != null) DefaultMessage.OTHER_JOINED_SUPPORT_QUEUE.sendMessage(modLogChannel, "users", channelsConfig.getSupportQueue().getJDAChannel().getMembers().stream().map(u -> u.getEffectiveName()).collect(Collectors.joining(", ")));
				}

				GuildAutoChannel ac = channelsConfig.getAutoChannelByID(channelJoined.getID());
				if(GraphiteMultiplex.isHighestRelativeHierarchy(guild, GraphiteFeature.CHANNEL_MANAGEMENT) && ac != null) {
					ac.createAutoChannel().thenAccept(c -> {
						Member m = member.getJDAMember();
						if(!m.getVoiceState().inAudioChannel()) {
							c.getJDAChannel().delete().queue(null, e -> {
								GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Failed to create channel", e);
							});
							return;
						}
						event.getGuild().moveVoiceMember(m, c.getJDAChannel()).queue();
					});
				}

				AmongUsCaptureUser u = Graphite.getAmongUs().getCaptureUserInChannel(channelJoined);
				if(u != null) Graphite.getAmongUs().autoLinkUserIfExists(u, member);
			}
		}));

		Graphite.getJDAListener().registerHandler(SingleEventHandler.of(GuildMemberUpdateNicknameEvent.class, event -> {
			GraphiteGuild guild = Graphite.getGuild(event.getGuild());
			GraphiteMember member = guild.getMember(event.getMember());
			if(GraphiteMultiplex.isHighestRelativeHierarchy(event.getGuild()) && !member.isBot() && event.getNewNickname() != null && event.getNewNickname().equalsIgnoreCase("Smile")) {
				if(!member.getConfig().hasFoundEasterEgg(EasterEgg.SMILE)) {
					member.getConfig().addEasterEgg(EasterEgg.SMILE, true);
				}
			}
		}));

		Graphite.getJDAListener().registerHandler(SingleEventHandler.of(ChannelCreateEvent.class, event -> {
			GraphiteGuild guild = Graphite.getGuild(event.getGuild());

			if(event.getChannelType() == ChannelType.TEXT) {
				TextChannel tc = (TextChannel) event.getChannel();
				if(GraphiteMultiplex.isHighestRelativeHierarchy(guild, GraphiteFeature.MODERATION)) {
					GraphiteRole mutedRole = guild.getRolesConfig().getMutedRoleRaw();
					if(mutedRole != null) {
						tc.upsertPermissionOverride(mutedRole.getJDARole()).deny(GuildRolesConfig.MUTED_ROLE_DENIED_PERMISSIONS).queue();
					}
				}
			}
		}));
	}

}
