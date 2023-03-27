package me.eglp.gv2.util.game;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "users_minigame_stats",
	columns = {
		"UserId varchar(255) NOT NULL",
		"Minigame varchar(255) NOT NULL",
		"Wins int DEFAULT NULL",
		"Losses int DEFAULT NULL",
		"PRIMARY KEY (UserId, Minigame)"
	}
)
public class GraphiteMinigameStats {
	
	public void addUserWin(GraphiteMinigame game, GraphiteUser user) {
		Graphite.getMySQL().query("INSERT IGNORE INTO users_minigame_stats(UserId, Minigame, Wins, Losses) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE Wins = Wins + 1", user.getID(), game.name(), 1, 0);
	}

	public void addUserLoss(GraphiteMinigame game, GraphiteUser user) {
		Graphite.getMySQL().query("INSERT IGNORE INTO users_minigame_stats(UserId, Minigame, Wins, Losses) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE Losses = Losses + 1", user.getID(), game.name(), 0, 1);
	}
	
	public int getUserWins(GraphiteMinigame game, GraphiteUser user) {
		return Graphite.getMySQL().query(Integer.class, 0, "SELECT Wins FROM users_minigame_stats WHERE UserId = ? AND Minigame = ?", user.getID(), game.name())
				.orElseThrowOther(e -> new FriendlyException("Failed to load wins from MySQL", e));
	}
	
	public int getUserLosses(GraphiteMinigame game, GraphiteUser user) {
		return Graphite.getMySQL().query(Integer.class, 0, "SELECT Losses FROM users_minigame_stats WHERE UserId = ? AND Minigame = ?", user.getID(), game.name())
				.orElseThrowOther(e -> new FriendlyException("Failed to load losses from MySQL", e));
	}
	
}
