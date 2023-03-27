package me.eglp.gv2.util.base.guild.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.ContextHandle;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.mysql.SQLTable;
import me.eglp.gv2.util.stats.element.GuildStatisticsElement;
import me.eglp.gv2.util.stats.element.StatisticsElementSettings;
import me.eglp.gv2.util.stats.element.StatisticsElementType;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.SerializationOption;
import me.mrletsplay.mrcore.misc.FriendlyException;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

@SQLTable(
	name = "guilds_statistics_elements",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"`Id` varchar(255) NOT NULL",
		"`Type` varchar(255) NOT NULL",
		"Settings text NOT NULL",
		"ChannelId varchar(255) DEFAULT NULL",
		"MessageId varchar(255) DEFAULT NULL",
		"PRIMARY KEY (GuildId, `Id`)"
	},
	guildReference = "GuildId"
)
public class GuildStatisticsConfig implements IGuildConfig {
	
	private GraphiteGuild guild;
	
	public GuildStatisticsConfig(GraphiteGuild guild) {
		this.guild = guild;
	}
	
	public GraphiteGuild getGuild() {
		return guild;
	}
	
	@ChannelRemoveListener
	public void removeChannel(GraphiteGuildMessageChannel channel) {
		Graphite.getMySQL().query("DELETE FROM guilds_statistics_elements WHERE GuildId = ? AND ChannelId = ?", guild.getID(), channel.getID());
	}
	
	public GuildStatisticsElement createStatisticsElement(StatisticsElementType type, StatisticsElementSettings settings) {
		String id = UUID.randomUUID().toString();
		
		Graphite.getMySQL().query("INSERT INTO guilds_statistics_elements(GuildId, Id, Type, Settings) VALUES(?, ?, ?, ?)", guild.getID(), id, type.name(), settings.toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString());
		return new GuildStatisticsElement(guild, id, type, settings, null, null);
	}
	
	public void updateStatisticsElementSettings(String elementID, StatisticsElementSettings settings) {
		Graphite.getMySQL().query("UPDATE guilds_statistics_elements SET Settings = ? WHERE GuildId = ? AND `Id` = ?", settings.toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString(), guild.getID(), elementID);
	}
	
	public List<GuildStatisticsElement> getStatisticsElements() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_statistics_elements WHERE GuildId = ?")) {
				s.setString(1, guild.getID());
				try(ResultSet r = s.executeQuery()) {
					List<GuildStatisticsElement> e = new ArrayList<>();
					while(r.next()) {
						e.add(loadElement(r));
					}
					return e;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load statistics elements from MySQL", e));
	}
	
	public GuildStatisticsElement getStatisticsElementByID(String id) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_statistics_elements WHERE GuildId = ? AND `Id` = ?")) {
				s.setString(1, guild.getID());
				s.setString(2, id);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return null;
					return loadElement(r);
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load statistics element from MySQL", e));
	}
	
	private GuildStatisticsElement loadElement(ResultSet r) throws SQLException {
		var type = StatisticsElementType.valueOf(r.getString("Type"));
		return new GuildStatisticsElement(guild, r.getString("Id"), type, JSONConverter.decodeObject(new JSONObject(r.getString("Settings")), type.getSettingsType()), r.getString("ChannelID"), r.getString("MessageID"));
	}
	
	public void setMessageID(String elementID, String channelID, String messageID) {
		Graphite.getMySQL().query("UPDATE guilds_statistics_elements SET ChannelId = ?, MessageId = ? WHERE `Id` = ?", channelID, messageID, elementID);
	}
	
	public String getChannelID(String elementID) {
		return Graphite.getMySQL().query(String.class, null, "SELECT ChannelId FROM guilds_statistics_elements WHERE `Id` = ?", elementID)
				.orElseThrowOther(e -> new FriendlyException("Failed to load channel id for statistics element from MySQL", e));
	}
	
	public void updateMessageIfExists(String elementID, Supplier<String> newContent) {
		ContextHandle handle = GraphiteMultiplex.handle();
		Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT ChannelId, MessageId FROM guilds_statistics_elements WHERE `Id` = ?")) {
				s.setString(1, elementID);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return;
					String channelID = r.getString("ChannelId");
					String messageID = r.getString("MessageId");
					if(channelID == null || messageID == null) {
						removeStatisticsElement(elementID);
						return;
					}
					handle.reset();
					GraphiteTextChannel ch = guild.getTextChannelByID(channelID);
					if(ch == null) {
						removeStatisticsElement(elementID);
						return;
					}
					TextChannel tc = ch.getJDAChannel();
					tc.editMessageById(messageID, newContent.get()).queue(null, new ErrorHandler()
							.handle(Arrays.asList(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.UNKNOWN_CHANNEL), e -> removeStatisticsElement(elementID)));
				}
			}
		});
	}
	
	public void deleteOldMessage(String elementID) {
		ContextHandle handle = GraphiteMultiplex.handle();
		Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT ChannelId, MessageId FROM guilds_statistics_elements WHERE `Id` = ?")) {
				s.setString(1, elementID);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return;
					String channelID = r.getString("ChannelId");
					String messageID = r.getString("MessageId");
					if(channelID == null || messageID == null) return;
					handle.reset();
					TextChannel tc = guild.getTextChannelByID(channelID).getJDAChannel();
					tc.deleteMessageById(messageID).queue(null, new ErrorHandler()
							.handle(Arrays.asList(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.UNKNOWN_CHANNEL), e -> removeStatisticsElement(elementID)));
				}
			}
		});
	}
	
	public void removeStatisticsElement(String elementID) {
		Graphite.getMySQL().query("DELETE FROM guilds_statistics_elements WHERE GuildId = ? AND `Id` = ?", guild.getID(), elementID);
	}
	
	public void removeAllStatisticsElements() {
		Graphite.getMySQL().query("DELETE FROM guilds_statistics_elements WHERE GuildId = ?", guild.getID());
	}
	
}
