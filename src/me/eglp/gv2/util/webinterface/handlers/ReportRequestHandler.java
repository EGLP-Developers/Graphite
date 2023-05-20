package me.eglp.gv2.util.webinterface.handlers;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GuildReport;
import me.eglp.gv2.guild.config.GuildReportsConfig;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class ReportRequestHandler {

	@WebinterfaceHandler(requestMethod = "getReports", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_MODERATION)
	public static WebinterfaceResponse getReports(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		List<GuildReport> reports = g.getReportsConfig().getReports();
		JSONObject o = new JSONObject();
		o.put("reports", new JSONArray(reports.stream().map(r -> r.toWebinterfaceObject()).collect(Collectors.toList())));
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "deleteReport", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_MODERATION)
	public static WebinterfaceResponse deleteReport(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildReportsConfig c = g.getReportsConfig();
		String id = event.getRequestData().getString("report_id");
		c.removeReportByID(id);
		return WebinterfaceResponse.success();
	}

}
