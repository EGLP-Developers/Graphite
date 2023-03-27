package me.eglp.gv2.util.settings;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class MiscellaneousSettings implements JSONConvertible {
	
	@JSONValue
	private String messageServerID;
	
	@JSONValue
	private String upvotesChannelID;
	
	@JSONValue
	private String reportedTemplatesChannelID;
	
	@JSONValue
	private String testingServerID;
	
	@JSONConstructor
	private MiscellaneousSettings() {}
	
	public static MiscellaneousSettings createDefault() {
		MiscellaneousSettings s = new MiscellaneousSettings();
		s.messageServerID = "Server ID for messages";
		s.upvotesChannelID = "Channel ID for upvotes";
		s.reportedTemplatesChannelID = "Channel ID for reported templates";
		s.testingServerID = "Server ID for slash command testing (beta only)";
		return s;
	}
	
	public String getMessageServerID() {
		return messageServerID;
	}
	
	public String getUpvotesChannelID() {
		return upvotesChannelID;
	}
	
	public String getReportedTemplatesChannelID() {
		return reportedTemplatesChannelID;
	}
	
	public String getTestingServerID() {
		return testingServerID;
	}

}
