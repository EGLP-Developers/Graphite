package me.eglp.gv2.commands.channel_management;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.util.base.guild.GraphiteCategory;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.channel.concrete.CategoryManager;

public class CommandCategoryCopy extends Command{

	public CommandCategoryCopy() {
		super(GraphiteModule.CHANNEL_MANAGEMENT, CommandCategory.CHANNEL_MANAGEMENT, "categorycopy");
		setDescription(DefaultLocaleString.COMMAND_CATEGORYCOPY_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_CATEGORYCOPY_USAGE);
		setPermission(DefaultPermissions.CHANNEL_CATEGORYCOPY);
		addAlias("catcopy");
		requirePermissions(Permission.MANAGE_CHANNEL);
	}

	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteCategory fromCategory = (GraphiteCategory) event.getOption("from-category");
		GraphiteCategory toCategory = (GraphiteCategory) event.getOption("to-category");
		
		if(!toCategory.equals(fromCategory)) {
			List<PermissionOverride> newOverrides = fromCategory.getJDACategory().getPermissionOverrides();
			List<PermissionOverride> oldOverrides = toCategory.getJDACategory().getPermissionOverrides();
			
			CategoryManager m = toCategory.getJDACategory().getManager();
			oldOverrides.forEach(o -> m.removePermissionOverride(o.getIdLong()));
			newOverrides.forEach(o -> m.putPermissionOverride(o.getPermissionHolder(), o.getAllowedRaw(), o.getDeniedRaw()));
			m.queue();
		}
		
		DefaultMessage.COMMAND_CATEGORYCOPY_SUCCESS.reply(event, "from_category", fromCategory.getName(), "to_category", toCategory.getName());
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.CHANNEL, "from-category", "The category you want to copy from", true).setChannelTypes(ChannelType.CATEGORY),
				new OptionData(OptionType.CHANNEL, "to-category", "The category you want to copy to", true).setChannelTypes(ChannelType.CATEGORY)
			);
	}

}
