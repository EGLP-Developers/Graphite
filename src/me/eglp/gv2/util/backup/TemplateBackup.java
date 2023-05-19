package me.eglp.gv2.util.backup;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.backup.data.channels.ChannelsData;
import me.eglp.gv2.util.backup.data.config.GuildConfigData;
import me.eglp.gv2.util.backup.data.overview_settings.OverviewSettingsData;
import me.eglp.gv2.util.backup.data.permissions.PermissionsData;
import me.eglp.gv2.util.backup.data.roles.RolesData;
import me.eglp.gv2.util.mysql.GraphiteMySQL.UnsafeFunction;
import me.eglp.gv2.util.mysql.SQLTable;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.SerializationOption;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "global_template_upvotes",
	columns = {
		"TemplateId varchar(255) DEFAULT NULL",
		"UserId varchar(255) DEFAULT NULL",
		"PRIMARY KEY (TemplateId, UserId)"
	}
)
@SQLTable(
	name = "global_templates",
	columns = {
		"TemplateId varchar(255) DEFAULT NULL",
		"AuthorId varchar(255) DEFAULT NULL",
		"Timestamp bigint DEFAULT NULL",
		"GuildIcon mediumblob DEFAULT NULL",
		"Channels longtext DEFAULT NULL",
		"Roles longtext DEFAULT NULL",
		"OverviewSettings longtext DEFAULT NULL",
		"Permissions longtext DEFAULT NULL",
		"Config longtext DEFAULT NULL",
		"Name varchar(255) DEFAULT NULL",
		"Description mediumtext DEFAULT NULL",
		"PRIMARY KEY (TemplateId)"
	}
)
@JavaScriptClass(name = "TempBackup")
public class TemplateBackup implements WebinterfaceObject {

	public static final int
		MAX_TEMPLATE_NAME_LENGTH = 50,
		MAX_TEMPLATE_DESCRIPTION_LENGTH = 255,
		USER_TEMPLATE_CREATE_COOLDOWN = 60 * 60 * 1000;

	@JavaScriptValue(getter = "getID")
	private String id;

	@JavaScriptValue(getter = "getAuthorID")
	private String authorID;

	@JavaScriptValue(getter = "getTimestamp")
	private long timestamp;

	@JavaScriptValue(getter = "getName")
	private String name;

	@JavaScriptValue(getter = "getDescription")
	private String description;

	private TemplateBackup(String id, String authorID, long timestamp, String name, String description) {
		this.id = id;
		this.authorID = authorID;
		this.timestamp = timestamp;
		this.name = name;
		this.description = description;
	}

	public String getID() {
		return id;
	}

	public String getAuthorID() {
		return authorID;
	}

	public GraphiteUser getAuthor() {
		return Graphite.getGlobalUser(authorID);
	}

	public long getTimestamp() {
		return timestamp;
	}

	@JavaScriptGetter(name = "getUpvotes", returning = "upvotes")
	public int loadUpvotes() {
		return Graphite.getMySQL().query(Long.class, 0L, "SELECT COUNT(UserId) FROM global_template_upvotes WHERE TemplateId = ?", id)
				.orElseThrowOther(e -> new FriendlyException("Failed to retrieve upvote data from MySQL", e))
				.intValue();
	}

	public boolean hasUpvoted(String userID) {
		return Graphite.getMySQL().query(String.class, null, "SELECT UserId FROM global_template_upvotes WHERE TemplateId = ? AND UserId = ?", id, userID)
				.orElseThrowOther(e -> new FriendlyException("Failed to load upvote from MySQL", e)) != null;
	}

	public void addUpvote(String userID) {
		Graphite.getMySQL().query("INSERT IGNORE INTO global_template_upvotes(TemplateId, UserId) VALUES(?, ?)", id, userID);
	}

	public void removeUpvote(String userID) {
		Graphite.getMySQL().query("DELETE FROM global_template_upvotes WHERE TemplateId = ? AND UserId = ?", id, userID);
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public byte[] loadGuildIconRaw() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT GuildIcon FROM global_templates WHERE TemplateId = ?")) {
				st.setString(1, id);

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
			try(PreparedStatement st = con.prepareStatement("SELECT Channels FROM global_templates WHERE TemplateId = ?")) {
				st.setString(1, id);

				try(ResultSet set = st.executeQuery()) {
					if(!set.next()) return null;
					return ChannelsData.load(set.getString("Channels"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve channels data from MySQL", e));
	}

	public RolesData loadRolesData() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT Roles FROM global_templates WHERE TemplateId = ?")) {
				st.setString(1, id);

				try(ResultSet set = st.executeQuery()) {
					if(!set.next()) return null;
					return RolesData.load(set.getString("Roles"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve roles data from MySQL", e));
	}

	public PermissionsData loadPermissionsData() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT Permissions FROM global_templates WHERE TemplateId = ?")) {
				st.setString(1, id);

				try(ResultSet set = st.executeQuery()) {
					if(!set.next()) return null;
					return PermissionsData.load(set.getString("Permissions"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve permissions data from MySQL", e));
	}

	public GuildConfigData loadConfigData() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT Config FROM global_templates WHERE TemplateId = ?")) {
				st.setString(1, id);

				try(ResultSet set = st.executeQuery()) {
					if(!set.next()) return null;
					return GuildConfigData.load(set.getString("Permissions"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to config permissions data from MySQL", e));
	}

	public OverviewSettingsData loadDiscordOverviewSettingsData() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT OverviewSettings FROM global_templates WHERE TemplateId = ?")) {
				st.setString(1, id);

				try(ResultSet set = st.executeQuery()) {
					if(!set.next()) return null;
					return OverviewSettingsData.load(set.getString("OverviewSettings"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to retrieve overview settings data from MySQL", e));
	}

	public long restore(GraphiteGuild guild, EnumSet<RestoreSelector> selectors) {
		long start = System.currentTimeMillis();

		IDMappings mappings = new IDMappings();

		if(RestoreSelector.DISCORD_ROLES.appliesTo(selectors)) {
			RolesData roles = loadRolesData();
			roles.restore(guild, RestoreSelector.DISCORD_ROLE_ASSIGNMENTS.appliesTo(selectors), mappings);
		}

		if(RestoreSelector.DISCORD_CHANNELS.appliesTo(selectors)) {
			ChannelsData channels = loadChannelsData();
			channels.restore(guild, selectors, mappings);
		}

		if(RestoreSelector.PERMISSIONS.appliesTo(selectors)) {
			PermissionsData d = loadPermissionsData();
			d.restore(guild);
		}

		if(RestoreSelector.DISCORD_OVERVIEW_SETTINGS.appliesTo(selectors)) {
			OverviewSettingsData d = loadDiscordOverviewSettingsData();
			d.restore(guild, mappings);
		}

		return System.currentTimeMillis() - start;
	}

	public static TemplateBackup createNew(GuildBackup backup, GraphiteUser author, String name, String description) {
		return Graphite.getMySQL().run(con -> {
			String templateID = GraphiteUtil.randomShortID();

			try(PreparedStatement s = con.prepareStatement("INSERT INTO global_templates(TemplateId, AuthorId, Timestamp, GuildIcon, Channels, Roles, OverviewSettings, Permissions, Config, Name, Description) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				s.setString(1, templateID); // TemplateId
				s.setString(2, author.getID()); // AuthorId
				long timestamp = System.currentTimeMillis();
				s.setLong(3, timestamp); // Timestamp
				byte[] bytes = backup.loadGuildIconRaw();
				s.setBlob(4, bytes == null ? null : new SerialBlob(bytes)); // GuildIcon
				s.setString(5, backup.loadChannelsData().toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); // Channels
				s.setString(6, backup.loadRolesData().toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); // Roles
				s.setString(7, backup.loadDiscordOverviewSettingsData().toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); // GuildOverviewSettings
				s.setString(8, backup.loadPermissionsData().toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); // Permissions
				s.setString(9, backup.loadConfigData().toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString()); // Config
				s.setString(10, name); // Name
				s.setString(11, description); // Description
				s.execute();

				return new TemplateBackup(templateID, author.getID(), timestamp, name, description);
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to create backup", e));
	}

	public static TemplateBackup getTemplateByID(String id) {
		return Graphite.getMySQL().run((UnsafeFunction<Connection, TemplateBackup>) con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM global_templates WHERE TemplateId = ?")) {
				s.setString(1, id);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return null;
					return new TemplateBackup(id, r.getString("AuthorId"), r.getLong("Timestamp"), r.getString("Name"), r.getString("Description"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to get backup", e));
	}

	public static List<TemplateBackup> getTemplateBackups() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM global_templates")) {
				try(ResultSet r = s.executeQuery()) {
					List<TemplateBackup> bs = new ArrayList<>();
					while(r.next()) {
						bs.add(new TemplateBackup(r.getString("TemplateId"), r.getString("AuthorId"), r.getLong("Timestamp"), r.getString("Name"), r.getString("Description")));
					}
					return bs;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to get template backups", e));
	}

	private static long getLastTemplate(String userID) {
		return Graphite.getMySQL().query(Long.class, 0L,  "SELECT Timestamp FROM global_templates WHERE AuthorId = ? ORDER BY Timestamp DESC LIMIT 1", userID)
				.orElseThrowOther(e -> new FriendlyException("Failed to load last template timestamp from MySQL", e));
	}

	public static long getTemplateCreateCooldown(GraphiteUser user) {
		return Math.max(0, (getLastTemplate(user.getID()) + USER_TEMPLATE_CREATE_COOLDOWN) - System.currentTimeMillis());
	}

	public void delete() {
		Graphite.getMySQL().query("DELETE FROM global_templates WHERE TemplateId = ?", id);
	}

	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("upvotes", loadUpvotes());
	}

	@JavaScriptFunction(calling = "getTempBackups", returning = "template_backups", withGuild = true)
	public static void getTempBackups() {}

	@JavaScriptFunction(calling = "createTemplateBackup", returning = "template_backup", withGuild = true)
	public static void createTemplateBackup(@JavaScriptParameter(name = "backup_name") String backupName, @JavaScriptParameter(name = "name") String name, @JavaScriptParameter(name = "description") String description) {}

	@JavaScriptFunction(calling = "deleteTemplateBackup", withGuild = true)
	public static void deleteTemplateBackup(@JavaScriptParameter(name = "backup_id") String id) {}

	@JavaScriptFunction(calling = "getTemplateBackupChannelsData", returning = "data", withGuild = true)
	public static void getTemplateBackupChannelsData(@JavaScriptParameter(name = "backup_id") String id) {}

	@JavaScriptFunction(calling = "getTemplateBackupRolesData", returning = "data", withGuild = true)
	public static void getTemplateBackupRolesData(@JavaScriptParameter(name = "backup_id") String id) {}

	@JavaScriptFunction(calling = "loadTemplateBackup", withGuild = true)
	public static void loadTemplateBackup(@JavaScriptParameter(name = "backup_id") String id, @JavaScriptParameter(name = "params") String... params) {}

	@JavaScriptFunction(calling = "reportTemplateBackupByID", withGuild = false)
	public static void reportTemplateBackupByID(@JavaScriptParameter(name = "id") String id, @JavaScriptParameter(name = "reason") String reason) {}

	@JavaScriptFunction(calling = "upvoteTemplateBackupByID", withGuild = false)
	public static void upvoteTemplateBackupByID(@JavaScriptParameter(name = "id") String id) {}

	@JavaScriptFunction(calling = "hasUpvotedTemplateByID", returning = "upvoted", withGuild = false)
	public static void hasUpvotedTemplateByID(@JavaScriptParameter(name = "id") String id, @JavaScriptParameter(name = "user_id") String userID) {}

}
