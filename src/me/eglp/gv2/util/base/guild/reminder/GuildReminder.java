package me.eglp.gv2.util.base.guild.reminder;

import static java.lang.System.getLogger;
import static java.time.temporal.ChronoField.AMPM_OF_DAY;
import static java.time.temporal.ChronoField.CLOCK_HOUR_OF_AMPM;
import static java.time.temporal.ChronoField.CLOCK_HOUR_OF_DAY;
import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoField.HOUR_OF_AMPM;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.MICRO_OF_DAY;
import static java.time.temporal.ChronoField.MICRO_OF_SECOND;
import static java.time.temporal.ChronoField.MILLI_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_DAY;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;
import static java.time.temporal.ChronoField.SECOND_OF_DAY;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.lang.System.Logger;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.spi.BreakIteratorProvider;
import java.text.spi.CollatorProvider;
import java.text.spi.DateFormatProvider;
import java.text.spi.DateFormatSymbolsProvider;
import java.text.spi.DecimalFormatSymbolsProvider;
import java.text.spi.NumberFormatProvider;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DecimalStyle;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.spi.CalendarDataProvider;
import java.util.spi.CalendarNameProvider;
import java.util.spi.CurrencyNameProvider;
import java.util.spi.LocaleNameProvider;
import java.util.spi.LocaleServiceProvider;
import java.util.spi.TimeZoneNameProvider;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;

/**
 * This is the reminders executive class. It creates a Timer
 * {@link ScheduledFuture} to execute, when the timer is due and send the
 * appropriate message
 * 
 * @author The Arrayser
 * @date Mon Mar 27 20:11:34 2023
 */

@JavaScriptClass(name = "GuildReminder")
public class GuildReminder implements WebinterfaceObject {
	
	private static final DateTimeFormatter ISO_LOCAL_TIME_EDIT;
    static {
    	DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2);
    	
    	DateTimeFormatter temp = null;
    	
    	try {
    		Class c = Class.forName(DateTimeFormatterBuilder.class.getName());
    		Class parameterTypes[] = new Class[2];
    		parameterTypes[0] = ResolverStyle.class;
    		parameterTypes[1] = Chronology.class;
    		Method theToStringMethod = c.getDeclaredMethod("toFormatter", parameterTypes);
    		theToStringMethod.setAccessible(true);
    		temp = (DateTimeFormatter) theToStringMethod.invoke(builder, ResolverStyle.STRICT, null);
    	}catch(Throwable e) {
    		e.printStackTrace();
    		//throw e; // Java does not allow throws anymore, for some reason
    	}
    	ISO_LOCAL_TIME_EDIT = temp;
        
        //ISO_LOCAL_TIME_EDIT = toFormatter(builder, ResolverStyle.STRICT, null);
    }
	
	public static final DateTimeFormatter HUMAN_TIMESTAMP_FORMAT; // = DateTimeFormatter.ofPattern("EEE, dd LLL yyyy HH:mm z");
	
    static {
    	
    	DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral(' ')
                .append(ISO_LOCAL_TIME_EDIT);
    	
    	DateTimeFormatter temp = null;
    	
    	try {
    		Class c = Class.forName(DateTimeFormatterBuilder.class.getName());
    		Class parameterTypes[] = new Class[2];
    		parameterTypes[0] = ResolverStyle.class;
    		parameterTypes[1] = Chronology.class;
    		Method theToStringMethod = c.getDeclaredMethod("toFormatter", parameterTypes);
    		theToStringMethod.setAccessible(true);
    		temp = (DateTimeFormatter) theToStringMethod.invoke(builder, ResolverStyle.STRICT, IsoChronology.INSTANCE);
    	}catch(Exception e) {
    		e.printStackTrace();
    		//throw e; // Java does not allow throws anymore, for some reason
    	}
    	
    	
    	
    	HUMAN_TIMESTAMP_FORMAT = temp;
    	
    	//HUMAN_TIMESTAMP_FORMAT = toFormatter(builder, ResolverStyle.STRICT, IsoChronology.INSTANCE);
    }

	private GraphiteGuild guild;
	private String id;

	private String channelID;
	private String message;
	private ReminderRepetitionEnum repeatMs;
	private LocalDateTime date;

	private LocalDateTime latestPossibleDate;
	private ScheduledFuture<?> finishFuture;

	public GuildReminder(GraphiteGuild guild, LocalDateTime date, String message, ReminderRepetitionEnum repeatMs,
			GraphiteGuildMessageChannel channel) {
		this.guild = guild;
		this.date = date;
		this.latestPossibleDate = date;
		this.message = message;
		this.repeatMs = repeatMs;
		this.channelID = channel.getID();
		this.id = GraphiteUtil.randomShortID();
	}

	public GuildReminder(GraphiteGuild guild, String id, LocalDateTime date, LocalDateTime latestPossibleDate,
			String message, ReminderRepetitionEnum repeatMs, GraphiteGuildMessageChannel channel) {
		this.guild = guild;
		this.date = date;
		this.latestPossibleDate = latestPossibleDate;
		this.message = message;
		this.repeatMs = repeatMs;
		this.channelID = channel.getID();
		this.id = id;
	}

	public GuildReminder(GraphiteGuild guild, String id, String channelID, String message,
			ReminderRepetitionEnum repeatMs, LocalDateTime date, LocalDateTime latestPossibleDate) {
		this.guild = guild;
		this.id = id;
		this.channelID = channelID;
		this.message = message;
		this.repeatMs = repeatMs;
		this.date = date;
		this.latestPossibleDate = latestPossibleDate;
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

	public String getId() {
		return id;
	}

	public ReminderRepetitionEnum getRepeatMs() {
		return repeatMs;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public LocalDateTime getLatestPossibleDate() {
		return latestPossibleDate;
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
				this.remove();
				return;
			}
			try {
				if (repeatMs != null) {
					messageChannel.sendMessageComplete(repeatMs.getFriendlyName() + " Reminder: " + message);
				} else {
					messageChannel.sendMessageComplete("Simple Reminder: " + message);
				}
			} catch (Exception e) {
				throw e;
			}
		});
	}

	private void calculateNextPossibleReminderDate() {
		LocalDateTime now = LocalDateTime.now(guild.getConfig().getTimezone());// Current time with correct UTC for
																				// guild
		while (!latestPossibleDate.isAfter(now)) {
			// maybe
			latestPossibleDate = latestPossibleDate.plusYears(repeatMs.getYearsDisplacement());
			latestPossibleDate = latestPossibleDate.plusMonths(repeatMs.getMonthsDisplacement());
			latestPossibleDate = latestPossibleDate.plusWeeks(repeatMs.getWeeksDisplacement());
			latestPossibleDate = latestPossibleDate.plusDays(repeatMs.getDaysDisplacement());
		}
	}

	public void enqueue() {
		finishFuture = Graphite.getScheduler().getExecutorService().schedule(() -> {
			sendMessage();

			if (repeatMs != null) {
				// Todo reenqueue
				calculateNextPossibleReminderDate();
				enqueue();
			} else {
				// Todo remove from db
				this.remove();
			}
		}, latestPossibleDate.atZone(guild.getConfig().getTimezone()).toEpochSecond() - Instant.now().getEpochSecond(),
				TimeUnit.SECONDS);
	}

	public boolean load() {
		if (this.repeatMs == null)
			return false;
		calculateNextPossibleReminderDate();
		enqueue();
		return true;
	}

	@Override
	public void preSerializeWI(JSONObject object) {
	}

	@JavaScriptFunction(calling = "getReminders", returning = "reminders", withGuild = true)
	public static void getReminders() {
	};

	@JavaScriptFunction(calling = "finishReminder", withGuild = true)
	public static void finishReminder(@JavaScriptParameter(name = "id") String id) {
	}

}
