package me.eglp.gv2.util.base.guild.reminder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
 * This is the reminders executive class. It creates a Timer {@link ScheduledFuture} to execute, when the timer is due and send the appropriate message
 * 
 * @author The Arrayser
 * @date Mon Mar 27 20:11:34 2023
 */

@JavaScriptClass(name = "GuildReminder")
public class GuildReminder implements WebinterfaceObject{
	
	//private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("EEE, dd LLL yyyy HH:mm z");
	
	private GraphiteGuild guild;
	private String id;
	
	private String channelID;
	private String message;
	private ReminderRepetitionEnum repeatMs;
	private LocalDateTime date;
	
	private LocalDateTime latestPossibleDate;
	private ScheduledFuture<?> finishFuture;
	
	public GuildReminder(GraphiteGuild guild, LocalDateTime date, String message, ReminderRepetitionEnum repeatMs, GraphiteGuildMessageChannel channel) {
		this.guild = guild;
		this.date = date;
		this.latestPossibleDate = date;
		this.message = message;
		this.repeatMs = repeatMs;
		this.channelID = channel.getID();
		this.id = GraphiteUtil.randomShortID();
	}
	
	public GuildReminder(GraphiteGuild guild, String id, LocalDateTime date, LocalDateTime latestPossibleDate, String message, ReminderRepetitionEnum repeatMs, GraphiteGuildMessageChannel channel) {
		this.guild = guild;
		this.date = date;
		this.latestPossibleDate = latestPossibleDate;
		this.message = message;
		this.repeatMs = repeatMs;
		this.channelID = channel.getID();
		this.id = id;
	}
	
	public GuildReminder(GraphiteGuild guild, String id, String channelID, String message, ReminderRepetitionEnum repeatMs, LocalDateTime date, LocalDateTime latestPossibleDate) {
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
			if(finishFuture != null) finishFuture.cancel(false);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendMessage() {
		Graphite.withBot(Graphite.getGraphiteBot(), () -> {
			GraphiteGuildMessageChannel messageChannel = guild.getGuildMessageChannelByID(channelID);
			if(messageChannel == null) {
				this.remove();
				return;
			}else {}
			try {
				if(repeatMs != null) {
					messageChannel.sendMessageComplete(repeatMs.getFriendlyName() + " Reminder: " + message);
				}else {
					messageChannel.sendMessageComplete("Simple Reminder: " + message);
				}
			}catch(Exception e) {
				throw e;
			}
		});
	}
	
	private void calculateNextPossibleReminderDate() {
		LocalDateTime now = LocalDateTime.now(guild.getConfig().getTimezone());// Current time with correct UTC for guild
		while(!latestPossibleDate.isAfter(now)) {
			//maybe
			latestPossibleDate = latestPossibleDate.plusYears(repeatMs.getYearsDisplacement());
			latestPossibleDate = latestPossibleDate.plusMonths(repeatMs.getMonthsDisplacement());
			latestPossibleDate = latestPossibleDate.plusWeeks(repeatMs.getWeeksDisplacement());
			latestPossibleDate = latestPossibleDate.plusDays(repeatMs.getDaysDisplacement());
		}
	}
	
	public void enqueue() {
		finishFuture = Graphite.getScheduler().getExecutorService().schedule(() -> {
			sendMessage();
			
			if(repeatMs != null) {
				//Todo reenqueue
				calculateNextPossibleReminderDate();
				enqueue();
			}else {
				//Todo remove from db
				this.remove();
			}
		}, latestPossibleDate.atZone(guild.getConfig().getTimezone()).toEpochSecond() - Instant.now().getEpochSecond(), TimeUnit.SECONDS);
	}
	
	public boolean load() {
		if(this.repeatMs == null) return false;
		calculateNextPossibleReminderDate();
		enqueue();
		return true;
	}
	
	@Override
	public void preSerializeWI(JSONObject object) {		
		object.put("guild", getGuild());
		object.put("id", getId());
		
		object.put("channelID", getChannelID());
		object.put("message", getMessage());
		object.put("repeatMs", getRepeatMs());
		object.put("date", getDate());
		
		object.put("latestPossibleDate", getLatestPossibleDate());
		object.put("finishFuture", getFinishFuture());
	}
	
	@JavaScriptFunction(calling = "getReminders", returning = "reminders", withGuild = true)
	public static void getReminders() {};
	
	@JavaScriptFunction(calling = "finishReminder", withGuild = true)
	public static void finishReminder(@JavaScriptParameter(name = "id") String id) {}
	
}
