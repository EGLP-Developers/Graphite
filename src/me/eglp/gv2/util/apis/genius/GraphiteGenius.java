package me.eglp.gv2.util.apis.genius;

import me.eglp.genius.GeniusAPI;
import me.eglp.gv2.main.Graphite;

public class GraphiteGenius {
	
	private GeniusAPI genius;
	
	public GraphiteGenius() {
		this.genius = new GeniusAPI(Graphite.getBotInfo().getGenius().getAccessToken());
	}
	
	public GeniusAPI getGeniusAPI() {
		return genius;
	}

}
