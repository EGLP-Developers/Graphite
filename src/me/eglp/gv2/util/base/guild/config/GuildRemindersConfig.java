package me.eglp.gv2.util.base.guild.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.reminder.GuildReminder;
import me.eglp.gv2.util.base.guild.reminder.ReminderRepetition;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.misc.FriendlyException;

/**
 * The configuration class for reminders. Stores reminders in the database, loads them and keeps them cached.
 * 
 * @author The Arrayser
 * @date Tue Mar 28 17:26:31 2023
 */

@SQLTable(
	name = "guilds_reminders",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"`Id` varchar(255) NOT NULL",
		"ChannelId varchar(255) NOT NULL",
		"Message text NOT NULL",
		"`Repeat` varchar(255) DEFAULT NULL",
		"`Date` text NOT NULL",
		"PRIMARY KEY (GuildId, `Id`)"
	},
	guildReference = "GuildId"
)
public class GuildRemindersConfig {

	private GraphiteGuild guild;

	List<GuildReminder> cachedReminders = new ArrayList<>();

	public GuildRemindersConfig(GraphiteGuild guild) {
		this.guild = guild;
	}

	public void init() {
		getRemindersDB().forEach(p -> {
			if (!p.load()) {
				removeReminder(p.getID());
				return;
			}
			
			cachedReminders.add(p);
		});
	}

	public void saveReminder(GuildReminder reminder) {
		cachedReminders.add(reminder);
		Graphite.getMySQL().query(
				"INSERT INTO guilds_reminders(GuildId, `Id`, ChannelId, Message, `Repeat`, `Date`) VALUES(?, ?, ?, ?, ?, ?)",
				guild.getID(),
				reminder.getID(),
				reminder.getChannelID(),
				reminder.getMessage(),
				reminder.getRepeat() == null ? null : reminder.getRepeat().name(),
				reminder.getDate().toString());
	}

	public void removeReminder(String reminderID) {
		Graphite.getMySQL().query("DELETE FROM guilds_reminders WHERE GuildId = ? AND `Id` = ?", guild.getID(), reminderID);
		cachedReminders.removeIf(r -> r.getID().equals(reminderID));
	}

	private List<GuildReminder> getRemindersDB() {
		return Graphite.getMySQL().run(con -> {
			try (PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_reminders WHERE GuildId = ?")) {
				s.setString(1, guild.getID());
				try (ResultSet r = s.executeQuery()) {
					List<GuildReminder> reminders = new ArrayList<>();
					while (r.next()) {
						reminders.add(getReminder(r));
					}
					return reminders;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve reminders from MySQL", e));
	}

	public List<GuildReminder> getReminders() {
		return cachedReminders;
	}
	
	public GuildReminder getReminder(String reminderID) {
		return cachedReminders.stream()
			.filter(r -> r.getID().equals(reminderID))
			.findFirst().orElse(null);
	}

	private GuildReminder getReminder(ResultSet r) throws SQLException {
		ReminderRepetition repeat = null;
		String rawRepeat = r.getString("Repeat");
		if(rawRepeat != null) {
			try {
				repeat = ReminderRepetition.valueOf(rawRepeat);
			}catch(IllegalArgumentException ignored) {}
		}
		
		return new GuildReminder(guild,
				r.getString("Id"),
				r.getString("ChannelId"),
				r.getString("Message"),
				repeat,
				LocalDateTime.parse(r.getString("Date")));
	}
}
