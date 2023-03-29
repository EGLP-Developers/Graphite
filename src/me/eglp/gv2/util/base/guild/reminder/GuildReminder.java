package me.eglp.gv2.util.base.guild.reminder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
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
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

/**
 * This is the reminders executive class. It creates a Timer {@link ScheduledFuture} to execute, when the timer is due and send the appropriate message
 * 
 * @author The Arrayser
 * @date 2023.03
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
		//GuildReminder reminder = new GuildReminder(event.getGuild(), dateMs, message, repeatMs, channel);
		this.guild = guild;
		this.date = date;
		this.latestPossibleDate = date;
		this.message = message;
		this.repeatMs = repeatMs;
		this.channelID = channel.getID();
		this.id = GraphiteUtil.randomShortID();
	}
	
	public GuildReminder(GraphiteGuild guild, String id, LocalDateTime date, LocalDateTime latestPossibleDate, String message, ReminderRepetitionEnum repeatMs, GraphiteGuildMessageChannel channel) {
		//GuildReminder reminder = new GuildReminder(event.getGuild(), dateMs, message, repeatMs, channel);
		this.guild = guild;
		this.date = date;
		this.latestPossibleDate = latestPossibleDate;
		this.message = message;
		this.repeatMs = repeatMs;
		this.channelID = channel.getID();
		this.id = id;
	}
	
	/*"GuildId varchar(255) NOT NULL",
	"`Id` varchar(255) NOT NULL",
	"ChannelId varchar(255) NOT NULL",
	"Message text NOT NULL",
	"Repetition integer DEFAULT NULL",
	"Date text NOT NULL",
	"LatestPossibleDate text NOT NULL",*/
	
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
		//System.out.println("removing reminder from db");
		try {
			guild.getRemindersConfig().removeReminder(id);
			if(finishFuture != null) finishFuture.cancel(false);
			//if(!finishFuture.isCancelled()) {
			//	System.out.println("Did not get canceled!");
			//}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendMessage() {
		Graphite.withBot(Graphite.getGraphiteBot(), () -> {
			GraphiteGuildMessageChannel messageChannel = guild.getGuildMessageChannelByID(channelID);
			if(messageChannel == null) {
				//System.out.println("Channel was null\n");
				return;
			}else {
				//System.out.println("amogus");
			}
			//System.out.println(messageChannel.getJDAChannel() + "");
			try {
				if(repeatMs != null) {
					messageChannel.sendMessageComplete(repeatMs.getFriendlyName() + " Reminder: " + message);
				}else {
					messageChannel.sendMessageComplete("Simple Reminder: " + message);
				}
			}catch(Exception e) {
				//System.out.println(e.toString());
				//e.printStackTrace();
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
			
			latestPossibleDate = latestPossibleDate.plusMinutes(repeatMs.getMinutesDisplacement());
		}
	}
	
	public void enqueue() {
		//System.out.println("Delta Time: " + (latestPossibleDate.atZone(guild.getConfig().getTimezone()).toEpochSecond() - Instant.now().getEpochSecond()));
		//System.out.println(repeatMs.toString());
		
		finishFuture = Graphite.getScheduler().getExecutorService().schedule(() -> {
			//System.out.println("in schedule!");
			sendMessage();
			
			if(repeatMs != null) {
				//Todo reenqueue
				calculateNextPossibleReminderDate();
				//System.out.println("Next possible date: " + getLatestPossibleDate().toString());
				enqueue();
			}else {
				//Todo remove from db
				this.remove();
			}
		}, latestPossibleDate.atZone(guild.getConfig().getTimezone()).toEpochSecond() - Instant.now().getEpochSecond(), TimeUnit.SECONDS);
		//System.out.println("finishFuture is: " + finishFuture);
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
