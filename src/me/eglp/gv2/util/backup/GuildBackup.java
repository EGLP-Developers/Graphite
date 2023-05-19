package me.eglp.gv2.util.backup;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.backup.data.bans.BansData;
import me.eglp.gv2.util.backup.data.channels.ChannelsData;
import me.eglp.gv2.util.backup.data.config.GuildConfigData;
import me.eglp.gv2.util.backup.data.messages.MessagesData;
import me.eglp.gv2.util.backup.data.overview_settings.OverviewSettingsData;
import me.eglp.gv2.util.backup.data.permissions.PermissionsData;
import me.eglp.gv2.util.backup.data.roles.RolesData;
import me.eglp.gv2.util.crypto.GraphiteCrypto;
import me.eglp.gv2.util.mysql.SQLTable;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.SerializationOption;
import me.mrletsplay.mrcore.misc.FriendlyException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Icon.IconType;

@SQLTable(
	name = "guilds_backups",
	columns = {
		"GuildId varchar(255) DEFAULT NULL",
		"BackupName varchar(255) DEFAULT NULL",
		"Timestamp bigint DEFAULT NULL",
		"GuildIcon mediumblob DEFAULT NULL",
		"Channels longtext DEFAULT NULL",
		"Roles longtext DEFAULT NULL",
		"Bans longtext DEFAULT NULL",
		"AESKey tinyblob DEFAULT NULL",
		"Messages longblob DEFAULT NULL",
		"OverviewSettings longtext DEFAULT NULL",
		"Permissions longtext DEFAULT NULL",
		"Config longtext DEFAULT NULL",
		"AutoBackup bool DEFAULT 0",
		"PRIMARY KEY (GuildId, BackupName)"
	},
	guildReference = "GuildId",
	charset = "utf8mb4"
)
@JavaScriptClass(name = "Backup")
public class GuildBackup implements WebinterfaceObject {

	public static final long BACKUP_COOLDOWN = 60 * 1000;
	public static final String TASK_ID = "backup";

	private GraphiteGuild guild;

	@JavaScriptValue(getter = "getName")
	private String name;

	@JavaScriptValue(getter = "getTimestamp")
	private long timestamp;

	@JavaScriptValue(getter = "isAutoBackup")
	private boolean autoBackup;

	private GuildBackup(GraphiteGuild guild, String name, long timestamp, boolean autoBackup) {
		this.guild = guild;
		this.name = name;
		this.timestamp = timestamp;
		this.autoBackup = autoBackup;
	}

	public GraphiteGuild getGuild() {
		return guild;
	}

	public String getName() {
		return name;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean isAutoBackup() {
		return autoBackup;
	}

	public byte[] loadGuildIconRaw() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT GuildIcon FROM guilds_backups WHERE GuildId = ? AND BackupName = ?")) {
				st.setString(1, guild.getID());
				st.setString(2, name);

				try(ResultSet set = st.executeQuery()) {
					if(!set.next()) return null;
					Blob b = set.getBlob("GuildIcon");
					if(b == null) return null;
					return b.getBytes(1, (int) b.length());
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve guild icon from MySQL", e));
	}

	public BufferedImage loadGuildIcon() {
		try {
			return ImageIO.read(new ByteArrayInputStream(loadGuildIconRaw()));
		} catch (IOException e) {
			throw new FriendlyException("Failed to load guild icon");
		}
	}

	public ChannelsData loadChannelsData() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT Channels FROM guilds_backups WHERE GuildId = ? AND BackupName = ?")) {
				st.setString(1, guild.getID());
				st.setString(2, name);

				try(ResultSet set = st.executeQuery()) {
					if(!set.next()) return null;
					return ChannelsData.load(set.getString("Channels"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve channels data from MySQL", e));
	}

	public boolean hasMessagesData() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT Messages, AESKey FROM guilds_backups WHERE GuildId = ? AND BackupName = ?")) {
				st.setString(1, guild.getID());
				st.setString(2, name);

				try(ResultSet set = st.executeQuery()) {
					if(!set.next()) return null;

					return set.getBlob("AESKey") != null && set.getBlob("Messages") != null;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve messages data from MySQL", e));
	}

	public MessagesData loadMessagesData(PrivateKey decryptionKey) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT Messages, AESKey FROM guilds_backups WHERE GuildId = ? AND BackupName = ?")) {
				st.setString(1, guild.getID());
				st.setString(2, name);

				try(ResultSet set = st.executeQuery()) {
					if(!set.next()) return null;

					SecretKey aesKey;
					try {
						Blob blob = set.getBlob("AESKey");
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

						Blob msgs = set.getBlob("Messages");
						if(msgs == null) return null;
						byte[] enc = msgs.getBytes(1, (int) msgs.length());
						return MessagesData.load(new String(cipher.doFinal(enc), StandardCharsets.UTF_8));
					}catch(NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
						Graphite.log("Decryption error: messages");
						GraphiteDebug.log(DebugCategory.BACKUP, e);
						throw new FriendlyException("Failed to decrypt messages");
					}
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve messages data from MySQL", e));
	}

	public void removeMessagesData() {
		Graphite.getMySQL().query("UPDATE guilds_backups SET Messages = NULL WHERE GuildId = ? AND BackupName = ?", guild.getID(), name);
	}

	public RolesData loadRolesData() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT Roles FROM guilds_backups WHERE GuildId = ? AND BackupName = ?")) {
				st.setString(1, guild.getID());
				st.setString(2, name);

				try(ResultSet set = st.executeQuery()) {
					if(!set.next()) return null;
					return RolesData.load(set.getString("Roles"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve roles data from MySQL", e));
	}

	public PermissionsData loadPermissionsData() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT Permissions FROM guilds_backups WHERE GuildId = ? AND BackupName = ?")) {
				st.setString(1, guild.getID());
				st.setString(2, name);

				try(ResultSet set = st.executeQuery()) {
					if(!set.next()) return null;
					return PermissionsData.load(set.getString("Permissions"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve permissions data from MySQL", e));
	}

	public BansData loadBansData() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT Bans FROM guilds_backups WHERE GuildId = ? AND BackupName = ?")) {
				st.setString(1, guild.getID());
				st.setString(2, name);

				try(ResultSet set = st.executeQuery()) {
					if(!set.next()) return null;
					return BansData.load(set.getString("Bans"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve bans data from MySQL", e));
	}

	public OverviewSettingsData loadDiscordOverviewSettingsData() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT OverviewSettings FROM guilds_backups WHERE GuildId = ? AND BackupName = ?")) {
				st.setString(1, guild.getID());
				st.setString(2, name);

				try(ResultSet set = st.executeQuery()) {
					if(!set.next()) return null;
					return OverviewSettingsData.load(set.getString("OverviewSettings"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve overview settings data from MySQL", e));
	}

	public GuildConfigData loadConfigData() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT Config FROM guilds_backups WHERE GuildId = ? AND BackupName = ?")) {
				st.setString(1, guild.getID());
				st.setString(2, name);

				try(ResultSet set = st.executeQuery()) {
					if(!set.next()) return null;
					return GuildConfigData.load(set.getString("Config"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve graphite settings data from MySQL", e));
	}

	public long restore(GraphiteGuild guild, PrivateKey decryptionKey, EnumSet<RestoreSelector> selectors) {
		long start = System.currentTimeMillis();

		if(RestoreSelector.DISCORD_ICON.appliesTo(selectors) && guild.hasPermissions(Permission.MANAGE_SERVER)) {
			byte[] bytes = loadGuildIconRaw();
 			guild.getJDAGuild().getManager().setIcon(bytes == null ? null : Icon.from(bytes, IconType.PNG)).queue();
		}

		IDMappings mappings = new IDMappings();

		if(RestoreSelector.DISCORD_ROLES.appliesTo(selectors) && guild.hasPermissions(Permission.MANAGE_ROLES)) {
			RolesData roles = loadRolesData();
			if(roles != null) roles.restore(guild, RestoreSelector.DISCORD_ROLE_ASSIGNMENTS.appliesTo(selectors), mappings);
		}

		if(RestoreSelector.DISCORD_CHANNELS.appliesTo(selectors) && guild.hasPermissions(Permission.MANAGE_CHANNEL, Permission.MANAGE_SERVER)) {
			ChannelsData channels = loadChannelsData();
			if(channels != null) channels.restore(guild, selectors, mappings);
		}

		if(RestoreSelector.DISCORD_CHAT_HISTORY.appliesTo(selectors) && decryptionKey != null && guild.hasPermissions(Permission.MANAGE_WEBHOOKS)) {
			MessagesData d = loadMessagesData(decryptionKey);
			if(d != null) d.restore(guild, mappings);
		}

		if(RestoreSelector.PERMISSIONS.appliesTo(selectors) && guild.hasPermissions(Permission.MANAGE_PERMISSIONS)) {
			PermissionsData d = loadPermissionsData();
			if(d != null) d.restore(guild);
		}

		if(RestoreSelector.DISCORD_BANS.appliesTo(selectors) && guild.hasPermissions(Permission.BAN_MEMBERS)) {
			BansData d = loadBansData();
			if(d != null) d.restore(guild);
		}

		if(RestoreSelector.DISCORD_OVERVIEW_SETTINGS.appliesTo(selectors) && guild.hasPermissions(Permission.MANAGE_SERVER)) {
			OverviewSettingsData d = loadDiscordOverviewSettingsData();
			if(d != null) d.restore(guild, mappings);
		}

		GuildConfigData d = loadConfigData();
		if(d != null) d.restore(guild, selectors, mappings);

		return System.currentTimeMillis() - start;
	}

	public void delete() {
		Graphite.getMySQL().query("DELETE FROM guilds_backups WHERE GuildId = ? AND BackupName = ?", guild.getID(), name);
	}

	public static GuildBackup createNew(GraphiteGuild guild, PublicKey encryptionKey, int messageCount, boolean autoBackup) {
		return Graphite.getMySQL().run(con -> {
			String backupID = GraphiteUtil.randomShortID();

			try(PreparedStatement s = con.prepareStatement("INSERT INTO guilds_backups(GuildId, BackupName, Timestamp, GuildIcon, Channels, Roles, Bans, AESKey, Messages, OverviewSettings, Permissions, Config, AutoBackup) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				s.setString(1, guild.getID()); // GuildId
				s.setString(2, backupID); // BackupName
				long timestamp = System.currentTimeMillis();
				s.setLong(3, timestamp); // Timestamp

				Blob guildIcon = null;

				if(guild.getJDAGuild().getIconUrl() != null) {
					try {
						BufferedImage img = ImageIO.read(downloadIcon(guild.getJDAGuild().getIconUrl()));
						ByteArrayOutputStream bOut = new ByteArrayOutputStream();
						ImageIO.write(img, "PNG", bOut);
						guildIcon = new SerialBlob(bOut.toByteArray());
					}catch(IOException e) {
						throw new FriendlyException("Failed to convert guild icon");
					}
				}

				s.setBlob(4, guildIcon); // GuildIcon
				s.setString(5, new ChannelsData(guild).toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); // Channels
				s.setString(6, new RolesData(guild).toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); // Roles
				s.setString(7, new BansData(guild).toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); //Bans

				if(encryptionKey != null) {
					SecretKey key = GraphiteCrypto.generateSymmetricKey();
					Cipher keyEncryptionCipher = Cipher.getInstance("RSA");
					keyEncryptionCipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
					byte[] enc = keyEncryptionCipher.doFinal(key.getEncoded());

					s.setBlob(8, new SerialBlob(enc)); // AESKey
					s.setBlob(9, new SerialBlob(new MessagesData(guild, messageCount).getEncrypted(key))); // Messages
				}else {
					s.setBlob(8, (Blob) null); // AESKey
					s.setBlob(9, (Blob) null); // Messages
				}

				s.setString(10, new OverviewSettingsData(guild).toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); // GuildOverviewSettings
				s.setString(11, new PermissionsData(guild).toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); // Permissions
				s.setString(12, new GuildConfigData(guild).toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); // Config
				s.setBoolean(13, autoBackup); // AutoBackup
				s.execute();

				return new GuildBackup(guild, backupID, timestamp, autoBackup);
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to create backup", e));
	}

	public static GuildBackup saveCopy(GraphiteGuild guild, GuildBackup backup, PrivateKey decryptionKey, PublicKey encryptionKey) {
		return Graphite.getMySQL().run(con -> {
			String backupID = GraphiteUtil.randomShortID();

			try(PreparedStatement s = con.prepareStatement("INSERT INTO guilds_backups(GuildId, BackupName, Timestamp, GuildIcon, Channels, Roles, Bans, AESKey, Messages, OverviewSettings, Permissions, Config, AutoBackup) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				s.setString(1, guild.getID()); // GuildId
				s.setString(2, backupID); // BackupName
				long timestamp = System.currentTimeMillis();
				s.setLong(3, timestamp); // Timestamp

				Blob guildIcon = null;

				byte[] icon = backup.loadGuildIconRaw();
				if(icon != null) guildIcon = new SerialBlob(icon);

				s.setBlob(4, guildIcon); // GuildIcon
				s.setString(5, backup.loadChannelsData().toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); // Channels
				s.setString(6, backup.loadRolesData().toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); // Roles
				s.setString(7, backup.loadBansData().toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); //Bans

				if(decryptionKey != null && encryptionKey != null) {
					SecretKey key = GraphiteCrypto.generateSymmetricKey();
					Cipher keyEncryptionCipher = Cipher.getInstance("RSA");
					keyEncryptionCipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
					byte[] enc = keyEncryptionCipher.doFinal(key.getEncoded());

					s.setBlob(8, new SerialBlob(enc)); // AESKey
					MessagesData dt = backup.loadMessagesData(decryptionKey);
					s.setBlob(9, dt == null ? null : new SerialBlob(dt.getEncrypted(key))); // Messages
				}else {
					s.setBlob(8, (Blob) null); // AESKey
					s.setBlob(9, (Blob) null); // Messages
				}

				s.setString(10, backup.loadDiscordOverviewSettingsData().toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); // GuildOverviewSettings
				s.setString(11, backup.loadPermissionsData().toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); // Permissions
				s.setString(12, backup.loadConfigData().toJSON().toString()); // Config
				s.setBoolean(13, false); // AutoBackup
				s.execute();

				return new GuildBackup(guild, backupID, timestamp, false);
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to copy backup", e));
	}

	public static GuildBackup getBackupByName(GraphiteGuild guild, String name) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT Timestamp, AutoBackup FROM guilds_backups WHERE GuildId = ? AND BackupName = ?")) {
				s.setString(1, guild.getID());
				s.setString(2, name);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return null;
					return new GuildBackup(guild, name, r.getLong("Timestamp"), r.getBoolean("AutoBackup"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to get backup", e));
	}

	public static List<GuildBackup> getBackups(GraphiteGuild guild) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT BackupName, Timestamp, AutoBackup FROM guilds_backups WHERE GuildId = ?")) {
				s.setString(1, guild.getID());
				try(ResultSet r = s.executeQuery()) {
					List<GuildBackup> bs = new ArrayList<>();
					while(r.next()) {
						bs.add(new GuildBackup(guild, r.getString("BackupName"), r.getLong("Timestamp"), r.getBoolean("AutoBackup")));
					}
					return bs;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to get backups", e));
	}

	public static void deleteLastAutoBackup(GraphiteGuild guild) {
		Graphite.getMySQL().query("DELETE FROM guilds_backups WHERE GuildId = ? AND AutoBackup = ? ORDER BY Timestamp LIMIT 1", guild.getID(), true);
	}

	public boolean isCorrectKey(PrivateKey decryptionKey) {
		byte[] b = Graphite.getMySQL().query(byte[].class, null, "SELECT AESKey FROM guilds_backups WHERE GuildId = ? AND BackupName = ?", guild.getID(), name)
				.orElseThrowOther(e -> new FriendlyException("Failed to verify key", e));
		if(b == null) return false;
		try {
			GraphiteCrypto.decryptAESKey(b, decryptionKey);
			return true;
		}catch(Exception e) {
			return false;
		}
	}

	public static void renameBackup(GraphiteGuild g, String name, String newName) {
		Graphite.getMySQL().query("UPDATE guilds_backups SET BackupName = ? WHERE GuildId = ? AND BackupName = ?", newName, g.getID(), name);
	}

	public static InputStream downloadIcon(String url) {
		try {
			HttpClient cl = HttpClients.createDefault();
			HttpGet get = new HttpGet(url);
			get.setHeader("Authorization", Graphite.getBotInfo().getToken());
			HttpResponse r = cl.execute(get);
			return r.getEntity().getContent();
		}catch(Exception e){
			GraphiteDebug.log(DebugCategory.BACKUP, e);
			throw new FriendlyException("Failed to download guild icon");
		}
	}

	@JavaScriptFunction(calling = "getLastAutoBackup", returning = "lastAutoBackup", withGuild = true)
	public static void getLastAutoBackup() {}

	@JavaScriptFunction(calling = "getBackupInterval", returning = "backupInterval", withGuild = true)
	public static void getBackupInterval() {}

	@JavaScriptFunction(calling = "setBackupInterval", withGuild = true)
	public static void setBackupInterval(@JavaScriptParameter(name = "interval_days") int days) {}

	@JavaScriptFunction(calling = "disableAutoBackups", withGuild = true)
	public static void disableAutoBackups() {}

	@JavaScriptFunction(calling = "getBackups", returning = "backups", withGuild = true)
	public static void getBackups() {}

	@JavaScriptFunction(calling = "getBackupByName", returning = "backup", withGuild = true)
	public static void getBackup(@JavaScriptParameter(name = "backup_name") String id) {}

	@JavaScriptFunction(calling = "getBackupChannelsData", returning = "data", withGuild = true)
	public static void getBackupChannelsData(@JavaScriptParameter(name = "backup_name") String id) {}

	@JavaScriptFunction(calling = "getBackupRolesData", returning = "data", withGuild = true)
	public static void getBackupRolesData(@JavaScriptParameter(name = "backup_name") String id) {}

	@JavaScriptFunction(calling = "getBackupsOfGuild", returning = "backups", withGuild = true)
	public static void getBackupsOfGuild(@JavaScriptParameter(name = "guild_id") String id) {}

	@JavaScriptFunction(calling = "createBackup", withGuild = true)
	public static void createBackup() {}

	@JavaScriptFunction(calling = "deleteBackup", withGuild = true)
	public static void deleteBackup(@JavaScriptParameter(name = "backup_name") String id) {}

	@JavaScriptFunction(calling = "deleteAllBackups", withGuild = true)
	public static void deleteAllBackups() {}

	@JavaScriptFunction(calling = "restoreBackup", withGuild = true)
	public static void restoreBackup(@JavaScriptParameter(name = "backup_name") String id, @JavaScriptParameter(name = "key") String key, @JavaScriptParameter(name = "params") String... params) {}

	@JavaScriptFunction(calling = "copyBackup", withGuild = true)
	public static void copyBackup(@JavaScriptParameter(name = "from") String from, @JavaScriptParameter(name = "name") String name, @JavaScriptParameter(name = "key") String key) {}

	@JavaScriptFunction(calling = "canCreateBackup", returning = "canCreateBackup", withGuild = true)
	public static void canCreateBackup() {}

	@JavaScriptFunction(calling = "renameBackup", withGuild = true)
	public static void renameBackup(@JavaScriptParameter(name = "name") String name, @JavaScriptParameter(name = "new_name") String newName) {};

}
