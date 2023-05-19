package me.eglp.gv2.user;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.mysql.SQLTable;

@SQLTable(
	name = "users_blocked_scripts",
	columns = {
		"UserId varchar(255) NOT NULL",
		"GuildId varchar(255) NOT NULL",
		"PRIMARY KEY (GuildId, UserId)"
	},
	guildReference = "GuildId"
)
@SQLTable(
	name = "users_eastereggs",
	columns = {
		"UserId varchar(255) NOT NULL",
		"EasterEgg varchar(255) NOT NULL",
		"PRIMARY KEY (UserId, EasterEgg)"
	}
)
public class UserConfig {

	private GraphiteUser user;

	public UserConfig(GraphiteUser user) {
		this.user = user;
	}

	public GraphiteUser getUser() {
		return user;
	}

	public void addEasterEgg(EasterEgg easterEgg, boolean sendMessage) {
		if(hasFoundEasterEgg(easterEgg)) return;

		if(sendMessage) {
			GraphitePrivateChannel ch = user.openPrivateChannel();
			if(ch == null) return;
			DefaultMessage.OTHER_FOUND_AN_EASTEREGG.sendMessage(ch);
		}

		Graphite.getMySQL().query("INSERT INTO users_eastereggs(UserId, EasterEgg) VALUES(?, ?)", user.getID(), easterEgg.name());
	}

	public boolean hasFoundEasterEgg(EasterEgg easterEgg) {
		return getFoundEasterEggs().contains(easterEgg);
	}

	public List<EasterEgg> getFoundEasterEggs() {
		return Graphite.getMySQL().queryArray(String.class, "SELECT EasterEgg FROM users_eastereggs WHERE UserId = ?", user.getID()).orElse(Collections.emptyList()).stream()
				.map(s -> {
					try {
						return EasterEgg.valueOf(s);
					} catch(Exception e) {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public void addBlockedGuild(GraphiteGuild guild) {
		if(isGuildBlocked(guild)) return;
		Graphite.getMySQL().query("INSERT INTO users_blocked_scripts(UserId, GuildId) VALUES(?, ?)", user.getID(), guild.getID());
	}

	public void removeBlockedGuild(GraphiteGuild guild) {
		Graphite.getMySQL().query("DELETE FROM users_blocked_scripts WHERE UserId = ? AND GuildId = ?", user.getID(), guild.getID());
	}

	public void removeAllBlockedGuilds() {
		Graphite.getMySQL().query("DELETE FROM users_blocked_scripts WHERE UserId = ?", user.getID());
	}

	public List<GraphiteGuild> getBlockedGuilds() {
		return Graphite.getMySQL().queryArray(String.class, "SELECT GuildId FROM users_blocked_scripts WHERE UserId = ?", user.getID()).orElse(Collections.emptyList()).stream()
				.map(Graphite::getGuild)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public boolean isGuildBlocked(GraphiteGuild guild) {
		return getBlockedGuilds().contains(guild);
	}

}
