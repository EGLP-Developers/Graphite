package me.eglp.gv2.util.apis.twitch;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteSetupException;
import me.eglp.gv2.util.settings.TwitchSettings;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.eglp.twitch.TwitchAPI;

public class GraphiteTwitch implements WebinterfaceObject{
	
	private TwitchAPI twitch;
	
	public GraphiteTwitch() {
		TwitchSettings t = Graphite.getBotInfo().getTwitch();
		
		try {
			twitch = new TwitchAPI(t.getClientID(), t.getClientSecret());
		}catch(Exception e) {
			throw new GraphiteSetupException("Failed to create Twitch API, check credentials", e);
		}
	}
	
	public TwitchAPI getTwitchAPI() {
		return twitch;
	}
	
}
