package me.eglp.gv2.util.apis.twitter;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteSetupException;
import me.eglp.gv2.util.settings.TwitterSettings;
import me.eglp.twitter.TwitterAPI;

public class GraphiteTwitter {
	
	private TwitterAPI twitter;
	
	public GraphiteTwitter() {
		TwitterSettings t = Graphite.getMainBotInfo().getTwitter();
		
		try {
			this.twitter = new TwitterAPI(t.getToken());
		}catch(Exception e) {
			throw new GraphiteSetupException("Failed to create Twitter API, check credentials", e);
		}
	}
	
	public TwitterAPI getTwitterAPI() {
		return twitter;
	}

}
