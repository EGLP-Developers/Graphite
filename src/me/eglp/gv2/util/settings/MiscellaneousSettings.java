package me.eglp.gv2.util.settings;

import java.util.Arrays;
import java.util.List;

import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONListType;
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
	
	@JSONValue
	@JSONListType(JSONType.STRING)
	private List<String> emojiServerIDs;
	
	@JSONConstructor
	private MiscellaneousSettings() {}
	
	public static MiscellaneousSettings createDefault() {
		MiscellaneousSettings s = new MiscellaneousSettings();
		s.messageServerID = "Server ID for messages";
		s.upvotesChannelID = "Channel ID for upvotes";
		s.reportedTemplatesChannelID = "Channel ID for reported templates";
		s.testingServerID = "Server ID for slash command testing (beta only)";
		s.emojiServerIDs = Arrays.asList("Emoji Server ID", "Another Emoji Server ID");
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
	
	public List<String> getEmojiServerIDs() {
		return emojiServerIDs;
	}
	
	public void validate(List<String> errors) {
		if(messageServerID == null) errors.add("Message server id missing");
		if(upvotesChannelID == null) errors.add("Upvotes channel id missing");
		if(reportedTemplatesChannelID == null) errors.add("Reported templates channel id missing");
		if(emojiServerIDs == null) errors.add("Emoji server ids missing");
	}
	
}
