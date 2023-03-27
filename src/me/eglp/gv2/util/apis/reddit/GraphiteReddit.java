package me.eglp.gv2.util.apis.reddit;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.settings.RedditSettings;
import me.eglp.reddit.RedditAPI;
import me.eglp.reddit.UserAgent;

public class GraphiteReddit {
	
	private RedditAPI reddit;
	
	public GraphiteReddit() {
		RedditSettings r = Graphite.getMainBotInfo().getReddit();
		UserAgent a = new UserAgent("discord", "com.graphite-official.graphite", "1.0", "MrLetsplay2003");
		reddit = new RedditAPI(r.getClientID(), r.getClientSecret(), a);
	}
	
	public RedditAPI getRedditAPI() {
		return reddit;
	}
	
}
