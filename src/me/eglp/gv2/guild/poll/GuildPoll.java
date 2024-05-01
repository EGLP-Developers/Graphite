package me.eglp.gv2.guild.poll;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteTextChannel;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.input.ButtonInput;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

@JavaScriptClass(name = "GuildPoll")
public class GuildPoll implements WebinterfaceObject{

	private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("EEE, dd LLL yyyy HH:mm z");

	private GraphiteGuild guild;
	private String id;
	private String channelID;
	private String messageID;
	private String question;
	private long expiresAt;
	private boolean allowMultipleVotes;
	private List<PollOption> options;

	private ButtonInput<String> input;
	private long lastUpdate;
	private boolean updateQueued;
	private boolean finished;
	private ScheduledFuture<?> finishFuture;

	public GuildPoll(GraphiteGuild guild, String id, String channelID, String messageID, String question, long expiresAt, boolean allowMultipleVotes, List<PollOption> options) {
		this.guild = guild;
		this.id = id;
		this.channelID = channelID;
		this.messageID = messageID;
		this.question = question;
		this.expiresAt = expiresAt;
		this.allowMultipleVotes = allowMultipleVotes;
		this.options = options;
	}

	public GuildPoll(GraphiteGuild guild, String question, long expiresAt, boolean allowMultipleVotes) {
		this(guild, GraphiteUtil.randomShortID(), null, null, question, expiresAt, allowMultipleVotes, new ArrayList<>());
	}

	public String getID() {
		return id;
	}

	public String getChannelID() {
		return channelID;
	}

	public String getMessageID() {
		return messageID;
	}

	public String getQuestion() {
		return question;
	}

	public long getExpiresAt() {
		return expiresAt;
	}

	public boolean isAllowMultipleVotes() {
		return allowMultipleVotes;
	}

	public void addOption(String id, Emoji emoji, String name) {
		options.add(new PollOption(id, emoji, name));
	}

	public List<PollOption> getOptions() {
		return options;
	}

	public void updateMessage() {
		if(finished) return;
		// Update immediately if possible, but not more than every 10s
		if(System.currentTimeMillis() - lastUpdate < 10 * 1000) {
			if(updateQueued) return;
			updateQueued = true;
			Graphite.getScheduler().getExecutorService().schedule(() -> {
				lastUpdate = System.currentTimeMillis();
				updateQueued = false;
				updateMessage0(false);
			}, 10, TimeUnit.SECONDS);
			return;
		}

		lastUpdate = System.currentTimeMillis();
		updateMessage0(false);
	}

	private void updateMessage0(boolean showResults) {
		GraphiteTextChannel ch = guild.getTextChannelByID(channelID);
		if(ch == null) {
			remove();
			return;
		}

		MessageCreateBuilder b = createMessage(showResults);
		if(!showResults) b.setComponents(input.createActionRows());
		MessageEditBuilder edit = MessageEditBuilder.fromCreateData(b.build());
		ch.getJDAChannel().editMessageById(messageID, edit.build()).queue(null, new ErrorHandler()
				.handle(ErrorResponse.UNKNOWN_MESSAGE, ex -> remove()));
	}

	private void remove() {
		guild.getPollsConfig().removePoll(id);
		if(input != null) input.remove();
		if(finishFuture != null) finishFuture.cancel(false);
	}

	public void finish() {
		if(finished) return;
		finished = true;
		updateMessage0(true);
		remove();
	}

	private ButtonInput<String> createButtonInput() {
		input = new ButtonInput<String>(ev -> {
			if(expiresAt < System.currentTimeMillis()) return;

			PollOption op = options.stream()
					.filter(o -> o.getID().equals(ev.getItem()))
					.findFirst().orElse(null);
			if(op == null) return;

			int opIdx = options.indexOf(op);

			if(allowMultipleVotes && guild.getPollsConfig().hasVoted(id, ev.getUser().getID(), opIdx)) {
				ev.getJDAEvent().deferReply(true).setContent(DefaultLocaleString.OTHER_POLL_ALREADY_VOTED_OPTION.getFor(guild)).queue();
				return;
			}else if(!allowMultipleVotes && guild.getPollsConfig().hasVoted(id, ev.getUser().getID())){
				ev.getJDAEvent().deferReply(true).setContent(DefaultLocaleString.OTHER_POLL_ALREADY_VOTED.getFor(guild)).queue();
				return;
			}

			guild.getPollsConfig().addPollVote(id, ev.getUser().getID(), opIdx);
			updateMessage();
			ev.getJDAEvent().deferReply(true).setContent(DefaultLocaleString.OTHER_POLL_SUCCESS.getFor(guild, "option", op.getName())).queue();
		})
		.autoRemove(false)
		.removeMessage(false)
		.editOnExpire(false)
		.expireAfter(expiresAt - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

		for(int i = 0; i < options.size(); i++) {
			PollOption op = options.get(i);
			input.addOptionRaw(Button.of(ButtonStyle.PRIMARY, op.getID(), op.getEmoji()), op.getID());
			if((i + 1) % 5 == 0 && i != options.size() - 1) input.newRow();
		}

		return input;
	}

	@SuppressWarnings("null")
	private MessageCreateBuilder createMessage(boolean showResults) {
		ZonedDateTime endTime = Instant.ofEpochMilli(expiresAt).atZone(guild.getConfig().getTimezone());

		Map<Integer, Integer> results = showResults ? guild.getPollsConfig().getPollResults(id) : null;
		EmbedBuilder b = new EmbedBuilder();
		b.addField(DefaultLocaleString.COMMAND_POLL_CREATE_MESSAGE_QUESTION_TITLE.getFor(guild), question, false);
		b.addField(DefaultLocaleString.COMMAND_POLL_CREATE_MESSAGE_MULTIPLE_VOTES_TITLE.getFor(guild), allowMultipleVotes ? DefaultLocaleString.COMMAND_POLL_CREATE_MESSAGE_MULTIPLE_VOTES_YES.getFor(guild) : DefaultLocaleString.COMMAND_POLL_CREATE_MESSAGE_MULTIPLE_VOTES_NO.getFor(guild), true);
		b.addField(DefaultLocaleString.COMMAND_POLL_CREATE_MESSAGE_POLL_END_TITLE.getFor(guild), endTime.format(TIMESTAMP_FORMAT), true);
		b.addField(DefaultLocaleString.COMMAND_POLL_CREATE_MESSAGE_OPTIONS_TITLE.getFor(guild), options.stream().map(op -> op.getEmoji().getFormatted() + " - ``" + op.getName().replace("``", "\u200B`\u200B`\u200B") + "``" + (showResults ? " - " + results.getOrDefault(options.indexOf(op), 0) + " vote(s)" : "")).collect(Collectors.joining("\n")), false);
		b.setFooter(DefaultLocaleString.COMMAND_POLL_CREATE_MESSAGE_TOTAL.getFor(guild, "votes", String.valueOf(guild.getPollsConfig().getVoteCount(id)), "users", String.valueOf(guild.getPollsConfig().getVoteUserCount(id)), "poll_id", id));

		return new MessageCreateBuilder().setEmbeds(b.build());
	}

	public void send(GraphiteTextChannel channel) {
		Message m = createButtonInput().sendComplete(channel, createMessage(false));
		channelID = channel.getID();
		messageID = m.getId();
		finishFuture = Graphite.getScheduler().getExecutorService().schedule(() -> finish(), expiresAt - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	public void load() {
		if(expiresAt < System.currentTimeMillis()) {
			finish();
			return;
		}

		createButtonInput().registerHandler();
		finishFuture = Graphite.getScheduler().getExecutorService().schedule(() -> finish(), expiresAt - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("pollID", getID());
		object.put("pollQuestion", getQuestion());
		object.put("channelID", getChannelID());
		object.put("pollAllowMultipleVotes", isAllowMultipleVotes());
		object.put("pollExpiresAt", getExpiresAt());
		object.put("pollOptions", new JSONArray(getOptions().stream().map(o -> o.toWebinterfaceObject()).toList()));
	}

	@JavaScriptFunction(calling = "getPolls", returning = "polls", withGuild = true)
	public static void getPolls() {};

	@JavaScriptFunction(calling = "finishPoll", withGuild = true)
	public static void finishPoll(@JavaScriptParameter(name = "id") String id) {}

}
