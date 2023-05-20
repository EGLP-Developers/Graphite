package me.eglp.gv2.util.webinterface.handlers.backup;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteTextChannel;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.backup.GuildBackup;
import me.eglp.gv2.util.backup.RestoreSelector;
import me.eglp.gv2.util.backup.TemplateBackup;
import me.eglp.gv2.util.lang.LocalizedTimeUnit;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.queue.GraphiteTaskInfo;
import me.eglp.gv2.util.settings.MiscellaneousSettings;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.Complex;
import net.dv8tion.jda.api.EmbedBuilder;

public class TemplateRequestHandler {

	@WebinterfaceHandler(requestMethod = "getTempBackups", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_BACKUPS)
	public static WebinterfaceResponse getTempBackups(WebinterfaceRequestEvent event) {
		List<TemplateBackup> templates = TemplateBackup.getTemplateBackups();

		JSONArray arr = new JSONArray();
		for(TemplateBackup b : templates) {
			arr.add(b.toWebinterfaceObject());
		}

		JSONObject o = new JSONObject();
		o.put("template_backups", arr);

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "createTemplateBackup", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_BACKUPS)
	public static WebinterfaceResponse createTemplateBackup(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();

		long cd = TemplateBackup.getTemplateCreateCooldown(event.getUser().getDiscordUser());
		if(cd > 0) {
			return WebinterfaceResponse.error("Please wait another " + LocalizedTimeUnit.formatTime(g, cd) + " before creating another template");
		}

		GuildBackup b = g.getBackupByName(event.getRequestData().getString("backup_name"));
		if(b == null) {
			return WebinterfaceResponse.error("Backup doesn't exist");
		}

		String name = event.getRequestData().getString("name");
		String desc = event.getRequestData().getString("description");

		if(name.length() > TemplateBackup.MAX_TEMPLATE_NAME_LENGTH) {
			return WebinterfaceResponse.error("The name of the template cannot be longer than " + TemplateBackup.MAX_TEMPLATE_NAME_LENGTH + " characters");
		}

		if(desc.length() > TemplateBackup.MAX_TEMPLATE_DESCRIPTION_LENGTH) {
			return WebinterfaceResponse.error("The description of the template cannot be longer than " + TemplateBackup.MAX_TEMPLATE_DESCRIPTION_LENGTH + " characters");
		}

		// NONBETA: Check special chars

		TemplateBackup t = TemplateBackup.createNew(b, event.getUser().getDiscordUser(), name, desc);

		JSONObject o = new JSONObject();
		o.put("template_backup", t.toWebinterfaceObject());

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "deleteTemplateBackup", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_BACKUPS)
	public static WebinterfaceResponse deleteTemplateBackup(WebinterfaceRequestEvent event) {
		String id = event.getRequestData().getString("backup_id");
		TemplateBackup t = TemplateBackup.getTemplateByID(id);

		if(t == null) {
			return WebinterfaceResponse.error("Template may be broken or doesn't exist anymore");
		}

		if(!event.getUser().isAdmin() && !event.getUser().getDiscordUser().getID().equalsIgnoreCase(t.getAuthorID())) {
			return WebinterfaceResponse.error("You are not allowed to delete other templates");
		}

		t.delete();

		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "loadTemplateBackup", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_BACKUPS)
	public static WebinterfaceResponse loadTemplateBackup(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();

		long cd = event.getSelectedGuild().getBackupCooldown();;
		if(cd > 0) {
			return WebinterfaceResponse.error("Please wait another " + LocalizedTimeUnit.formatTime(g, cd));
		}

		String id = event.getRequestData().getString("backup_id");
		TemplateBackup t = TemplateBackup.getTemplateByID(id);

		if(t == null) {
			return WebinterfaceResponse.error("Template doesn't exist");
		}

		List<String> params = Complex.castList(event.getRequestData().getJSONArray("params"), String.class).get();

		g.getResponsibleQueue().queueHeavy(g, new GraphiteTaskInfo(GuildBackup.TASK_ID, "Load template backup (webinterface)"), () -> {
			t.restore(g, params.stream().map(p -> RestoreSelector.valueOf(p)).collect(Collectors.toCollection(() -> EnumSet.noneOf(RestoreSelector.class))));
		});

		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "reportTemplateBackupByID", requireGuild = false, requirePermissions = DefaultPermissions.WEBINTERFACE_BACKUPS)
	public static WebinterfaceResponse reportTemplateBackupByID(WebinterfaceRequestEvent event) {
		String templateID = event.getRequestData().getString("id");
		String reason = event.getRequestData().getString("reason");

		TemplateBackup tb = TemplateBackup.getTemplateByID(templateID);
		if(tb == null) {
			return WebinterfaceResponse.error("Template doesn't exist");
		}

		MiscellaneousSettings misc = Graphite.getBotInfo().getMiscellaneous();
		GraphiteTextChannel tc = Graphite.getGuild(misc.getMessageServerID()).getTextChannelByID(misc.getReportedTemplatesChannelID());

		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Reported Template: " + tb.getName());
		eb.setDescription(reason);
		eb.addField("ID", tb.getID(), true);
		if(tb.getAuthor() != null) eb.addField("Author", tb.getAuthor().getName(), true);

		tc.sendMessage(eb.build());

		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "getTemplateBackupChannelsData", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_BACKUPS)
	public static WebinterfaceResponse getTemplateBackupChannelsData(WebinterfaceRequestEvent event) {
		String id = event.getRequestData().getString("backup_id");

		TemplateBackup b = TemplateBackup.getTemplateByID(id);

		if(b == null) {
			return WebinterfaceResponse.error("Template doesn't exist");
		}

		JSONObject o = new JSONObject();
		o.put("data", b.loadChannelsData().toWebinterfaceObject());

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "getTemplateBackupRolesData", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_BACKUPS)
	public static WebinterfaceResponse getTemplateBackupRolesData(WebinterfaceRequestEvent event) {
		String id = event.getRequestData().getString("backup_id");

		TemplateBackup b = TemplateBackup.getTemplateByID(id);

		if(b == null) {
			return WebinterfaceResponse.error("Template doesn't exist");
		}

		JSONObject o = new JSONObject();
		o.put("data", b.loadRolesData().toWebinterfaceObject());

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "upvoteTemplateBackupByID", requireGuild = false, requirePermissions = DefaultPermissions.WEBINTERFACE_BACKUPS)
	public static WebinterfaceResponse upvoteTemplateBackupByID(WebinterfaceRequestEvent event) {
		String backupID = event.getRequestData().getString("id");

		TemplateBackup tb = TemplateBackup.getTemplateByID(backupID);

		if(event.getUser().getDiscordUser().equals(tb.getAuthor())) {
			return WebinterfaceResponse.error("You can't upvote your own backup");
		}

		String userID = event.getUser().getDiscordUser().getID();

		if(tb.hasUpvoted(userID)) {
			tb.removeUpvote(userID);
		}else{
			tb.addUpvote(userID);
		}

		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "hasUpvotedTemplateByID", requireGuild = false, requirePermissions = DefaultPermissions.WEBINTERFACE_BACKUPS)
	public static WebinterfaceResponse hasUpvotedTemplateByID(WebinterfaceRequestEvent event) {
		String backupID = event.getRequestData().getString("id");
		String userID = event.getRequestData().getString("user_id");

		TemplateBackup tb = TemplateBackup.getTemplateByID(backupID);

		JSONObject o = new JSONObject();
		o.put("upvoted", tb.hasUpvoted(userID));
		return WebinterfaceResponse.success(o);
	}

}
