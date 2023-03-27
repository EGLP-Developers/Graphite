package me.eglp.gv2.util.webinterface.session;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.mysql.SQLTable;
import me.eglp.gv2.util.webinterface.base.GraphiteWebinterfaceUser;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "webinterface_sessions",
	columns = {
		"Id varchar(255) NOT NULL",
		"UserId varchar(255) NOT NULL",
		"ExpiresAt bigint NOT NULL",
		"Data text NOT NULL",
		"PRIMARY KEY (Id)"
	}
)
public class GraphiteMySQLSessionStorage {
	
	private static final long SESSION_TIMEOUT = 14 * 24 * 60 * 60 * 1000; // 14 days
	
	public WebinterfaceSession getSession(String sessionID) {
		WebinterfaceSession sess = loadSession(sessionID);
		if(sess != null && !sess.isValid()) {
			deleteSession(sessionID);
			return null;
		}
		return sess;
	}
	
	public void purgeSessions() {
		Graphite.getMySQL().query("DELETE FROM webinterface_sessions WHERE ExpiresAt < ?", System.currentTimeMillis());
	}
	
	private WebinterfaceSession loadSession(String sessionID) {
		WebinterfaceSession sess = Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM webinterface_sessions WHERE Id = ?")) {
				s.setString(1, sessionID);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return null;
					return new WebinterfaceSession(r.getString("Id"), r.getString("UserId"), r.getLong("ExpiresAt"), new JSONObject(r.getString("Data")));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve session from MySQL", e));
		if(sess != null) sess.load(); // To prevent nested MySQL requests
		return sess;
	}
	
	public void deleteSession(String sessionID) {
		Graphite.getMySQL().query("DELETE FROM webinterface_sessions WHERE Id = ?", sessionID);
	}
	
	public WebinterfaceSession createNew(GraphiteWebinterfaceUser user) {
		WebinterfaceSession sess = new WebinterfaceSession(randomSessionID(), user.getDiscordUser().getID(), System.currentTimeMillis() + SESSION_TIMEOUT, new JSONObject());
		Graphite.getMySQL().query("INSERT INTO webinterface_sessions(Id, UserId, ExpiresAt, Data) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE UserId = VALUES(UserId), ExpiresAt = VALUES(ExpiresAt)", sess.getID(), sess.getUserID(), sess.getExpiresAt(), sess.getData().toString());
		return sess;
	}
	
	public void updateData(String sessionID, JSONObject data) {
		Graphite.getMySQL().query("UPDATE webinterface_sessions SET Data = ? WHERE Id = ?", data.toString(), sessionID);
	}

	private String randomSessionID() {
		return UUID.randomUUID().toString();
	}

}
