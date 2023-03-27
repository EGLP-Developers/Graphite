package me.eglp.gv2.commands.premium;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.command.SpecialExecute;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.premium.PremiumKey;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandPremium extends ParentCommand {
	
	public CommandPremium() {
		super(null, CommandCategory.PREMIUM, "premium");
		setDescription(DefaultLocaleString.COMMAND_PREMIUM_DESCRIPTION);
		
		addSubCommand(new Command(this, "redeem") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				String key = (String) event.getOption("key");
				PremiumKey k = Graphite.getPremium().getKey(key);
				if(k == null) {
					DefaultMessage.COMMAND_PREMIUM_REDEEM_INVALID_KEY.reply(event);
					return;
				}
				
				if(event.getGuild().getPremiumLevel().equals(k.getPremiumLevel())) {
					DefaultMessage.COMMAND_PREMIUM_REDEEM_KEY_MATCH.reply(event);
					return;
				}
				
				if(k.isInUse()) {
					DefaultMessage.COMMAND_PREMIUM_REDEEM_KEY_IN_USE.reply(event);
					return;
				}
				
				event.getGuild().redeemPremiumKey(k);
				DefaultMessage.COMMAND_PREMIUM_REDEEM_KEY_REDEEMED.reply(event, 
						"level", k.getPremiumLevel().getFriendlyName().getFor(event.getSender()));
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "key", "The key to redeem", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_PREMIUM_REDEEM_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_PREMIUM_REDEEM_USAGE)
		.setPermission(DefaultPermissions.PREMIUM_REDEEM);
		
		addSubCommand(new Command(this, "addkey") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				String key = (String) event.getOption("key");
				PremiumKey k = Graphite.getPremium().getKey(key);
				if(k == null) {
					DefaultMessage.COMMAND_PREMIUM_ADDKEY_INVALID_KEY.reply(event);
					return;
				}
				
				if(event.getAuthor().redeemKey(k)) {
					DefaultMessage.COMMAND_PREMIUM_ADDKEY_ADDED.reply(event,
							"key", key);
				}else {
					DefaultMessage.COMMAND_PREMIUM_ADDKEY_KEY_ALREADY_REDEEMED.reply(event);
				}
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "key", "The key to claim", true)
					);
			}
		})
		.setAllowPrivate(true)
		.setAllowServer(false)
		.setDescription(DefaultLocaleString.COMMAND_PREMIUM_ADDKEY_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_PREMIUM_ADDKEY_USAGE);
		
		addSubCommand(new Command(this, "level") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				DefaultMessage.COMMAND_PREMIUM_LEVEL_MESSAGE.reply(event,
						"level", event.getGuild().getPremiumLevel().getFriendlyName().getFor(event.getSender()));
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_PREMIUM_LEVEL_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_PREMIUM_LEVEL_USAGE)
		.setPermission(DefaultPermissions.PREMIUM_LEVEL);
	}
	
	@SpecialExecute(allowPrivate = true)
	@Override
	public void action(CommandInvokedEvent event) {
		super.action(event);
	}

}
