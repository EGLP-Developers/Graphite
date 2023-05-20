package me.eglp.gv2.guild;

import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

@JavaScriptClass(name = "Report")
public class GuildReport implements WebinterfaceObject, JSONConvertible{

	@JSONValue
	private GraphiteUser reporter;

	@JSONValue
	private GraphiteUser reported;

	@JSONValue
	@JavaScriptValue(getter = "getReason")
	private String reason;

	@JSONValue
	@JavaScriptValue(getter = "getID")
	private String id;

	@JSONValue
	@JavaScriptValue(getter = "getTimestamp")
	private long timestamp;

	public GuildReport(String id, GraphiteUser reporter, GraphiteUser reported, String reason, long timestamp) {
		this.id = id;
		this.reporter = reporter;
		this.reported = reported;
		this.reason = reason;
		this.timestamp = timestamp;
	}

	public String getID() {
		return id;
	}

	@JavaScriptGetter(returning = "reporter")
	public GraphiteUser getReporter() {
		return reporter;
	}

	@JavaScriptGetter(returning = "reported")
	public GraphiteUser getReported() {
		return reported;
	}

	public String getReason() {
		return reason;
	}

	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof GuildReport)) return false;
		GuildReport o = (GuildReport) obj;
		return reported.equals(o.reported) &&
				reporter.equals(o.reporter) &&
				reason.equals(o.reason);
	}

	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("reporter", reporter.getName() + "#" + reporter.getDiscriminator());
		object.put("reported", reported.getName() + "#" + reported.getDiscriminator());
	}

	@JavaScriptFunction(calling = "getReports", returning = "reports", withGuild = true)
	public static void getReports() {};

	@JavaScriptFunction(calling = "deleteReport", withGuild = true)
	public static void deleteReport(@JavaScriptParameter(name = "report_id") String id) {};

}
