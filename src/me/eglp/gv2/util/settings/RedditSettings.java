package me.eglp.gv2.util.settings;

import java.util.List;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class RedditSettings implements JSONConvertible {
	
	@JSONValue
	private boolean enable;
	
	@JSONValue
	private RedditUserAgent userAgent;
	
	@JSONValue
	private String
		clientID,
		clientSecret;
	
	@JSONConstructor
	private RedditSettings() {}
	
	public boolean isEnabled() {
		return enable;
	}
	
	public RedditUserAgent getUserAgent() {
		return userAgent;
	}

	public String getClientID() {
		return clientID;
	}
	
	public String getClientSecret() {
		return clientSecret;
	}
	
	public void validate(List<String> errors) {
		if(!enable) return;
		if(userAgent == null) errors.add("Reddit user agent missing");
		if(userAgent != null) userAgent.validate(errors);
		if(clientID == null) errors.add("Reddit client id missing");
		if(clientSecret == null) errors.add("Reddit client secret missing");
	}
	
	public static RedditSettings createDefault() {
		RedditSettings s = new RedditSettings();
		s.enable = false;
		s.userAgent = RedditUserAgent.createDefault();
		s.clientID = "Reddit client ID";
		s.clientSecret = "Reddit client secret";
		return s;
	}
	
	public static class RedditUserAgent implements JSONConvertible {
		
		@JSONValue
		private String
			platform,
			appID,
			version,
			author;
		
		@JSONConstructor
		private RedditUserAgent() {}
		
		public String getPlatform() {
			return platform;
		}

		public String getAppID() {
			return appID;
		}

		public String getVersion() {
			return version;
		}

		public String getAuthor() {
			return author;
		}
		
		public void validate(List<String> errors) {
			if(platform == null) errors.add("Reddit user agent platform missing");
			if(appID == null) errors.add("Reddit user agent app id missing");
			if(version == null) errors.add("Reddit user agent version missing");
			if(author == null) errors.add("Reddit user agent author missing");
		}

		public static RedditUserAgent createDefault() {
			RedditUserAgent u = new RedditUserAgent();
			u.platform = "discord";
			u.appID = "com.example.mybot";
			u.version = "1.0";
			u.author = "Your Reddit username";
			return u;
		}
		
	}

}
