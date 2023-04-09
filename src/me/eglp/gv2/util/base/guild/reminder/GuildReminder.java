package me.eglp.gv2.util.base.guild.reminder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Represents a reminder on a guild, which sends a message when the reminder expires. The reminder can optionally repeat.
 * 
 * @author The Arrayser
 * @date Mon Mar 27 20:11:34 2023
 */
public class GuildReminder {
	
	public static final DateTimeFormatter HUMAN_TIMESTAMP_FORMAT;

	static {
		HUMAN_TIMESTAMP_FORMAT = new DateTimeFormatterBuilder()
			.parseCaseInsensitive()
			.append(DateTimeFormatter.ISO_LOCAL_DATE)
			.appendLiteral(' ')
			.appendValue(ChronoField.HOUR_OF_DAY, 2)
			.appendLiteral(':')
			.appendValue(ChronoField.MINUTE_OF_HOUR, 2)
			.toFormatter();
	}

	private GraphiteGuild guild;
	private String id;

	private String channelID;
	private String message;
	private ReminderRepetition repeat;
	private LocalDateTime date;

	private LocalDateTime nextReminderDate;
	private ScheduledFuture<?> finishFuture;

	public GuildReminder(GraphiteGuildMessageChannel channel, String message, ReminderRepetition repeat, LocalDateTime date) {
		this.guild = channel.getGuild();
		this.date = date;
		this.nextReminderDate = date;
		this.message = message;
		this.repeat = repeat;
		this.channelID = channel.getID();
		this.id = GraphiteUtil.randomShortID();
	}

	public GuildReminder(GraphiteGuild guild, String id, String channelID, String message, ReminderRepetition repeat, LocalDateTime date) {
		this.guild = guild;
		this.id = id;
		this.channelID = channelID;
		this.message = message;
		this.repeat = repeat;
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public GraphiteGuild getGuild() {
		return guild;
	}

	public String getChannelID() {
		return channelID;
	}

	public String getID() {
		return id;
	}

	public ReminderRepetition getRepeat() {
		return repeat;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public LocalDateTime getLatestPossibleDate() {
		return nextReminderDate;
	}

	public ScheduledFuture<?> getFinishFuture() {
		return finishFuture;
	}

	public void remove() {
		try {
			guild.getRemindersConfig().removeReminder(id);
			if (finishFuture != null)
				finishFuture.cancel(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendMessage() {
		Graphite.withBot(Graphite.getGraphiteBot(), () -> {
			GraphiteGuildMessageChannel messageChannel = guild.getGuildMessageChannelByID(channelID);
			if (messageChannel == null) {
				remove();
				return;
			}
			
			try {
				EmbedBuilder b = new EmbedBuilder();
				b.setTitle(DefaultLocaleString.COMMAND_REMINDER_MESSAGE_TITLE.getFor(guild));
				
				if (repeat != null) {
					b.setDescription(DefaultLocaleString.COMMAND_REMINDER_MESSAGE_REPEATING.getFor(guild,
						"repeat", repeat.getFriendlyName(),
						"message", message));
					b.setFooter(DefaultLocaleString.COMMAND_REMINDER_MESSAGE_REPEATING_FOOTER.getFor(guild, "reminder_id", getID()));
				} else {
					b.setDescription(DefaultLocaleString.COMMAND_REMINDER_MESSAGE_SIMPLE.getFor(guild,
						"message", message));
				}
				
				messageChannel.sendMessage(b.build());
			} catch (Exception e) {
				throw e;
			}
		});
	}

	private void calculateNextReminderDate() {
		// Get time at guild's timezone
		LocalDateTime now = LocalDateTime.now(guild.getConfig().getTimezone());
		if(nextReminderDate == null) nextReminderDate = date;
		
		// Calculate the next repeat date in the future
		while (!nextReminderDate.isAfter(now)) {
			nextReminderDate = nextReminderDate
				.plusYears(repeat.getYears())
				.plusMonths(repeat.getMonths())
				.plusWeeks(repeat.getWeeks())
				.plusDays(repeat.getDays());
		}
	}

	public void schedule() {
		Instant nextReminder = nextReminderDate.atZone(guild.getConfig().getTimezone()).toInstant();
		
		finishFuture = Graphite.getScheduler().getExecutorService().schedule(() -> {
			sendMessage();

			if (repeat != null) {
				calculateNextReminderDate();
				schedule();
			} else {
				remove();
			}
		}, nextReminder.getEpochSecond() - Instant.now().getEpochSecond(), TimeUnit.SECONDS);
	}

	public boolean load() {
		if (repeat == null) return false;
		
		calculateNextReminderDate();
		schedule();
		return true;
	}

}
