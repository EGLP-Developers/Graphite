package me.eglp.gv2.util.apis.twitch;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.settings.TwitchSettings;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.eglp.twitch.TwitchAPI;

public class GraphiteTwitch implements WebinterfaceObject{
	
	private TwitchAPI twitch;
	
	public GraphiteTwitch() {
		TwitchSettings t = Graphite.getMainBotInfo().getTwitch();
		twitch = new TwitchAPI(t.getClientID(), t.getClientSecret());
	}
	
	public TwitchAPI getTwitchAPI() {
		return twitch;
	}
	
}
