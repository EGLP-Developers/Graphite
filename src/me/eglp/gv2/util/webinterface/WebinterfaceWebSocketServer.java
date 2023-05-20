package me.eglp.gv2.util.webinterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.main.GraphiteOption;
import me.eglp.gv2.util.Statics;
import me.eglp.gv2.util.webinterface.base.GraphiteWebinterfaceGuild;
import me.eglp.gv2.util.webinterface.base.GraphiteWebinterfaceUser;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.eglp.gv2.util.webinterface.js.ObjectSerializer;
import me.eglp.gv2.util.webinterface.session.WebinterfaceSession;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class WebinterfaceWebSocketServer extends WebSocketServer {

	public WebinterfaceWebSocketServer(int port) {
		super(new InetSocketAddress("127.0.0.1", port));
		setReuseAddr(true);
		setTcpNoDelay(true);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		JSONArray descriptors = new JSONArray(ObjectSerializer.generateClassDescriptors());
		JSONObject o = new JSONObject();
		o.put("descriptors", descriptors);
		if(conn.isOpen()) conn.send(WebinterfacePacket.of("init", o).toJSON().toString());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {

	}

	@SuppressWarnings("null")
	@Override
	public void onMessage(WebSocket conn, String message) {
		if(Graphite.hasOption(GraphiteOption.WEBINTERFACE_DEBUG)) Graphite.log(">> " + message);
		try {
			WebinterfacePacket p = WebinterfacePacket.deserialize(new JSONObject(message));

			if(p.getRequestMethod().equals("login")) {
				String code = p.getData().getString("code");
				WebinterfaceResponse r = login(conn, code);
				conn.send(r.toPacket(p.getID()).toJSON().toString());
				return;
			}

			if(p.getRequestMethod().equals("setSession")) {
				String sessionID = p.getData().getString("sessionID");

				WebinterfaceSession s = Graphite.getWebinterface().getSessionStorage().getSession(sessionID);
				boolean sessionValid = s != null && s.isValid();
				JSONObject o = new JSONObject();
				o.put("sessionValid", sessionValid);
				conn.send(WebinterfacePacket.ofResponse(p.getID(), o).toJSON().toString());
				if(!sessionValid) {
					if(Graphite.hasOption(GraphiteOption.WEBINTERFACE_DEBUG)) Graphite.log("Session invalid: " + sessionID);
					conn.close();
				}else {
					if(Graphite.hasOption(GraphiteOption.WEBINTERFACE_DEBUG)) Graphite.log("Session valid: " + sessionID + " (User: " + s.getUserID() + ")");
					conn.setAttachment(s);
				}

				return;
			}

			WebinterfaceSession session = conn.getAttachment();
			if(session == null) {
				conn.close(CloseFrame.POLICY_VALIDATION, "Not logged in");
				return;
			}

			if(p.getRequestMethod().equals("updateSelectedGuild")) {
				if(p.getGuildID() != null && !session.getUser().isOnGuild(p.getGuildID())) {
					conn.send(WebinterfaceResponse.error("No permission").toPacket(p.getID()).toJSON().toString());
					return;
				}

				session.getData().put("lastKnownGuildID", p.getGuildID());
				session.commitData();
				conn.send(WebinterfacePacket.ofResponse(p.getID(), null).toJSON().toString());
				return;
			}

			WebinterfacePacket r = Graphite.getWebinterface().handlePacket(conn, p);
			if(Graphite.hasOption(GraphiteOption.WEBINTERFACE_DEBUG)) Graphite.log("<< " + r.toJSON().toString());
			conn.send(r.toJSON().toString());
		}catch(Exception e) {
			GraphiteDebug.log(DebugCategory.WEBINTERFACE, e);
			conn.close(CloseFrame.POLICY_VALIDATION, "Unknown error");
		}
	}

	private WebinterfaceResponse login(WebSocket conn, String code) {
		try {
			String in = post("https://discord.com/api/oauth2/token", null,
					"client_id", Graphite.getBotID(),
					"client_secret", Graphite.getBotInfo().getClientSecret(),
					"grant_type", "authorization_code",
					"code", code,
					"redirect_uri", Graphite.getBotInfo().getWebsite().getBaseURL() + "/login",
					"scope", "identify guilds");
			JSONObject tokenThing = new JSONObject(in);
			if(tokenThing.has("error")) {
				return WebinterfaceResponse.error("Invalid code");
			}
			JSONObject userInfo = new JSONObject(get(Statics.DISCORD_USERS_ME, tokenThing.getString("access_token")));
			GraphiteWebinterfaceUser user = Graphite.getWebinterfaceUser(userInfo.getString("id"));
			if(user.getDiscordUser() == null) {
				return WebinterfaceResponse.error("Failed to find user");
			}
			JSONArray guildInfo = new JSONArray(get(Statics.DISCORD_USERS_ME_GUILDS, tokenThing.getString("access_token")));
			List<GraphiteWebinterfaceGuild> guilds = new ArrayList<>();
			guildInfo.forEach(obj -> {
				JSONObject g = (JSONObject) obj;
				GraphiteGuild grG = Graphite.getGuild(g.getString("id"));
				boolean admin = g.getBoolean("owner") || ((g.getLong("permissions") >> 3) & 1) == 1;
				if(admin) {
					guilds.add(new GraphiteWebinterfaceGuild(user, g));
				}else if(grG != null) {
					GraphiteMember mem = grG.getMember(user.getDiscordUser());
					if(mem == null) return;
					guilds.add(new GraphiteWebinterfaceGuild(user, g));
				}
			});
			user.setGuilds(guilds);
			WebinterfaceSession sess = Graphite.getWebinterface().getSessionStorage().createNew(user);
			JSONObject resp = new JSONObject();
			resp.put("user_id", user.getDiscordUser().getJDAUser().getId());
			resp.put("guilds", new JSONArray(guilds.stream().map(g -> g.getRawGuild()).collect(Collectors.toList())));
			resp.put("sessionID", sess.getID());
			conn.setAttachment(sess);
			return WebinterfaceResponse.success(resp);
		} catch (Exception e) {
			GraphiteDebug.log(DebugCategory.WEBINTERFACE, e);
			return WebinterfaceResponse.error("Failed to verify login");
		}
	}

	public static String post(String url, String auth, String... postParams) throws IOException {
		HttpClient cl = HttpClients.createDefault();
		HttpPost post = new HttpPost(url);
		List<NameValuePair> ps = new ArrayList<>();
		for(int i = 0; i < postParams.length; i+=2) {
			ps.add(new BasicNameValuePair(postParams[i], postParams[i + 1]));
		}
		post.setEntity(new UrlEncodedFormEntity(ps));
		if(auth != null) post.addHeader("Authorization", "Bearer " + auth);
		HttpResponse p = cl.execute(post);
		InputStream in = p.getEntity().getContent();
		return readString(in);
	}

	private static String get(String url, String auth) throws IOException {
		HttpClient cl = HttpClients.createDefault();
		HttpGet get = new HttpGet(url);
		if(auth != null) get.addHeader("Authorization", "Bearer " + auth);
		HttpResponse p = cl.execute(get);
		InputStream in = p.getEntity().getContent();
		return readString(in);
	}

	private static String readString(InputStream in) throws IOException {
		ByteArrayOutputStream bO = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int len;
		while((len = in.read(buf)) > 0) {
			bO.write(buf, 0, len);
		}
		return bO.toString("UTF-8");
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		if(conn != null) conn.close(CloseFrame.POLICY_VALIDATION, "Unknown error");
		ex.printStackTrace();
	}

	@Override
	public void onStart() {

	}

}
