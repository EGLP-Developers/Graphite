package me.eglp.gv2.util.base.guild.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.reminder.GuildReminder;
import me.eglp.gv2.util.base.guild.reminder.ReminderRepetitionEnum;
//import me.eglp.gv2.util.base.guild.reminder.ReminderOption;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.SerializationOption;
import me.mrletsplay.mrcore.misc.FriendlyException;

/**
 * This is the reminders Storage class, which also contains an interface to the DataBase for persistent reminder storage
 * 
 * @author The Arrayser
 * @date 2023.03
 */

//private ReminderRepetitionEnum repeatMs;
//private LocalDateTime date;
//private LocalDateTime latestPossibleDate;

@SQLTable(
	name = "guilds_reminders",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"`Id` varchar(255) NOT NULL",
		"ChannelId varchar(255) NOT NULL",
		"Message text NOT NULL",
		"Repetition integer DEFAULT NULL",
		"Date text NOT NULL",
		"LatestPossibleDate text NOT NULL",
		
		"PRIMARY KEY (GuildId, `Id`)"
	},
	guildReference = "GuildId"
)

public class GuildRemindersConfig {
	
	private GraphiteGuild guild;
	
	List<GuildReminder> tempGuildReminders = new ArrayList<>();
	
	public GuildRemindersConfig(GraphiteGuild guild) {
		this.guild = guild;
	}
	
	public void init() {
		getRemindersDB().forEach(p -> {
			if(p.load() == false) {
				removeReminder(p.getId());
				return;
			}
			tempGuildReminders.add(p);
		});
	}
	
	public void saveReminder(GuildReminder reminder) {
		tempGuildReminders.add(reminder);
		Graphite.getMySQL().query("INSERT INTO guilds_reminders(GuildId, `Id`, ChannelId, Message, Repetition, Date, LatestPossibleDate) VALUES(?, ?, ?, ?, ?, ?, ?)",
				guild.getID(),
				reminder.getId(),
				reminder.getChannelID(),
				reminder.getMessage(),
				(reminder.getRepeatMs() == null) ? null : reminder.getRepeatMs().ordinal(),
				reminder.getDate().toString(),
				reminder.getLatestPossibleDate().toString());
	}
	
	public void removeReminder(String reminderID) {
		Graphite.getMySQL().query("DELETE FROM guilds_reminders WHERE GuildId = ? AND `Id` = ?", guild.getID(), reminderID);
		tempGuildReminders = tempGuildReminders.stream().filter(i -> i.getId() != reminderID).toList();
	}
	
	private List<GuildReminder> getRemindersDB() {
		//return new ArrayList<GuildReminder>();
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_reminders WHERE GuildId = ?")) {
				s.setString(1, guild.getID());
				try(ResultSet r = s.executeQuery()) {
					List<GuildReminder> reminders = new ArrayList<>();
					while(r.next()) {
						reminders.add(getReminder(r));
					}
					return reminders;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve reminders from MySQL", e));
	}
	
	public List<GuildReminder> getReminders(){
		return tempGuildReminders;
	}
	
	private GuildReminder getReminderDB(String reminderID) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_reminders WHERE GuildId = ? AND `Id` = ?")) {
				s.setString(1, guild.getID());
				s.setString(2, reminderID);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return null;
					return getReminder(r);
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve reminder from MySQL", e));
	}
	
	public GuildReminder getReminder(String reminderID) {
		for(GuildReminder a : tempGuildReminders) {
			if(a.getId().equals(reminderID)) {
				return a;
			}
		}
		throw new FriendlyException("No such item in the Reminders Buffer", new Exception("List did not contain that item"));
	}
	

	/*"GuildId varchar(255) NOT NULL",
	"`Id` varchar(255) NOT NULL",
	"ChannelId varchar(255) NOT NULL",
	"Message text NOT NULL",
	"Repetition integer DEFAULT NULL",
	"Date text NOT NULL",
	"LatestPossibleDate text NOT NULL",*/
	
	private GuildReminder getReminder(ResultSet r) throws SQLException {
		ReminderRepetitionEnum nRepeat = null;
		try {
			int repetitionTemp = r.getInt("Repetition"); // may be null.. does this line cause an exception?
			nRepeat = ReminderRepetitionEnum.values()[repetitionTemp];
		}catch(Exception e) {}
		return new GuildReminder(guild, r.getString("Id"), r.getString("ChannelId"), r.getString("Message"), nRepeat, LocalDateTime.parse(r.getString("Date")) , LocalDateTime.parse(r.getString("LatestPossibleDate")));
	}
}
