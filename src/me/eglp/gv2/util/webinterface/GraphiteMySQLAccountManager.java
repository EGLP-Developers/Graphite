package me.eglp.gv2.util.webinterface;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.mysql.SQLTable;
import me.eglp.gv2.util.webinterface.base.GraphiteWebinterfaceGuild;
import me.eglp.gv2.util.webinterface.base.GraphiteWebinterfaceUser;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "webinterface_users",
	columns = {
		"Id varchar(255) NOT NULL",
		"IsAdmin bool DEFAULT 0",
		"Guilds longtext DEFAULT NULL",
		"PRIMARY KEY (Id)"
	}
)
public class GraphiteMySQLAccountManager implements GraphiteAccountManager {
	
	private GraphiteWebinterface webinterface;
	private List<GraphiteWebinterfaceUser> cachedUsers;
	
	public GraphiteMySQLAccountManager(GraphiteWebinterface webinterface) {
		this.webinterface = webinterface;
		this.cachedUsers = new ArrayList<>();
	}
	
	@Override
	public GraphiteWebinterface getWebinterface() {
		return webinterface;
	}
	
	@Override
	public GraphiteWebinterfaceUser createUser(String id) {
		GraphiteWebinterfaceUser u = new GraphiteWebinterfaceUser(webinterface, id, false);
		Graphite.getMySQL().query("INSERT INTO webinterface_users(Id) VALUES(?)", id);
		return u;
	}
	
	@Override
	public void updateUser(GraphiteWebinterfaceUser user) {
		JSONArray guilds = new JSONArray();
		for(GraphiteWebinterfaceGuild g : user.getGuilds()) {
			guilds.add(g.getRawGuild());
		}
		Graphite.getMySQL().query("UPDATE webinterface_users SET IsAdmin=?, Guilds=? WHERE Id=?", user.isAdmin(), guilds.toString(), user.getDiscordUser().getID());
	}

	@Override
	public GraphiteWebinterfaceUser loadUser(String id) {
		GraphiteWebinterfaceUser u = loadUserIfExists(id);
		if(u == null) return createUser(id);
		return u;
	}
	
	private GraphiteWebinterfaceUser loadUserIfExists(String id) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM webinterface_users WHERE Id = ?")) {
				stmt.setString(1, id);
				try(ResultSet result = stmt.executeQuery()) {
					if(!result.next()) return null;
					GraphiteWebinterfaceUser u = new GraphiteWebinterfaceUser(webinterface, id, result.getBoolean("IsAdmin"));
					String g = result.getString("Guilds");
					List<GraphiteWebinterfaceGuild> gs = new ArrayList<>();
					if(g != null) {
						JSONArray a = new JSONArray(g);
						for(Object o : a) {
							gs.add(new GraphiteWebinterfaceGuild(u, (JSONObject) o));
						}
					}
					u.setGuildsRaw(gs);
					return u;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve webinterface user from MySQL", e));
	}

	@Override
	public List<GraphiteWebinterfaceUser> getCachedUsers() {
		return cachedUsers;
	}

	@Override
	public void close() {
	}
	
}
