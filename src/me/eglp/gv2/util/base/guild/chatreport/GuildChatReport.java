package me.eglp.gv2.util.base.guild.chatreport;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.crypto.GraphiteCrypto;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.misc.FriendlyException;

@JavaScriptClass(name = "ChatReport")
public class GuildChatReport implements WebinterfaceObject {

	private GraphiteGuild guild;
	
	@JavaScriptValue(getter = "getID")
	private String id;
	
	@JavaScriptValue(getter = "getReporter")
	private String reporter;
	
	@JavaScriptValue(getter = "getChannelName")
	private String channelName;
	
	@JavaScriptValue(getter = "getTimestamp")
	private long timestamp;
	
	public GuildChatReport(GraphiteGuild guild, String id, String reporter, String channelName, long timestamp) {
		this.guild = guild;
		this.id = id;
		this.reporter = reporter;
		this.channelName = channelName;
		this.timestamp = timestamp;
	}
	
	public String getID() {
		return id;
	}
	
	public String getReporter() {
		return reporter;
	}
	
	public String getChannelName() {
		return channelName;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public boolean isCorrectKey(PrivateKey decryptionKey) {
		byte[] b = Graphite.getMySQL().query(byte[].class, null, "SELECT AESKey FROM guilds_chatreports WHERE GuildId = ? AND Id = ?", guild.getID(), id)
				.orElseThrowOther(e -> new FriendlyException("Failed to verify key", e));
		if(b == null) return false;
		try {
			GraphiteCrypto.decryptAESKey(b, decryptionKey);
			return true;
		}catch(Exception e) {
			return false;
		}
	}
	
	public List<GuildChatReportMessage> loadChatHistory(PrivateKey decryptionKey) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT AESKey, Messages FROM guilds_chatreports WHERE GuildId = ? AND Id = ?")) {
				s.setString(1, guild.getID());
				s.setString(2, id);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return null;
					SecretKey aesKey;
					try {
						Blob blob = r.getBlob("AESKey");
						if(blob == null) return null;
						byte[] encryptedMessageKey = blob.getBytes(1, (int) blob.length());
						aesKey = GraphiteCrypto.decryptAESKey(encryptedMessageKey, decryptionKey);
					}catch(Exception e) {
						Graphite.log("Decryption error: key");
						GraphiteDebug.log(DebugCategory.BACKUP, e);
						throw new FriendlyException("Failed to decrypt key");
					}
					
					try {
						Cipher cipher = Cipher.getInstance("AES");
						cipher.init(Cipher.DECRYPT_MODE, aesKey);
						
						Blob msgs = r.getBlob("Messages");
						if(msgs == null) return null;
						byte[] enc = msgs.getBytes(1, (int) msgs.length());
						return new JSONArray(new String(cipher.doFinal(enc), StandardCharsets.UTF_8)).stream()
								.map(o -> JSONConverter.decodeObject((JSONObject) o, GuildChatReportMessage.class))
								.collect(Collectors.toList());
					}catch(NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
						Graphite.log("Decryption error: messages");
						GraphiteDebug.log(DebugCategory.BACKUP, e);
						throw new FriendlyException("Failed to decrypt messages");
					}
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load chatreport history from MySQL", e));
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof GuildChatReport)) return false;
		GuildChatReport r = (GuildChatReport) obj;
		return id.equals(r.id);
	}
	
	@JavaScriptFunction(calling = "chatReportsEnabled", returning = "is-enabled", withGuild = true)
	public static void chatReportsEnabled() {};
	
	@JavaScriptFunction(calling = "enableChatReports", returning = "decryption-key", withGuild = true)
	public static void enableChatReports() {};
	
	@JavaScriptFunction(calling = "disableChatReports", withGuild = true)
	public static void disableChatReports() {};
	
	@JavaScriptFunction(calling = "getChatReports", returning = "chatreports", withGuild = true)
	public static void getChatReports() {};
	
	@JavaScriptFunction(calling = "getChatReportHistory", returning = "chat-history", withGuild = true)
	public static void getChatReportHistory(@JavaScriptParameter(name = "id") String id, @JavaScriptParameter(name = "key") String key) {};

	@JavaScriptFunction(calling = "deleteChatReport", withGuild = true)
	public static void deleteChatReport(@JavaScriptParameter(name = "id") String id) {};
	
}
