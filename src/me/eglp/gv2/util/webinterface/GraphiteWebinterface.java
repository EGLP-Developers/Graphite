package me.eglp.gv2.util.webinterface;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.java_websocket.WebSocket;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.webinterface.base.GraphiteWebinterfaceUser;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequest;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.eglp.gv2.util.webinterface.session.GraphiteMySQLSessionStorage;
import me.eglp.gv2.util.webinterface.session.WebinterfaceSession;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.JSONType;
import net.dv8tion.jda.api.Permission;

public class GraphiteWebinterface {

	private GraphiteAccountManager accountManager;
	private GraphiteMySQLSessionStorage sessionStorage;
	private WebinterfaceWebSocketServer webSocketServer;

	private static final Reflections REFLECTIONS = new Reflections("me.eglp.gv2", Scanners.MethodsAnnotated);
	private static final Set<Method> HANDLER_METHODS = getHandlerMethods0();

	private static Set<Method> getHandlerMethods0() {
		return REFLECTIONS.getMethodsAnnotatedWith(WebinterfaceHandler.class).stream()
				.collect(Collectors.toSet());
	}

	public GraphiteWebinterface() {
		accountManager = new GraphiteMySQLAccountManager(this);
		sessionStorage = new GraphiteMySQLSessionStorage();

		webSocketServer = new WebinterfaceWebSocketServer(Graphite.getMainBotInfo().getWebsite().getWebsocketPort());
		webSocketServer.start();
	}

	public WebinterfaceWebSocketServer getWebSocketServer() {
		return webSocketServer;
	}

	public void kick(String userID) {
		webSocketServer.getConnections().forEach(c -> {
			WebinterfaceSession sess = c.getAttachment();
			if(sess == null || !userID.equals(sess.getUserID())) return;
			c.send(WebinterfacePacket.of("disconnect", null).toJSON().toString());
			c.close();
		});
	}

	public void kickAll() {
		webSocketServer.getConnections().forEach(c -> {
			c.send(WebinterfacePacket.of("disconnect", null).toJSON().toString());
			c.close();
		});
	}

	public void broadcastToUser(String userID, String msg) {
		webSocketServer.getConnections().forEach(c -> {
			WebinterfaceSession sess = c.getAttachment();
			if(!userID.equals(sess.getUserID())) return;
			JSONObject o = new JSONObject();
			o.put("message", msg);
			c.send(WebinterfacePacket.of("broadcast", o).toJSON().toString());
		});
	}

	public void broadcastToAll(String msg) {
		webSocketServer.getConnections().forEach(c -> {
			JSONObject o = new JSONObject();
			o.put("message", msg);
			c.send(WebinterfacePacket.of("broadcast", o).toJSON().toString());
		});
	}

	public void stop() {
		accountManager.close();
		try {
			webSocketServer.getConnections().forEach(c -> c.send(WebinterfacePacket.of("disconnect", null).toJSON().toString()));
			webSocketServer.stop(10000);
		} catch (InterruptedException e) {
			GraphiteDebug.log(DebugCategory.WEBINTERFACE, e);
		}
	}

	public void sendRequestToGuildUsers(String requestMethod, JSONObject requestData, String guildID, GraphiteFeature... requiredFeatures) {
		for(WebSocket socket : webSocketServer.getConnections()) {
			WebinterfaceSession sess = socket.getAttachment();
			if(sess == null) continue;
			if(sess.getData().isOfType("lastKnownGuildID", JSONType.STRING)
					&& sess.getData().getString("lastKnownGuildID").equals(guildID)) {
				GraphiteGuild guild = Graphite.getGuild(guildID);
				if(guild == null) continue;
				if(!Arrays.stream(requiredFeatures).allMatch(f -> guild.getPermissionManager().hasPermission(sess.getUser().getDiscordUser(), f.getWebinterfacePermission()))) continue;
				socket.send(WebinterfacePacket.of(requestMethod, requestData).toJSON().toString());
			}
		}
	}

	public WebinterfacePacket handlePacket(WebSocket webSocket, WebinterfacePacket packet) throws IOException {
		try {
			WebinterfaceSession session = webSocket.getAttachment();
			GraphiteWebinterfaceUser user = session.getUser();

			MultiplexBot bot = null;
			if(packet.getBotIdentifier() != null) {
				bot = Graphite.getMultiplexBot(packet.getBotIdentifier());
			}

			GraphiteGuild guild = null;
			if(packet.getGuildID() != null) {
				guild = Graphite.getGlobalGuild(packet.getGuildID());
			}

			if(guild != null) {
				if(!user.isOnGuild(guild.getID())) {
					return WebinterfacePacket.error(packet.getID(), "No permission for guild");
				}
			}

			WebinterfaceRequest req = new WebinterfaceRequest(user, bot, guild, packet);
			WebinterfaceRequestEvent e = new WebinterfaceRequestEvent(req);
			for(Method m : HANDLER_METHODS) {
				if(m.isAnnotationPresent(WebinterfaceHandler.class)) {
					WebinterfaceHandler wh = m.getAnnotation(WebinterfaceHandler.class);
					if(wh.requestMethod().equals(req.getRequestMethod()) &&
							(!wh.requireGuild() || (guild != null)) &&
							(!wh.requireBot() || (bot != null && bot.getBotInfo().hasFeaturesAvailable(wh.requireFeatures()))) &&
							(guild == null || guild.hasFeaturesAvailable(wh.requireFeatures()))) {
						if(!Modifier.isStatic(m.getModifiers()) ||
								!Arrays.equals(m.getParameterTypes(), new Class[] {WebinterfaceRequestEvent.class}) ||
								!m.getReturnType().equals(WebinterfaceResponse.class)) {
							webSocket.close();
							throw new UnsupportedOperationException("Illegal request handler @ " + m);
						}

						if(wh.requireBot()) {
							GraphiteMultiplex.setCurrentBot(bot);
						}

						if(wh.requireGuildAdmin()) {
							if(guild == null) {
								return WebinterfacePacket.error(packet.getID(), "No permission");
							}

							GraphiteMember mem = guild.getMember(user.getDiscordUser());
							if(mem == null || !mem.getJDAMember().hasPermission(Permission.ADMINISTRATOR)) {
								return WebinterfacePacket.error(packet.getID(), "No permission");
							}
						}

						if(guild != null) {
							final GraphiteGuild fGuild = guild;
							GraphiteMember mem = guild.getMember(user.getDiscordUser());
							if(mem == null || !Arrays.stream(wh.requireFeatures()).allMatch(f -> f.getWebinterfacePermission() == null || fGuild.getPermissionManager().hasPermission(mem, f.getWebinterfacePermission()) || mem.getJDAMember().hasPermission(Permission.ADMINISTRATOR))) {
								return WebinterfacePacket.error(packet.getID(), "No permission");
							}
						}

						try {
							WebinterfaceResponse response = (WebinterfaceResponse) m.invoke(null, e);
							return response.toPacket(packet.getID());
						}catch(InvocationTargetException e2) {
							GraphiteDebug.log(DebugCategory.WEBINTERFACE, e2);
							return WebinterfacePacket.error(packet.getID(), "Malformed request (1)");
						}
					}
				}
			}

			return WebinterfacePacket.error(packet.getID(), "Invalid request method");
		}catch(Exception e) {
			GraphiteDebug.log(DebugCategory.WEBINTERFACE, e);
			return WebinterfacePacket.error(packet.getID(), "Malformed request (2)");
		}
	}

	public static Set<Method> getHandlerMethods() {
		return HANDLER_METHODS;
	}

	public GraphiteAccountManager getAccountManager() {
		return accountManager;
	}

	public GraphiteMySQLSessionStorage getSessionStorage() {
		return sessionStorage;
	}

}
