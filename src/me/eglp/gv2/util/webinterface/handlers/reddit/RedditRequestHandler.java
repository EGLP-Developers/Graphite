package me.eglp.gv2.util.webinterface.handlers.reddit;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.util.apis.reddit.GraphiteSubreddit;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.eglp.gv2.util.webinterface.js.ObjectSerializer;
import me.eglp.reddit.entity.data.Subreddit;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class RedditRequestHandler {

	@WebinterfaceHandler(requestMethod = "getSubreddits", requireGuild = true, requireFeatures = GraphiteFeature.REDDIT)
	public static WebinterfaceResponse getSubreddits(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		List<GraphiteSubreddit> subreddits = g.getRedditConfig().getSubreddits();
		JSONObject o = new JSONObject();
		o.put("subreddits", new JSONArray(subreddits.stream().map(s -> s.toWebinterfaceObject()).collect(Collectors.toList())));
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "addSubreddit", requireGuild = true, requireFeatures = GraphiteFeature.REDDIT)
	public static WebinterfaceResponse addSubreddit(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String subreddit = event.getRequestData().getString("subreddit");
		String channel = event.getRequestData().getString("channel_id");

		GraphiteGuildMessageChannel tc = g.getGuildMessageChannelByID(channel);
		if(tc == null) {
			return WebinterfaceResponse.error("Channel doesn't exist");
		}

		Subreddit sr = Graphite.getReddit().getRedditAPI().getAbout(subreddit);
		if(sr == null) {
			return WebinterfaceResponse.error("Subreddit doesn't exist on Reddit");
		}

		if(g.getRedditConfig().getSubredditByName(subreddit) != null) {
			return WebinterfaceResponse.error("Subreddit already added");
		}

		GraphiteSubreddit gSr = g.getRedditConfig().createSubreddit(subreddit, sr, tc);

		JSONObject o = new JSONObject();
		o.put("subreddit", gSr.toWebinterfaceObject());

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "removeSubreddit", requireGuild = true, requireFeatures = GraphiteFeature.REDDIT)
	public static WebinterfaceResponse removeSubreddit(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String subreddit = event.getRequestData().getString("subreddit");
		GraphiteSubreddit gSr = g.getRedditConfig().getSubredditByName(subreddit);
		if(gSr == null) {
			return WebinterfaceResponse.error("Can't find subreddit");
		}
		g.getRedditConfig().removeSubreddit(gSr);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "updateSubreddit", requireGuild = true, requireFeatures = GraphiteFeature.REDDIT)
	public static WebinterfaceResponse updateSubreddit(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		JSONObject o = event.getRequestData().getJSONObject("object");
		GraphiteSubreddit sets = (GraphiteSubreddit) ObjectSerializer.deserialize(o);
		if(sets == null) {
			return WebinterfaceResponse.error("Subreddit doesn't exist in your notification list");
		}
		g.getRedditConfig().updateSubreddit(sets);
		return WebinterfaceResponse.success();
	}

}
