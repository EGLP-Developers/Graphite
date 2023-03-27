package me.eglp.gv2.util.webinterface;

import java.util.List;

import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.webinterface.base.GraphiteWebinterfaceUser;

public interface GraphiteAccountManager {
	
	public GraphiteWebinterface getWebinterface();
	
	public GraphiteWebinterfaceUser createUser(String id);
	
	public void updateUser(GraphiteWebinterfaceUser user);
	
	public GraphiteWebinterfaceUser loadUser(String id);
	
	public List<GraphiteWebinterfaceUser> getCachedUsers();
	
	public default GraphiteWebinterfaceUser getUser(String id) {
		return getCachedUsers().stream()
				.filter(c -> c.isKnown() && c.getDiscordUser().getID().equals(id))
				.findFirst().orElseGet(() -> retrieveUser(id));
	}
	
	public default GraphiteWebinterfaceUser getUser(GraphiteUser user) {
		return getCachedUsers().stream()
				.filter(c -> c.isKnown() && c.getDiscordUser().equals(user))
				.findFirst().orElseGet(() -> retrieveUser(user.getID()));
	}
	
	public default GraphiteWebinterfaceUser retrieveUser(String id) {
		GraphiteWebinterfaceUser user = loadUser(id);
		getCachedUsers().add(user);
		return user;
	}
	
	public void close();
	
}
