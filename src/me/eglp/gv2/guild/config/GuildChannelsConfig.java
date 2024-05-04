package me.eglp.gv2.guild.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.eglp.gv2.guild.GraphiteAudioChannel;
import me.eglp.gv2.guild.GraphiteCategory;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.GraphiteTextChannel;
import me.eglp.gv2.guild.GraphiteVoiceChannel;
import me.eglp.gv2.guild.GuildAutoChannel;
import me.eglp.gv2.guild.GuildUserChannel;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.misc.FriendlyException;
import net.dv8tion.jda.api.Permission;

@SQLTable(
	name = "guilds_channels",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"ChannelId varchar(255) NOT NULL",
		"`Type` varchar(255) NOT NULL",
		"PRIMARY KEY (GuildId, ChannelId, `Type`)"
	},
	guildReference = "GuildId"
)
@SQLTable(
	name = "guilds_userchannels",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"OwnerId varchar(255) NOT NULL",
		"ChannelId varchar(255) NOT NULL",
		"PRIMARY KEY (GuildId, OwnerId)"
	},
	guildReference = "GuildId"
)
@SQLTable(
	name = "guilds_autochannels",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"CategoryId varchar(255) DEFAULT NULL",
		"ChannelId varchar(255) NOT NULL",
		"PRIMARY KEY (GuildId, ChannelId)"
	},
	guildReference = "GuildId"
)
public class GuildChannelsConfig implements IGuildConfig {

	public static final String
		CHANNEL_TYPE_AUTO_CREATED = "auto-created",
		CHANNEL_TYPE_SUPPORT_QUEUE = "support-queue",
		CHANNEL_TYPE_MOD_LOG = "mod-log",
		CHANNEL_TYPE_USERCHANNEL_CATEGORY = "userchannel-category";

	private GraphiteGuild guild;

	public GuildChannelsConfig(GraphiteGuild guild) {
		this.guild = guild;
	}

	@ChannelRemoveListener
	private void removeChannel(GraphiteGuildMessageChannel channel) {
		Graphite.getMySQL().query("DELETE FROM guilds_channels WHERE GuildId = ? AND ChannelId = ?", guild.getID(), channel.getID());
	}

	@ChannelRemoveListener
	private void removeChannel(GraphiteAudioChannel channel) {
		Graphite.getMySQL().query("DELETE FROM guilds_autochannels WHERE GuildId = ? AND ChannelId = ?", guild.getID(), channel.getID());
		Graphite.getMySQL().query("DELETE FROM guilds_userchannels WHERE GuildId = ? AND ChannelId = ?", guild.getID(), channel.getID());
	}

	@ChannelRemoveListener
	private void removeCategory(GraphiteCategory category) {
		Graphite.getMySQL().query("UPDATE guilds_autochannels SET CategoryId = ? WHERE GuildId = ? AND CategoryId = ?", null, guild.getID(), category.getID());
		if(category.getID().equals(getUserChannelCategoryID())) guild.getChannelsConfig().getUserChannels().forEach(u -> u.delete());
	}

	public GraphiteVoiceChannel getSupportQueue() {
		String id = Graphite.getMySQL().query(String.class, null, "SELECT ChannelId FROM guilds_channels WHERE GuildId = ? AND Type = ?", guild.getID(), CHANNEL_TYPE_SUPPORT_QUEUE)
					.orElseThrowOther(e -> new FriendlyException("Failed to load support queue from MySQL", e));
		if(id == null) return null;
		return guild.getVoiceChannelByID(id);
	}

	public void setSupportQueue(GraphiteVoiceChannel channel) {
		unsetSupportQueue();
		if(channel == null) return;
		Graphite.getMySQL().query("INSERT IGNORE INTO guilds_channels(GuildId, ChannelId, Type) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE ChannelId = VALUES(ChannelId)", guild.getID(), channel.getID(), CHANNEL_TYPE_SUPPORT_QUEUE);
	}

	public void unsetSupportQueue() {
		Graphite.getMySQL().query("DELETE FROM guilds_channels WHERE GuildId = ? AND Type = ?", guild.getID(), CHANNEL_TYPE_SUPPORT_QUEUE);
	}

	public List<GuildAutoChannel> getAutoChannels() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT ChannelId, CategoryId FROM guilds_autochannels WHERE GuildId = ?")) {
				st.setString(1, guild.getID());

				try(ResultSet s = st.executeQuery()) {
					List<GuildAutoChannel> autochannels = new ArrayList<>();
					while(s.next()) {
						GuildAutoChannel ch = loadAutoChannel(s);
						if(ch == null) continue;
						autochannels.add(ch);
					}

					return autochannels;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load autochannels from MySQL", e));
	}

	private GuildAutoChannel loadAutoChannel(ResultSet s) throws SQLException {
		GraphiteVoiceChannel channel = guild.getVoiceChannelByID(s.getString("ChannelId"));
		if(channel == null) return null;

		String cID = s.getString("CategoryId");
		GraphiteCategory category = cID == null ? null : guild.getCategoryByID(cID);

		return new GuildAutoChannel(channel, category);
	}

	public void setAutoChannels(List<GuildAutoChannel> autoChannels) {
		removeAllAutoChannels();
		Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("INSERT INTO guilds_autochannels(GuildId, ChannelId, CategoryId) VALUES(?, ?, ?)")) {
				for(GuildAutoChannel a : autoChannels) {
					s.setString(1, guild.getID());
					s.setString(2, a.getChannel().getID());
					s.setString(3, a.getCategory() == null ? null : a.getCategory().getID());
					s.addBatch();
				}

				s.executeBatch();
			}
		});
	}

	private void removeAllAutoChannels() {
		Graphite.getMySQL().query("DELETE FROM guilds_autochannels WHERE GuildId = ?", guild.getID());
	}

	public GuildAutoChannel createAutoChannel(GraphiteVoiceChannel channel, GraphiteCategory category) {
		GuildAutoChannel a = new GuildAutoChannel(channel, category);
		addAutoChannel(a);
		return a;
	}

	private void addAutoChannel(GuildAutoChannel channel) {
		Graphite.getMySQL().query("INSERT IGNORE INTO guilds_autochannels(GuildId, ChannelId, CategoryId) VALUES(?, ?, ?)",
				guild.getID(),
				channel.getChannel().getID(),
				Optional.ofNullable(channel.getCategory()).map(c -> c.getID()).orElse(null));
	}

	public void removeAutoChannel(GuildAutoChannel channel) {
		removeAutoChannel(channel.getChannel());
	}

	public void removeAutoChannel(GraphiteVoiceChannel channel) {
		Graphite.getMySQL().query("DELETE FROM guilds_autochannels WHERE GuildId = ? AND ChannelId = ?", guild.getID(), channel.getID());
	}

	public GuildAutoChannel getAutoChannelByID(String id) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT ChannelId, CategoryId FROM guilds_autochannels WHERE GuildId = ? AND ChannelId = ?")) {
				st.setString(1, guild.getID());
				st.setString(2, id);

				try(ResultSet s = st.executeQuery()) {
					if(!s.next()) return null;
					GuildAutoChannel ch = loadAutoChannel(s);
					return ch;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load autochannel from MySQL", e));
	}

	public boolean isAutoChannel(GraphiteVoiceChannel channel) {
		return getAutoChannelByID(channel.getID()) != null;
	}

	public void addAutoCreatedChannel(GraphiteVoiceChannel channel) {
		Graphite.getMySQL().query("INSERT INTO guilds_channels(GuildId, ChannelId, Type) VALUES(?, ?, ?)", guild.getID(), channel.getID(), CHANNEL_TYPE_AUTO_CREATED);
	}

	public void removeAutoCreatedChannel(GraphiteVoiceChannel channel) {
		Graphite.getMySQL().query("DELETE FROM guilds_channels WHERE GuildId = ? AND ChannelId = ? AND Type = ?", guild.getID(), channel.getID(), CHANNEL_TYPE_AUTO_CREATED);
	}

	public boolean isAutoCreatedChannel(GraphiteVoiceChannel channel) {
		return Graphite.getMySQL().query(String.class, null, "SELECT ChannelId FROM guilds_channels WHERE GuildId = ? AND ChannelId = ? AND Type = ?", guild.getID(), channel.getID(), CHANNEL_TYPE_AUTO_CREATED)
				.orElseThrowOther(e -> new FriendlyException("Failed to load auto created channel from MySQL", e)) != null;
	}

	public void setAutoChannelCategory(GuildAutoChannel channel, GraphiteCategory category) {
		Graphite.getMySQL().query("UPDATE guilds_autochannels SET CategoryId = ? WHERE GuildId = ? AND ChannelId = ?", category == null ? null : category.getID(), guild.getID(), channel.getChannel().getID());
	}

	public List<GuildUserChannel> getUserChannels() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement st = con.prepareStatement("SELECT ChannelId, OwnerId FROM guilds_userchannels WHERE GuildId = ?")) {
				st.setString(1, guild.getID());

				try(ResultSet s = st.executeQuery()) {
					List<GuildUserChannel> mutes = new ArrayList<>();
					while(s.next()) {
						GraphiteMember m = guild.getMember(s.getString("OwnerId"));
						if(m == null) continue;
						GraphiteVoiceChannel c = guild.getVoiceChannelByID(s.getString("ChannelId"));
						if(c == null) continue;
						mutes.add(new GuildUserChannel(guild, m, c));
					}

					return mutes;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load user channels from MySQL", e));
	}

	private void addUserChannel(GuildUserChannel channel) {
		Graphite.getMySQL().query("INSERT IGNORE INTO guilds_userchannels(GuildId, OwnerId, ChannelId) VALUES(?, ?, ?)", guild.getID(), channel.getOwner().getID(), channel.getChannel().getID());
	}

	private void removeUserChannel(GuildUserChannel channel) {
		Graphite.getMySQL().query("DELETE FROM guilds_userchannels WHERE GuildId = ? AND OwnerId = ?", guild.getID(), channel.getOwner().getID());
	}

	public String getUserChannelCategoryID() {
		return Graphite.getMySQL().query(String.class, null, "SELECT ChannelId FROM guilds_channels WHERE GuildId = ? AND TYPE = ?", guild.getID(), CHANNEL_TYPE_USERCHANNEL_CATEGORY)
				.orElseThrowOther(e -> new FriendlyException("Failed to load userchannel category from MySQL", e));
	}

	public GraphiteCategory getUserChannelCategory() {
		String id = getUserChannelCategoryID();
		if(id == null) return null;
		return guild.getCategoryByID(id);
	}

	public void setUserChannelCategory(GraphiteCategory category) {
		Graphite.getMySQL().query("INSERT IGNORE INTO guilds_channels(GuildId, ChannelId, Type) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE ChannelId = VALUES(ChannelId)", guild.getID(), category.getID(), CHANNEL_TYPE_USERCHANNEL_CATEGORY);
	}

	public GuildUserChannel getUserChannelByOwner(GraphiteMember owner) {
		return getUserChannels().stream().filter(c -> c.getOwner().equals(owner)).findFirst().orElse(null);
	}

	public GuildUserChannel getUserChannelByChannel(GraphiteVoiceChannel channel) {
		return getUserChannels().stream().filter(c -> c.getChannel().equals(channel)).findFirst().orElse(null);
	}

	public boolean isUserChannel(GraphiteVoiceChannel channel) {
		return getUserChannelByChannel(channel) != null;
	}

	public GuildUserChannel createUserChannel(GraphiteMember owner) {
		GraphiteCategory cat = getUserChannelCategory();
		if(cat == null) {
			cat = guild.getCategory(guild.getJDAGuild().createCategory("User Channels").complete());
			setUserChannelCategory(cat);
		}

		GraphiteVoiceChannel vc = guild.getVoiceChannel(guild.getJDAGuild().createVoiceChannel(owner.getName() + "'s channel").setParent(cat.getJDACategory()).complete());
		vc.getJDAChannel().upsertPermissionOverride(owner.getMember()).complete().getManager().grant(Permission.ALL_CHANNEL_PERMISSIONS).queue();
		GuildUserChannel uc = new GuildUserChannel(guild, owner, vc);
		addUserChannel(uc);
		return uc;
	}

	public void deleteUserChannel(GuildUserChannel channel) {
		removeUserChannel(channel);
		if(channel.getChannel() != null && channel.getChannel().getJDAChannel() != null) channel.getChannel().getJDAChannel().delete().complete();
		if(getUserChannels().isEmpty()) {
			GraphiteCategory cat = getUserChannelCategory();
			if(cat != null) cat.getJDACategory().delete().queue(null, e -> {

			});
			Graphite.getMySQL().query("DELETE FROM guilds_channels WHERE GuildId = ? AND Type = ?", guild.getID(), CHANNEL_TYPE_USERCHANNEL_CATEGORY);
		}
	}

	public void setModLogChannel(GraphiteTextChannel channel) {
		if(channel == null) {
			unsetModLogChannel();
			return;
		}

		Graphite.getMySQL().query("INSERT INTO guilds_channels(GuildId, ChannelId, Type) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE ChannelId = VALUES(ChannelId)", guild.getID(), channel.getID(), CHANNEL_TYPE_MOD_LOG);
	}

	public void unsetModLogChannel() {
		Graphite.getMySQL().query("DELETE FROM guilds_channels WHERE GuildId = ? AND Type = ?", guild.getID(), CHANNEL_TYPE_MOD_LOG);
	}

	public GraphiteTextChannel getModLogChannel() {
		String id = Graphite.getMySQL().query(String.class, null, "SELECT ChannelId FROM guilds_channels WHERE GuildId = ? AND Type = ?", guild.getID(), CHANNEL_TYPE_MOD_LOG)
				.orElseThrowOther(e -> new FriendlyException("Failed to load mod log channel from MySQL", e));
		if(id == null) return null;
		return guild.getTextChannelByID(id);
	}

}
