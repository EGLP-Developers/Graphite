package me.eglp.gv2.commands.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.command.slash.CommandCompleter;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.input.multi.MultiInput;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.versioning.Beta;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

public class CommandTest extends ParentCommand {

	public CommandTest() {
		super(null, CommandCategory.FUN, "test");
		setDescription("Test command");
		setUsage("test");
		
		registerCompleter("test", CommandCompleter.ofString(event -> {
			return Arrays.asList("Amogus", "sus");
		}));
		
		registerCompleter("amogus", CommandCompleter.ofLong(event -> {
			return Arrays.asList(33l,44l);
		}));
		
		Command one = addSubCommand(new Command(this, "one") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				
//				GuildMessageChannel ch1 = event.getGuildChannel().getJDAChannel();
//				if(!(ch1 instanceof IThreadContainer)) return;
//				
//				IThreadContainer ch = (IThreadContainer) ch1;
//				
//				ThreadChannel th = ch.createThreadChannel("a").complete();
//				th.sendMessage("E").queue();
				
//				event.reply((String) event.getOption("test"));
				
//				event.reply("Yay: " + event.getOption("test") + ", " + event.getOption("amogus"));
//				event.getJDASlashCommandEvent().replyModal(Modal.create("a", "b")
//						.addActionRow(TextInput.create("t", "lbl", TextInputStyle.PARAGRAPH).build())
//						.addActionRow(TextInput.create("t2", "lbl", TextInputStyle.SHORT).build())
//						.addActionRow(TextInput.create("t3", "lbl", TextInputStyle.PARAGRAPH).build())
//						.addActionRow(TextInput.create("t4", "lbl", TextInputStyle.SHORT).build())
//						.build()).queue();
				
//				event.reply(new MessageBuilder()
//						.setContent("test")
//						.setActionRows(ActionRow.of(SelectMenu.create("E")
//								.addOption("Uppercase E", "E", "An uppercase letter E from the Latin alphabet", JDAEmote.ONE.getEmoji())
//								.addOption("lowercase e", "e", "A lowercase letter e from the Latin alphabet", JDAEmote.AMONG_US_RED_DEAD.getEmoji())
//								.setMinValues(0)
//								.setMaxValues(SelectMenu.OPTIONS_MAX_AMOUNT)
//								.setDefaultValues(Arrays.asList("e", "E"))
//								.build()))
//						.build());
				
				MultiInput in = new MultiInput(event.getAuthor());
				in.addSelectMenu("test", SelectOption.of("The letter e", "e"));
				in.newRow();
				in.addButton(ButtonStyle.DANGER, JDAEmote.ERROR.getEmoji(), e -> {
					System.out.println("Pressed!");
					e.markCancelled();
				});
				in.setOnSubmit(e -> {
					event.getChannel().sendMessage(e.getSelectMenuValues("test").toString());
					Attachment a = (Attachment) event.getOption("yayfile");
					event.getChannel().sendMessage(a != null ? a.getUrl() : "nope");
				});
				in.addSubmit(ButtonStyle.PRIMARY, JDAEmote.AMONG_US_BANANA.getEmoji());
				in.reply(event, DefaultMessage.COMMAND_ACCESSROLE_ADDED_ACCESSIBLE_ROLE);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
					new OptionData(OptionType.STRING, "test", "Desc", true, true),
					new OptionData(OptionType.INTEGER, "amogus", "An integer", true, true).setRequiredRange(5, 55),
					new OptionData(OptionType.ATTACHMENT, "yayfile", "Amogus file", false, false)
				);
			}
		});
		one.setDescription("Test command");
		one.setUsage("test");
		
		one.registerCompleter("test", CommandCompleter.ofString(event -> {
			return Arrays.asList("Amogus", "sus");
		}));
		
		one.registerCompleter("amogus", CommandCompleter.ofLong(event -> {
			return Arrays.asList(33l,44l);
		}));
		
		Command two = addSubCommand(new Command(this, "two") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				event.getGuild().getCustomCommandsConfig().getCustomCommands().forEach(cc -> {
					CommandCreateAction a = event.getGuild().getJDAGuild().upsertCommand(cc.getName(), "CustomCommand");
					a.addOptions(cc.getOptions());
					a.complete();
					System.out.println("CREATED " + cc.getName());
				});
				event.reply("Done");
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		});
		two.setDescription("Test");
	}
	
	@Beta
	@Override
	public void action(CommandInvokedEvent event) {
		super.action(event);
	}

}
