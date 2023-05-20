package me.eglp.gv2.commands.channel_management;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.guild.GuildUserChannel;
import me.eglp.gv2.guild.config.GuildChannelsConfig;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandUserChannel extends ParentCommand{

	public CommandUserChannel() {
		super(GraphiteModule.CHANNEL_MANAGEMENT, CommandCategory.CHANNEL_MANAGEMENT, "userchannel");
		setDescription(DefaultLocaleString.COMMAND_USERCHANNEL_DESCRIPTION);

		addSubCommand(new Command(this, "create") {

			@Override
			public void action(CommandInvokedEvent event) {
				if(event.getMember().getCurrentAudioChannel() == null) {
					DefaultMessage.ERROR_NOT_IN_AUDIOCHANNEL.reply(event);
					return;
				}

				GraphiteGuild g = event.getGuild();
				GuildChannelsConfig c = g.getChannelsConfig();
				if(c.getUserChannelByOwner(event.getMember()) != null) {
					DefaultMessage.COMMAND_USERCHANNEL_ONE_PER_MEMBER.reply(event);
					return;
				}

				GuildUserChannel usr = c.createUserChannel(event.getMember());
				event.getGuild().getJDAGuild().moveVoiceMember(event.getMember().getMember(), usr.getChannel().getJDAChannel()).queue();
				DefaultMessage.COMMAND_USERCHANNEL_CREATED.reply(event);
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_USERCHANNEL_CREATE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_USERCHANNEL_CREATE_USAGE)
		.setPermission(DefaultPermissions.CHANNEL_USERCHANNEL_CREATE)
		.requirePermissions(Permission.MANAGE_CHANNEL, Permission.VOICE_MOVE_OTHERS);

		addSubCommand(new Command(this, "delete") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				GuildChannelsConfig c = g.getChannelsConfig();
				GuildUserChannel uc = c.getUserChannelByOwner(event.getMember());
				if(uc == null) {
					DefaultMessage.COMMAND_USERCHANNEL_DOESNT_EXIST.reply(event);
					return;
				}

				uc.delete();
				DefaultMessage.COMMAND_USERCHANNEL_DELETED.reply(event);
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_USERCHANNEL_DELETE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_USERCHANNEL_DELETE_USAGE)
		.setPermission(DefaultPermissions.CHANNEL_USERCHANNEL_CREATE)
		.requirePermissions(Permission.MANAGE_CHANNEL);
	}

}
