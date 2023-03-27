package me.eglp.gv2.util.base.guild.config;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.sql.rowset.serial.SerialBlob;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteMember;
import me.eglp.gv2.util.base.guild.GuildReport;
import me.eglp.gv2.util.base.guild.chatreport.GuildChatReport;
import me.eglp.gv2.util.base.guild.chatreport.GuildChatReportMessage;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.crypto.GraphiteCrypto;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.converter.SerializationOption;
import me.mrletsplay.mrcore.misc.FriendlyException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

@SQLTable(
	name = "guilds_chatreports",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"Id varchar(255) NOT NULL",
		"ReporterId varchar(255) DEFAULT NULL",
		"ChannelName varchar(255) DEFAULT NULL",
		"AESKey tinyblob DEFAULT NULL",
		"Messages longblob DEFAULT NULL",
		"Timestamp bigint DEFAULT NULL",
		"PRIMARY KEY (Id)"
	},
	guildReference = "GuildId"
)
@SQLTable(
	name = "guilds_reports",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"Id varchar(255) NOT NULL",
		"ReporterId varchar(255) DEFAULT NULL",
		"ReportedId varchar(255) DEFAULT NULL",
		"Reason longtext DEFAULT NULL",
		"Timestamp bigint DEFAULT NULL",
		"PRIMARY KEY (Id)"
	},
	guildReference = "GuildId"
)
@SQLTable(
	name = "guilds_reports_config",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"ChatReportsKey tinyblob DEFAULT NULL",
		"PRIMARY KEY (GuildId)"
	},
	guildReference = "GuildId"
)
public class GuildReportsConfig {
	
	private GraphiteGuild guild;
	
	public GuildReportsConfig(GraphiteGuild guild) {
		this.guild = guild;
	}
	
	public List<GuildReport> getReports() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM guilds_reports WHERE GuildId = ? ORDER BY Timestamp DESC")) {
				stmt.setString(1, guild.getID());
	
				try(ResultSet s = stmt.executeQuery()) {
					List<GuildReport> reports = new ArrayList<>();
					while(s.next()) {
						String id = s.getString("Id");
						
						String reporterID = s.getString("ReporterId");
						GraphiteMember reporter = guild.getMember(reporterID);
						if(reporter == null) continue;
						
						String reportedID = s.getString("ReportedId");
						GraphiteMember reported = guild.getMember(reportedID);
						if(reported == null) continue;
						
						String reason = s.getString("Reason");
						long timestamp = s.getLong("Timestamp");
						
						GuildReport r = new GuildReport(id, reporter, reported, reason, timestamp);
						if(!r.isValid()) continue;
						reports.add(r);
					}
					return reports;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load reports from MySQL", e));
		
	}
	
	public void addReport(GuildReport report) {
		Graphite.getMySQL().query("INSERT IGNORE INTO guilds_reports(GuildId, Id, ReporterId, ReportedId, Reason, Timestamp) VALUES(?, ?, ?, ?, ?, ?)", guild.getID(), report.getID(), report.getReporter().getID(), report.getReported().getID(), report.getReason(), report.getTimestamp());
	}
	
	public void removeReport(GuildReport report) {
		removeReportByID(report.getID());
	}
	
	public void removeReportByID(String reportID) {
		Graphite.getMySQL().query("DELETE FROM guilds_reports WHERE GuildId = ? AND Id = ?", guild.getID(), reportID);
	}
	
	public List<GuildChatReport> getChatReports() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT * FROM guilds_chatreports WHERE GuildId = ? ORDER BY Timestamp DESC")) {
				st.setString(1, guild.getID());
				
				try(ResultSet set = st.executeQuery()) {
					List<GuildChatReport> reports = new ArrayList<>();
					while(set.next()) {
						reports.add(new GuildChatReport(guild, set.getString("Id"), set.getString("ReporterId"), set.getString("ChannelName"), set.getLong("Timestamp")));
					}
					return reports;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load chat reports from MySQL", e));
	}
	
	public GuildChatReport createChatReport(String reporter, MessageChannel channel, List<Message> chatHistory) {
		PublicKey key = getChatReportKey();
		if(key == null) return null;
		
		SecretKey k = GraphiteCrypto.generateSymmetricKey();
		String json = new JSONArray(chatHistory.stream()
				.map(m -> new GuildChatReportMessage(m).toJSON(SerializationOption.DONT_INCLUDE_CLASS))
				.collect(Collectors.toList())).toString();
		
		byte[] encryptedKey;
		byte[] encryptedChatHistory;
		try {
			Cipher keyCipher = Cipher.getInstance("RSA");
			keyCipher.init(Cipher.ENCRYPT_MODE, key);
			encryptedKey = keyCipher.doFinal(k.getEncoded());
			
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, k);
			encryptedChatHistory = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));
		}catch(NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			throw new FriendlyException("Decryption error", e);
		}
		
		String id = UUID.randomUUID().toString();
		long timestamp = System.currentTimeMillis();
		Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("INSERT IGNORE INTO guilds_chatreports(GuildId, Id, ReporterId, ChannelName, AESKey, Messages, Timestamp) VALUES(?, ?, ?, ?, ?, ?, ?)")) {
				s.setString(1, guild.getID());
				s.setString(2, id);
				s.setString(3, reporter);
				s.setString(4, channel.getName());
				s.setBlob(5, new SerialBlob(encryptedKey));
				s.setBlob(6, new SerialBlob(encryptedChatHistory));
				s.setLong(7, timestamp);
				s.execute();
			}
		});
		return new GuildChatReport(guild, id, reporter, channel.getName(), timestamp);
	}
	
	public void removeChatReport(GuildChatReport report) {
		removeChatReportByID(report.getID());
	}
	
	public void removeChatReportByID(String reportID) {
		Graphite.getMySQL().query("DELETE FROM guilds_chatreports WHERE GuildId = ? AND Id = ?", guild.getID(), reportID);
	}
	
	public PublicKey getChatReportKey() {
		byte[] key = Graphite.getMySQL().query(byte[].class, null, "SELECT ChatReportsKey FROM guilds_reports_config WHERE GuildId = ?", guild.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load chat reports key from MySQL", e));
		if(key == null) return null;
		try {
			return GraphiteCrypto.decodePublicKey(key);
		}catch(Exception e) {
			return null;
		}
	}
	
	public void setChatReportKey(PublicKey key) {
		try {
			Graphite.getMySQL().query("INSERT INTO guilds_reports_config(GuildId, ChatReportsKey) VALUES(?, ?) ON DUPLICATE KEY UPDATE ChatReportsKey = VALUES(ChatReportsKey)", guild.getID(), new SerialBlob(key.getEncoded()))
				.orElseThrowException();
		} catch (Exception e) {
			throw new FriendlyException("Failed to set chat report key on MySQL", e);
		}
	}
	
	public void unsetChatReportKey() {
		Graphite.getMySQL().query("UPDATE guilds_reports_config SET ChatReportsKey = NULL WHERE GuildId = ?", guild.getID());
	}
	
	public boolean hasReported(GraphiteUser reporter, GraphiteUser reported) {
		return getReports().stream().anyMatch(r -> r.getReporter().equals(reporter) && r.getReported().equals(reported));
	}
	
	public GuildReport createReport(GraphiteUser reporter, GraphiteUser reported, String reason) {
		if(hasReported(reporter, reported)) throw new IllegalStateException("User was already reported by that user");
		GuildReport report = new GuildReport(UUID.randomUUID().toString(), reporter, reported, reason, System.currentTimeMillis());
		addReport(report);
		return report;
	}
	
	public GuildReport getReportByID(String id) {
		return getReports().stream().filter(r -> r.getID().equals(id)).findFirst().orElse(null);
	}
	
	public GuildChatReport getChatReportByID(String id) {
		return getChatReports().stream().filter(r -> r.getID().equals(id)).findFirst().orElse(null);
	}

}
