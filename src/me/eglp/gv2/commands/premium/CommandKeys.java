package me.eglp.gv2.commands.premium;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.SpecialExecute;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandKeys extends Command{
	
	public CommandKeys() {
		super(null, CommandCategory.PREMIUM, "keys");
		setDescription(DefaultLocaleString.COMMAND_KEYS_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_KEYS_USAGE);
	}
	
	@SpecialExecute(allowPrivate = true, allowServer = false)
	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getAuthor().getPremiumKeys().isEmpty()) {
			DefaultMessage.COMMAND_KEYS_NO_KEYS.reply(event);
			return;
		}
		EmbedBuilder b = new EmbedBuilder();
		event.getAuthor().getPremiumKeys().forEach(key -> {
			b.addField(
					DefaultLocaleString.COMMAND_KEYS_FIELD_HEAD.getFor(event.getSender(), "key", key.getID()), 
					DefaultLocaleString.COMMAND_KEYS_FIELD_VALUES.getFor(event.getSender(), 
							"type", key.getKeyType().getFriendlyTypeName().getFor(event.getSender()),
							"level", key.getPremiumLevel().getFriendlyName().getFor(event.getSender()),
							"description", key.getKeyType().getDescription().getFor(event.getSender()),
							"time", key.getFormattedExpirationTime(event.getSender()),
							"user", (key.isInUse() ? key.getRedeemedGuild().getName() : "nobody"))
					, false);
		});
		event.reply(b.build());
	}

	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}

}
