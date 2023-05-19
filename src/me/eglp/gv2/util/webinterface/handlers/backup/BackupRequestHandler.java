package me.eglp.gv2.util.webinterface.handlers.backup;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.commands.backups.CommandBackup;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.util.backup.GuildBackup;
import me.eglp.gv2.util.backup.RestoreSelector;
import me.eglp.gv2.util.crypto.GraphiteCrypto;
import me.eglp.gv2.util.lang.LocalizedTimeUnit;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.queue.GraphiteTaskInfo;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.mrcore.misc.Complex;

public class BackupRequestHandler {

	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss z");
	private static ZonedDateTime utcTime(long localMillis) {
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(localMillis), ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
	}

	@WebinterfaceHandler(requestMethod = "canCreateBackup", requireGuild = true, requireFeatures = GraphiteFeature.BACKUPS)
	public static WebinterfaceResponse canCreateBackup(WebinterfaceRequestEvent event) {
		JSONObject o = new JSONObject();
		o.put("canCreateBackup", event.getSelectedGuild().canCreateBackup());
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "getBackupByName", requireGuild = true, requireFeatures = GraphiteFeature.BACKUPS)
	public static WebinterfaceResponse getBackupByName(WebinterfaceRequestEvent event) {
		String name = event.getRequestData().getString("backup_name");

		GraphiteGuild g = event.getSelectedGuild();
		GuildBackup b = g.getBackupByName(name);

		if(b == null) {
			return WebinterfaceResponse.error("Backup doesn't exist");
		}

		JSONObject o = new JSONObject();
		o.put("backup", b.toWebinterfaceObject());

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "getBackupChannelsData", requireGuild = true, requireFeatures = GraphiteFeature.BACKUPS)
	public static WebinterfaceResponse getBackupChannelsData(WebinterfaceRequestEvent event) {
		String name = event.getRequestData().getString("backup_name");

		GraphiteGuild g = event.getSelectedGuild();
		GuildBackup b = g.getBackupByName(name);

		if(b == null) {
			return WebinterfaceResponse.error("Backup doesn't exist");
		}

		JSONObject o = new JSONObject();
		o.put("data", b.loadChannelsData().toWebinterfaceObject());

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "getBackupRolesData", requireGuild = true, requireFeatures = GraphiteFeature.BACKUPS)
	public static WebinterfaceResponse getBackupRolesData(WebinterfaceRequestEvent event) {
		String name = event.getRequestData().getString("backup_name");

		GraphiteGuild g = event.getSelectedGuild();
		GuildBackup b = g.getBackupByName(name);

		if(b == null) {
			return WebinterfaceResponse.error("Backup doesn't exist");
		}

		JSONObject o = new JSONObject();
		o.put("data", b.loadRolesData().toWebinterfaceObject());

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "createBackup", requireGuild = true, requireFeatures = GraphiteFeature.BACKUPS)
	public static WebinterfaceResponse createBackup(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!event.getSelectedGuild().canCreateBackup()) {
			return WebinterfaceResponse.error("Max amount of backups reached");
		}

		KeyPair key = GraphiteCrypto.generateKeyPair();

		GuildBackup b = GuildBackup.createNew(g, key.getPublic(), 100, false);

		JSONObject o = new JSONObject();
		o.put("backup", b.toWebinterfaceObject());

		byte[] privateKeyData = key.getPrivate().getEncoded();
		o.put("decryptionKey", Base64.getEncoder().encodeToString(privateKeyData));

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "restoreBackup", requireGuild = true, requireFeatures = GraphiteFeature.BACKUPS)
	public static WebinterfaceResponse restoreBackup(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();

		long cd = event.getSelectedGuild().getBackupCooldown();
		if(cd > 0) {
			return WebinterfaceResponse.error("Please wait another " + LocalizedTimeUnit.formatTime(event.getSelectedGuild(), cd));
		}

		String name = event.getRequestData().getString("backup_name");
		GuildBackup b = g.getBackupByName(name);

		if(b == null) {
			return WebinterfaceResponse.error("Backup doesn't exist");
		}

		List<String> params = Complex.castList(event.getRequestData().getJSONArray("params"), String.class).get();

		PrivateKey k = null;

		if(event.getRequestData().isOfType("key", JSONType.STRING)) {
			String key = event.getRequestData().getString("key");
			byte[] encoded = Base64.getDecoder().decode(key);
			k = GraphiteCrypto.decodePrivateKey(encoded);
		}

		final PrivateKey fK = k;
		if(fK != null && !b.isCorrectKey(fK)) {
			return WebinterfaceResponse.error("Invalid decryption key");
		}

		g.getResponsibleQueue().queueHeavy(g, new GraphiteTaskInfo(GuildBackup.TASK_ID, "Restoring backup (webinterface)"), () -> {
			Graphite.withBot(GraphiteMultiplex.getHighestRelativeHierarchy(g, GraphiteFeature.BACKUPS), () -> b.restore(g, fK, params.stream().map(p -> RestoreSelector.valueOf(p)).collect(Collectors.toCollection(() -> EnumSet.noneOf(RestoreSelector.class)))));
		});

		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "copyBackup", requireGuild = true, requireFeatures = GraphiteFeature.BACKUPS)
	public static WebinterfaceResponse copyBackup(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();

		String fromID = event.getRequestData().getString("from");
		GraphiteGuild from = Graphite.getGuild(fromID);
		if(from == null) {
			return WebinterfaceResponse.error("Guild doesn't exist");
		}

		boolean hasPerm = from.getPermissionManager().hasPermission(event.getUser().getDiscordUser(), DefaultPermissions.BACKUP_COPY_TO_OTHER);
		if(!hasPerm) {
			return WebinterfaceResponse.error("You doesn't have the permissions on this guild to copy a backup");
		}

		String name = event.getRequestData().getString("name");
		GuildBackup b = from.getBackupByName(name);
		if(b == null) {
			return WebinterfaceResponse.error("Backup doesn't exist on this guild");
		}

		PrivateKey k = null;

		if(event.getRequestData().isOfType("key", JSONType.STRING)) {
			String key = event.getRequestData().getString("key");
			byte[] encoded = Base64.getDecoder().decode(key);
			k = GraphiteCrypto.decodePrivateKey(encoded);
		}

		final PrivateKey decryptionKey = k;
		if(decryptionKey != null && !b.isCorrectKey(decryptionKey)) {
			return WebinterfaceResponse.error("Invalid decryption key");
		}

		KeyPair kp = decryptionKey != null && b.hasMessagesData() ? GraphiteCrypto.generateKeyPair() : null;

		JSONObject o = new JSONObject();

		GuildBackup fromBackup = GuildBackup.saveCopy(g, b, decryptionKey, kp != null ? kp.getPublic() : null);
		o.put("backup", fromBackup.toWebinterfaceObject());

		if(kp != null) {
			byte[] privateKeyData = kp.getPrivate().getEncoded();
			o.put("decryptionKey", Base64.getEncoder().encodeToString(privateKeyData));
			return WebinterfaceResponse.success(o);
		}

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "deleteBackup", requireGuild = true, requireFeatures = GraphiteFeature.BACKUPS)
	public static WebinterfaceResponse deleteBackup(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();

		String name = event.getRequestData().getString("backup_name");
		GuildBackup b = g.getBackupByName(name);

		if(b == null) {
			return WebinterfaceResponse.error("Backup doesn't exist");
		}

		b.delete();

		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "deleteAllBackups", requireGuild = true, requireFeatures = GraphiteFeature.BACKUPS)
	public static WebinterfaceResponse deleteAllBackups(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		List<GuildBackup> backups = g.getBackups();

		for(GuildBackup b : backups) b.delete();

		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "getBackups", requireGuild = true, requireFeatures = GraphiteFeature.BACKUPS)
	public static WebinterfaceResponse getBackups(WebinterfaceRequestEvent event) {
		List<GuildBackup> backups = event.getSelectedGuild().getBackups();

		JSONArray arr = new JSONArray();
		for(GuildBackup b : backups) {
			arr.add(b.toWebinterfaceObject());
		}

		JSONObject o = new JSONObject();
		o.put("backups", arr);

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "getBackupsOfGuild", requireGuild = true, requireFeatures = GraphiteFeature.BACKUPS)
	public static WebinterfaceResponse getBackupsOfGuild(WebinterfaceRequestEvent event) {
		String gID = event.getRequestData().getString("guild_id");
		GraphiteGuild targetGuild = Graphite.getGuild(gID);

		if(targetGuild == null) {
			return WebinterfaceResponse.error("Target guild doesn't exist");
		}

		List<GuildBackup> backups = targetGuild.getBackups();

		JSONArray arr = new JSONArray();
		for(GuildBackup b : backups) {
			arr.add(b.toWebinterfaceObject());
		}

		JSONObject o = new JSONObject();
		o.put("backups", arr);

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "getLastAutoBackup", requireGuild = true, requireFeatures = GraphiteFeature.BACKUPS)
	public static WebinterfaceResponse getLastAutoBackup(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		long lastBackup = g.getBackupConfig().getLastAutoBackup();

		JSONObject obj = new JSONObject();
		obj.put("lastAutoBackup", (lastBackup == -1 ? -1 : dtf.format(utcTime(lastBackup))));

		return WebinterfaceResponse.success(obj);
	}

	@WebinterfaceHandler(requestMethod = "getBackupInterval", requireGuild = true, requireFeatures = GraphiteFeature.BACKUPS)
	public static WebinterfaceResponse getBackupInterval(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		int days = g.getBackupConfig().getBackupInterval();

		JSONObject obj = new JSONObject();
		obj.put("backupInterval", days);

		return WebinterfaceResponse.success(obj);
	}

	@WebinterfaceHandler(requestMethod = "setBackupInterval", requireGuild = true, requireFeatures = GraphiteFeature.BACKUPS)
	public static WebinterfaceResponse setBackupInterval(WebinterfaceRequestEvent event) {
		int days = event.getRequestData().getInt("interval_days");

		if(days <= 0) {
			return WebinterfaceResponse.error("Invalid interval");
		}

		event.getSelectedGuild().getBackupConfig().setBackupInterval(days);

		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "disableAutoBackups", requireGuild = true, requireFeatures = GraphiteFeature.BACKUPS)
	public static WebinterfaceResponse disableAutoBackups(WebinterfaceRequestEvent event) {
		event.getSelectedGuild().getBackupConfig().disableBackupInterval();
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "renameBackup", requireGuild = true, requireFeatures = GraphiteFeature.RECORD)
	public static WebinterfaceResponse renameBackup(WebinterfaceRequestEvent event) {
		String name = event.getRequestData().getString("name");
		String newName = event.getRequestData().getString("new_name");

		GraphiteGuild g = event.getSelectedGuild();
		GuildBackup b = GuildBackup.getBackupByName(g, name);
		if(b == null) {
			return WebinterfaceResponse.error("Backup doesn't exist");
		}

		GuildBackup nB = GuildBackup.getBackupByName(g, newName);
		if(nB != null) {
			return WebinterfaceResponse.error("A backup with this name already exist");
		}

		if(!CommandBackup.BACKUP_NAME_PATTERN.matcher(newName).matches()) {
			return WebinterfaceResponse.error("New backup name doesn't match our requirements");
		}

		GuildBackup.renameBackup(g, name, newName);

		return WebinterfaceResponse.success();
	}

}
