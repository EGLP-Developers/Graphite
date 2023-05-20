package me.eglp.gv2.util.website;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.main.GraphiteShard;
import me.eglp.gv2.main.task.GraphiteAlwaysRepeatingTask;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.text.CommandHandler;
import me.eglp.gv2.util.settings.BotInfo;
import me.eglp.gv2.util.voting.GraphiteVoteSource;
import me.eglp.gv2.util.voting.InvalidVoteException;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class GraphiteWebsiteEndpoint {

	private ServerSocket serverSocket;
	private GraphiteAlwaysRepeatingTask receiveTask;

	private WebsiteImageCache imageCache;

	public GraphiteWebsiteEndpoint() {
		this.imageCache = new WebsiteImageCache();
		try {
			start();
		} catch (IOException e) {
			GraphiteDebug.log(DebugCategory.WEBSITE_ENDPOINT, e);
			throw new FriendlyException("Failed to open website endpoint", e);
		}
	}

	private void start() throws IOException {
		serverSocket = new ServerSocket(Graphite.getBotInfo().getWebsite().getWebsiteEndpointPort());
		serverSocket.setSoTimeout(1000);

		receiveTask = Graphite.getScheduler().scheduleAlwaysRepeating("website-endpoint/receive", () -> {
			try {
				if(serverSocket.isClosed()) return;
				Socket s = serverSocket.accept();
				s.setSoTimeout(5000);
				Graphite.getScheduler().execute(() -> {
					try (s){
						receive(s);
					} catch (Exception e) {
						GraphiteDebug.log(DebugCategory.WEBSITE_ENDPOINT, e);
					}
				});
			}catch(SocketTimeoutException e) {
			}catch(SocketException e) {
			}catch(IOException e) {
				GraphiteDebug.log(DebugCategory.WEBSITE_ENDPOINT, e);
			}
		});
	}

	public WebsiteImageCache getImageCache() {
		return imageCache;
	}

	public void stop() {
		receiveTask.stop(false);
		try {
			serverSocket.close();
		} catch (IOException e) {
			GraphiteDebug.log(DebugCategory.WEBSITE_ENDPOINT, e);
		}
	}

	private void receive(Socket s) throws IOException {
		DataInputStream in = new DataInputStream(s.getInputStream());
		DataOutputStream out = new DataOutputStream(s.getOutputStream());

		String k = WebsiteUtils.readString(in);
		if(!k.equals(Graphite.getBotInfo().getWebsite().getWebsiteEndpointKey())) {
			s.close();
			return;
		}

		String request = WebsiteUtils.readString(in);
		JSONObject req = new JSONObject(request);
		String rType = req.getString("type");
		JSONObject rData = req.getJSONObject("data");

		JSONObject response = new JSONObject();
		switch(rType) {
			case "status":
			{
				status(rData, response);
				break;
			}
			case "vote":
			{
				vote(rData, response);
				break;
			}
			case "commands":
			{
				commands(rData, response);
				break;
			}
			case "image":
			{
				image(rData, response);
				break;
			}
		}

		WebsiteUtils.writeString(out, response.toString());
	}

	private void status(JSONObject requestData, JSONObject response) {
		JSONObject botO = new JSONObject();
		JSONArray shs = new JSONArray();

		BotInfo info = Graphite.getBotInfo();

		for(GraphiteShard shard : Graphite.getShards()) {
			JSONObject sh = new JSONObject();
			sh.put("id", shard.getID());
			sh.put("status", shard.getStatus());
			sh.put("ping", shard.getPing());
			shs.add(sh);
		}

		botO.put("name", info.getName());
		botO.put("shards", shs);

		response.put(info.getIdentifier(), botO);
	}

	private void vote(JSONObject requestData, JSONObject response) {
		if(!requestData.has("bot") || !requestData.has("vote_source")) {
			return;
		}

		String vsId = requestData.getString("vote_source");
		GraphiteVoteSource vs = Graphite.getVoting().getVoteSource(vsId);
		if(vs == null) return;

		try {
			vs.onVote(requestData);
		}catch(InvalidVoteException e) {
		}catch(Exception e) {
			GraphiteDebug.log(DebugCategory.WEBINTERFACE, e);
		}
	}

	private void commands(JSONObject requestData, JSONObject response) {
		Map<CommandCategory, List<Command>> commands = new HashMap<>();
		for(Command c : CommandHandler.getCommands()) CommandHandler.addCommands(c, commands);
		List<Map.Entry<CommandCategory, List<Command>>> cs = new ArrayList<>(commands.entrySet());
		Collections.sort(cs, (o1, o2) -> o2.getValue().size()-o1.getValue().size());
		cs.forEach(en -> {
			JSONArray arr2 = new JSONArray();
			for(Command c : en.getValue()) {
				JSONObject cm = new JSONObject();
				cm.put("command", c.getFullName());
				if(c.getDescription() != null) cm.put("description", c.getDescription().getFallback());
				if(c.getUsage() != null) cm.put("usage", c.getUsage().getFallback().replace("{prefix}", ""));
				if(c.getPermission() != null) cm.put("permission", c.getPermission());
				if(!c.getAliases().isEmpty()) cm.put("aliases", c.getAliases().stream().collect(Collectors.joining(", ")));
				cm.put("server_only", !c.allowsPrivate() && c.allowsServer());
				cm.put("private_only", c.allowsPrivate() && !c.allowsServer());
				arr2.add(cm);
			}
			response.put(en.getKey().getName().getFallback(), arr2);
		});

		JSONObject obj = new JSONObject();
		obj.put("data", response);
	}

	private void image(JSONObject requestData, JSONObject response) {
		byte[] bs = imageCache.getImage(requestData.getString("id"));
		if(bs == null) return;
		response.put("image", Base64.getEncoder().encodeToString(bs));
	}

}
