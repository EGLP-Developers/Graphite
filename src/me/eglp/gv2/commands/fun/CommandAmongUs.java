package me.eglp.gv2.commands.fun;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.eglp.amongus4graphite.auc.PlayerColor;
import me.eglp.amongus4graphite.game.AmongUsCaptureUser;
import me.eglp.amongus4graphite.game.AmongUsPlayer;
import me.eglp.gv2.guild.GraphiteAudioChannel;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.guild.GraphiteVoiceChannel;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.input.ButtonInput;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class CommandAmongUs extends ParentCommand {

	public CommandAmongUs() {
		super(GraphiteModule.FUN, CommandCategory.FUN, "amongus");
		addAlias("au");
		setDescription(DefaultLocaleString.COMMAND_AMONGUS_DESCRIPTION);

		addSubCommand(new Command(this, "create") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteAudioChannel aCh = event.getMember().getCurrentAudioChannel();

				if(aCh == null || !aCh.isVoiceChannel()) {
					DefaultMessage.COMMAND_AMONGUS_CREATE_NOT_IN_VOICECHANNEL.reply(event);
					return;
				}

				GraphiteVoiceChannel ch = (GraphiteVoiceChannel) aCh;

				if(Graphite.getAmongUs().getCaptureUserInChannel(ch) != null) {
					DefaultMessage.COMMAND_AMONGUS_CREATE_ALREADY_EXISTS.reply(event);
					return;
				}

				AmongUsCaptureUser capture = Graphite.getAmongUs().getServer().createCaptureUser();
				capture.setData("discord", event.getMember());

				EmbedBuilder b = new EmbedBuilder();
				b.setTitle("Link your Among Us Capture");

				String url = Graphite.getMainBotInfo().getAmongUs().getCaptureURL().replace("{code}", capture.getCode());
				b.setDescription("Click to link automatically:\n<" + url + ">\n\nDownload AmongUsCapture: Download it [here](https://capture.automute.us/)\n\nTo link manually:");
				b.addField("URL", Graphite.getMainBotInfo().getAmongUs().getCaptureAlternativeURL(), true);
				b.addField("Code", capture.getCode(), true);

				GraphiteMessageChannel<?> mCh = event.getAuthor().openPrivateChannel();
				if(mCh == null) mCh = event.getChannel(); // DMs are closed
				mCh.sendMessage(b.build());

//				DeferredReply d = event.deferReply(DefaultMessage.COMMAND_AMONGUS_CREATE_MESSAGE.createEmbed(event.getSender()));
				ButtonInput<PlayerColor> i = new ButtonInput<PlayerColor>((List<GraphiteUser>) null, ev -> {
					if(capture.getRoom() == null) return;
					GraphiteMember mem = event.getGuild().getMember(ev.getUser());
					AmongUsPlayer pl = capture.getRoom().getPlayer(ev.getItem());
					if(mem == null || pl == null || !ch.equals(mem.getCurrentAudioChannel())) return;
					Graphite.getAmongUs().linkUser(capture, mem, pl);
				});

				int idx = 0;
				for(PlayerColor c : PlayerColor.values()) {
					if(idx > 0 && idx % 5 == 0) i.newRow();
					i.addOption(ButtonStyle.PRIMARY, JDAEmote.getCrewmateEmote(c), c);
					idx++;
				}

				i.expireAfter(5, TimeUnit.HOURS);
				i.removeMessage(false);
				i.autoRemove(false);
				i.reply(event, new MessageCreateBuilder().setEmbeds(DefaultMessage.COMMAND_AMONGUS_CREATE_MESSAGE.createEmbed(event.getGuild())), o -> {
					Message m = null;
					if(o instanceof Message) {
						m = (Message) o;
					}else if(o instanceof InteractionHook){
						m = ((InteractionHook) o).retrieveOriginal().complete();
					}
					if(m == null) throw new IllegalStateException("No message");
					capture.setData("message", m);
				});

				capture.setData("select", i);
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_AMONGUS_CREATE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_AMONGUS_CREATE_USAGE)
		.setPermission(DefaultPermissions.FUN_AMONGUS_CREATE);

		addSubCommand(new Command(this, "link") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteAudioChannel aCh = event.getMember().getCurrentAudioChannel();

				if(aCh == null || !aCh.isVoiceChannel()) {
					DefaultMessage.COMMAND_AMONGUS_CREATE_NOT_IN_VOICECHANNEL.reply(event);
					return;
				}

				GraphiteVoiceChannel ch = (GraphiteVoiceChannel) aCh;

				AmongUsCaptureUser capture = Graphite.getAmongUs().getCaptureUserInChannel(ch);
				if(capture == null) {
					DefaultMessage.COMMAND_AMONGUS_NOT_IN_VALID_VOICECHANNEL.reply(event);
					return;
				}

				GraphiteUser u = (GraphiteUser) event.getOption("user");
				GraphiteMember mem = event.getGuild().getMember(u);
				if(mem == null) {
					DefaultMessage.COMMAND_AMONGUS_LINK_NOT_A_MEMBER.reply(event);
					return;
				}

				if(!ch.equals(mem.getCurrentAudioChannel())) {
					DefaultMessage.COMMAND_AMONGUS_LINK_MEMBER_NOT_IN_VOICECHANNEL.reply(event);
					return;
				}

				AmongUsPlayer pl;
				String color = (String) event.getOption("color");
				try {
					PlayerColor col = PlayerColor.valueOf(color.toUpperCase());
					pl = capture.getRoom().getPlayer(col);
				}catch(IllegalArgumentException e) {
					pl = capture.getRoom().getPlayers().stream()
							.filter(p -> p.getAmongUsName().equalsIgnoreCase(color))
							.findFirst().orElse(null);
				}

				if(pl == null) {
					DefaultMessage.COMMAND_AMONGUS_LINK_INVALID_COLOR_OR_PLAYER.reply(event);
					return;
				}

				Graphite.getAmongUs().linkUser(capture, mem, pl);
				event.deleteMessage(JDAEmote.OK_HAND);
			}

			@Override
			public List<OptionData> getOptions() {
				OptionData d = new OptionData(OptionType.STRING, "color", "The color for the linked user", true);
				for(PlayerColor c : PlayerColor.values()) {
					d.addChoice(c.name(), c.name());
				}
				return Arrays.asList(
						new OptionData(OptionType.USER, "user", "The user you want to link", true),
						d
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_AMONGUS_LINK_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_AMONGUS_LINK_USAGE)
		.setPermission(DefaultPermissions.FUN_AMONGUS_LINK);

		addSubCommand(new Command(this, "stop") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteAudioChannel aCh = event.getMember().getCurrentAudioChannel();

				if(aCh == null || !aCh.isVoiceChannel()) {
					DefaultMessage.COMMAND_AMONGUS_CREATE_NOT_IN_VOICECHANNEL.reply(event);
					return;
				}

				GraphiteVoiceChannel ch = (GraphiteVoiceChannel) aCh;

				AmongUsCaptureUser capture = Graphite.getAmongUs().getCaptureUserInChannel(ch);
				if(capture == null) {
					DefaultMessage.COMMAND_AMONGUS_NOT_IN_VALID_VOICECHANNEL.reply(event);
					return;
				}

				if(!event.getMember().equals(capture.getData("discord"))) {
					DefaultMessage.COMMAND_AMONGUS_STOP_NOT_CAPTURE_USER.reply(event);
					return;
				}

				Graphite.getAmongUs().stopRound(capture);
				event.deleteMessage(JDAEmote.OK_HAND);
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_AMONGUS_STOP_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_AMONGUS_STOP_USAGE)
		.setPermission(DefaultPermissions.FUN_AMONGUS_CREATE);
	}

}
