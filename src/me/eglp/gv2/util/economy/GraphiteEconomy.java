package me.eglp.gv2.util.economy;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "global_economy",
	columns = {
		"UserId varchar(255) NOT NULL",
		"Money int NOT NULL DEFAULT 0",
		"PRIMARY KEY(UserId)"
	}
)
public class GraphiteEconomy {

	public int getMoney(GraphiteUser user) {
		return Graphite.getMySQL().query(Integer.class, 0, "SELECT Money FROM global_economy WHERE UserId = ?", user.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to retrieve balance from MySQL", e));
	}

	public void setMoney(GraphiteUser user, int money) {
		Graphite.getMySQL().query("INSERT INTO global_economy(UserId, Money) VALUES(?, ?) ON DUPLICATE KEY UPDATE Money = VALUES(Money)", user.getID(), money);
	}

	public void addMoney(GraphiteUser user, int money) {
		setMoney(user, getMoney(user) + money);
	}

	public void withdrawMoney(GraphiteUser user, int money) {
		setMoney(user, getMoney(user) - money);
	}

	public boolean hasMoney(GraphiteUser user, int amount) {
		return getMoney(user) >= amount;
	}

}