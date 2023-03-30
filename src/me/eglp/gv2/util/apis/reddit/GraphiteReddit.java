package me.eglp.gv2.util.apis.reddit;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteSetupException;
import me.eglp.gv2.util.settings.RedditSettings;
import me.eglp.gv2.util.settings.RedditSettings.RedditUserAgent;
import me.eglp.reddit.RedditAPI;
import me.eglp.reddit.UserAgent;

public class GraphiteReddit {
	
	private RedditAPI reddit;
	
	public GraphiteReddit() {
		RedditSettings r = Graphite.getMainBotInfo().getReddit();
		RedditUserAgent u = r.getUserAgent();
		
		try {
			UserAgent a = new UserAgent(u.getPlatform(), u.getAppID(), u.getVersion(), u.getAuthor());
			reddit = new RedditAPI(r.getClientID(), r.getClientSecret(), a);
		}catch(Exception e) {
			throw new GraphiteSetupException("Failed to create Reddit API, check credentials", e);
		}
	}
	
	public RedditAPI getRedditAPI() {
		return reddit;
	}
	
}
