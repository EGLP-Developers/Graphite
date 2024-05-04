package me.eglp.gv2.guild;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.automod.GuildAutoModSettings;
import me.eglp.gv2.guild.config.GuildBackupConfig;
import me.eglp.gv2.guild.config.GuildChannelsConfig;
import me.eglp.gv2.guild.config.GuildConfig;
import me.eglp.gv2.guild.config.GuildCustomCommandsConfig;
import me.eglp.gv2.guild.config.GuildGreeterConfig;
import me.eglp.gv2.guild.config.GuildModerationConfig;
import me.eglp.gv2.guild.config.GuildPollsConfig;
import me.eglp.gv2.guild.config.GuildRecordingsConfig;
import me.eglp.gv2.guild.config.GuildRedditConfig;
import me.eglp.gv2.guild.config.GuildRemindersConfig;
import me.eglp.gv2.guild.config.GuildReportsConfig;
import me.eglp.gv2.guild.config.GuildRolesConfig;
import me.eglp.gv2.guild.config.GuildStatisticsConfig;
import me.eglp.gv2.guild.config.GuildTemporaryActionsConfig;
import me.eglp.gv2.guild.config.GuildTwitchConfig;
import me.eglp.gv2.guild.music.GuildMusic;
import me.eglp.gv2.guild.recorder.GuildRecorder;
import me.eglp.gv2.guild.scripting.GuildScripts;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.LoadTimes;
import me.eglp.gv2.util.backup.GuildBackup;
import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.base.GraphiteMusical;
import me.eglp.gv2.util.base.GraphiteOwning;
import me.eglp.gv2.util.command.CommandSender;
import me.eglp.gv2.util.lang.GuildLocale;
import me.eglp.gv2.util.permission.GuildPermissionManager;
import me.eglp.gv2.util.queue.GraphiteQueue;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;

@JavaScriptClass(name = "Guild")
public class GraphiteGuild implements GraphiteLocalizable, CommandSender, GraphiteOwning, GraphiteMusical, WebinterfaceObject, GuildChannels {

	private GuildChannelsConfig channelsConfig;
	private GuildConfig config;
	private GuildReportsConfig reportsConfig;
	private GuildRolesConfig rolesConfig;
	private GuildTemporaryActionsConfig temporaryActionsConfig;
	private GuildGreeterConfig greeterConfig;
	private GuildModerationConfig moderationConfig;
	private GuildBackupConfig backupConfig;
	private GuildRecordingsConfig recordingsConfig;
	private GuildCustomCommandsConfig customCommandsConfig;
	private GuildStatisticsConfig statisticsConfig;
	private GuildTwitchConfig twitchConfig;
	private GuildRedditConfig redditConfig;
	private GuildPollsConfig pollsConfig;
	private GuildRemindersConfig remindersConfig;

	private String guildID;
	private Guild jdaGuild;
	private GuildLocale locale;
	private GuildPermissionManager permissionManager;
	private GuildMusic music;
	private GuildRecorder recorder;
	private GuildScripts scripts;
	private GuildAutoModSettings autoModSettings;

	private long lastBackup;

	private List<GraphiteMember> cachedMembers;
	private List<GraphiteRole> cachedRoles;
	private List<GraphiteGuildChannel> cachedChannels;
	private List<GraphiteCategory> cachedCategories;

	public GraphiteGuild(Guild guild) {
		guildID = guild.getId();
		this.jdaGuild = guild;

		this.cachedMembers = Collections.synchronizedList(new ArrayList<>());
		this.cachedRoles = Collections.synchronizedList(new ArrayList<>());
		this.cachedChannels = Collections.synchronizedList(new ArrayList<>());
		this.cachedCategories = Collections.synchronizedList(new ArrayList<>());
	}

	public LoadTimes load() {
		LoadTimes times = new LoadTimes();
		times.startTiming();

		this.channelsConfig = new GuildChannelsConfig(this);
		times.stopTimingAndRestart("channels");

		this.config = new GuildConfig(this);
		times.stopTimingAndRestart("config");

		this.reportsConfig = new GuildReportsConfig(this);
		times.stopTimingAndRestart("reports");

		this.rolesConfig = new GuildRolesConfig(this);
		times.stopTimingAndRestart("roles");

		this.temporaryActionsConfig = new GuildTemporaryActionsConfig(this);
		times.stopTimingAndRestart("temporary");

		this.greeterConfig = new GuildGreeterConfig(this);
		times.stopTimingAndRestart("greeter");

		this.moderationConfig = new GuildModerationConfig(this);
		times.stopTimingAndRestart("moderation");

		this.backupConfig = new GuildBackupConfig(this);
		times.stopTimingAndRestart("backup");

		this.recordingsConfig = new GuildRecordingsConfig(this);
		times.stopTimingAndRestart("recordings");

		this.customCommandsConfig = new GuildCustomCommandsConfig(this);
		times.stopTimingAndRestart("customCommands");

		this.customCommandsConfig.updateSlashCommands();
		times.stopTimingAndRestart("customCommands (update)");

		this.statisticsConfig = new GuildStatisticsConfig(this);
		times.stopTimingAndRestart("statistics");

		this.locale = new GuildLocale(this);
		times.stopTimingAndRestart("locale");

		this.twitchConfig = new GuildTwitchConfig(this);
		times.stopTimingAndRestart("twitch");

		this.redditConfig = new GuildRedditConfig(this);
		times.stopTimingAndRestart("reddit");

		this.pollsConfig = new GuildPollsConfig(this);
		this.pollsConfig.init();
		times.stopTimingAndRestart("polls");

		this.remindersConfig = new GuildRemindersConfig(this);
		this.remindersConfig.init();
		times.stopTimingAndRestart("reminders");

		this.permissionManager = new GuildPermissionManager(this);
		times.stopTimingAndRestart("permission");

		this.autoModSettings = new GuildAutoModSettings(this);
		times.stopTimingAndRestart("autoMod");

		this.music = new GuildMusic(this);
		times.stopTimingAndRestart("music");

		this.recorder = new GuildRecorder(this);
		times.stopTimingAndRestart("recorder");

		this.scripts = new GuildScripts(this);
		times.stopTimingAndRestart("scripts");

		return times;
	}

	public synchronized List<GraphiteCategory> getCachedCategories() {
		return cachedCategories;
	}

	public synchronized List<GraphiteRole> getCachedRoles() {
		return cachedRoles;
	}

	public synchronized List<GraphiteGuildChannel> getCachedChannels() {
		return cachedChannels;
	}

	public boolean hasPermissions(Permission... permissions) {
		return getJDAGuild().getSelfMember().hasPermission(permissions);
	}

	public boolean hasPermissions(Collection<Permission> permissions) {
		return getJDAGuild().getSelfMember().hasPermission(permissions);
	}

	@Override
	public Guild getJDAGuild() {
		return jdaGuild;
	}

	@JavaScriptGetter(name = "getName", returning = "name")
	public String getName() {
		return getJDAGuild().getName();
	}

	public GraphiteMember getMember(GraphiteUser user) {
		if(user == null) throw new IllegalArgumentException("user == null");
		return getMember(getJDAGuild().retrieveMember(user.getJDAUser()).onErrorMap(t -> null).complete());
	}

	public GraphiteMember getMember(String id) {
		if(id == null) throw new IllegalArgumentException("id == null");
		return getMember(getJDAGuild().retrieveMemberById(id).onErrorMap(t -> null).complete());
	}

	private synchronized GraphiteMember getMemberRaw(Member member) {
		if(member == null) return null;
		return new ArrayList<>(cachedMembers).stream()
				.filter(c -> c.getMember().equals(member))
				.findFirst().orElse(null);
	}

	public GraphiteMember getMember(Member member) {
		if(member == null) return null;
		if(!jdaGuild.equals(member.getGuild())) return null;
		GraphiteMember m = getMemberRaw(member);
		if(m == null) {
			m = new GraphiteMember(member, this);
			cachedMembers.add(m);
		}
		return m;
	}

	public List<GraphiteMember> getMembers() {
		return getJDAGuild().loadMembers().get().stream().map(m -> getMember(m)).collect(Collectors.toList());
	}

	public List<GraphiteMember> getMembersWithAllRoles(GraphiteRole... roles) {
		return getMembersWithAllRoles(Arrays.asList(roles));
	}

	public List<GraphiteMember> getMembersWithAllRoles(List<GraphiteRole> roles) {
		return getJDAGuild().findMembersWithRoles(roles.stream().map(GraphiteRole::getJDARole).collect(Collectors.toList())).get().stream().map(this::getMember).collect(Collectors.toList());
	}

	public List<GraphiteMember> getMembersWithAnyRole(List<GraphiteRole> roles) {
		List<Role> jdaRoles = roles.stream().map(r -> r.getJDARole()).collect(Collectors.toList());
		return getJDAGuild().findMembers(m -> m.getRoles().stream().anyMatch(jdaRoles::contains)).get().stream().map(this::getMember).collect(Collectors.toList());
	}

	public void updateMember(GraphiteMember member, boolean mute, boolean deafen) {
		DataObject body = DataObject.empty().put("deaf", deafen).put("mute", mute);
		Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(getID(), member.getID());
		new AuditableRestActionImpl<>(member.getMember().getJDA(), route, body).queue();
	}

	public GraphiteRole getRoleByID(String id) {
		return getRole(getJDAGuild().getRoleById(id)); // NONBETA: refactor?
	}

	private synchronized GraphiteRole getRoleRaw(Role role) {
		return new ArrayList<>(getCachedRoles()).stream().filter(c -> c.getID().equals(role.getId())).findFirst().orElse(null);
	}

	public GraphiteRole getRole(Role role) {
		if(role == null) return null;
		GraphiteRole r = getRoleRaw(role);
		if(r == null) {
			r = new GraphiteRole(this, role.getId());
			cachedRoles.add(r);
		}
		return r;
	}

	public GraphiteRole getPublicRole() {
		return getRole(getJDAGuild().getPublicRole());
	}

	public List<GraphiteRole> getRoles() {
		return getJDAGuild().getRoles().stream().map(m -> getRole(m)).collect(Collectors.toList());
	}

	private synchronized GraphiteGuildChannel getGuildChannelRaw(GuildChannel channel) {
		return new ArrayList<>(getCachedChannels()).stream().filter(c -> c.getID().equals(channel.getId())).findFirst().orElse(null);
	}

	@Override
	public GraphiteGuildChannel getGuildChannel(GuildChannel channel) {
		if(channel == null) return null;
		GraphiteGuildChannel r = getGuildChannelRaw(channel);
		if(r == null) {
			switch(channel.getType()) {
				case TEXT:
					r = new GraphiteTextChannel(this, channel.getId());
					break;
				case CATEGORY:
					r = new GraphiteCategory(this, channel.getId());
					break;
				case NEWS:
					r = new GraphiteNewsChannel(this, channel.getId());
					break;
				case VOICE:
					r = new GraphiteVoiceChannel(this, channel.getId());
					break;
				case STAGE:
					r = new GraphiteStageChannel(this, channel.getId());
					break;
				case FORUM:
					r = new GraphiteForumChannel(this, channel.getId());
					break;
				case GUILD_NEWS_THREAD:
				case GUILD_PUBLIC_THREAD:
				case GUILD_PRIVATE_THREAD:
					r = new GraphiteThreadChannel(this, channel.getId());
					break;
				default:
					throw new IllegalArgumentException("Unknown guild channel type: " + channel.getType());
			}
		}
		cachedChannels.add(r);
		return r;
	}

	@Override
	@JavaScriptGetter(name = "getID", returning = "id")
	public String getID() {
		return guildID;
	}

	@JavaScriptGetter(name = "getIconURL", returning = "iconURL")
	public String getIconURL() {
		return getJDAGuild().getIconUrl();
	}

	@Override
	public GuildLocale getLocale() {
		return locale;
	}

	public boolean hasLocale(String locale) {
		return this.locale.hasLocale(locale);
	}

	public boolean hasCustomLocale(String locale) {
		return this.locale.hasCustomLocale(locale);
	}

	public GuildConfig getConfig() {
		return config;
	}

	public GuildReportsConfig getReportsConfig() {
		return reportsConfig;
	}

	public GuildChannelsConfig getChannelsConfig() {
		return channelsConfig;
	}

	public GuildTemporaryActionsConfig getTemporaryActionsConfig() {
		return temporaryActionsConfig;
	}

	public GuildRolesConfig getRolesConfig() {
		return rolesConfig;
	}

	public GuildBackupConfig getBackupConfig() {
		return backupConfig;
	}

	public GuildRecordingsConfig getRecordingsConfig() {
		return recordingsConfig;
	}

	public GuildGreeterConfig getGreeterConfig() {
		return greeterConfig;
	}

	public GuildModerationConfig getModerationConfig() {
		return moderationConfig;
	}

	public GuildCustomCommandsConfig getCustomCommandsConfig() {
		return customCommandsConfig;
	}

	public GuildStatisticsConfig getStatisticsConfig() {
		return statisticsConfig;
	}

	public GuildTwitchConfig getTwitchConfig() {
		return twitchConfig;
	}

	public GuildRedditConfig getRedditConfig() {
		return redditConfig;
	}

	public GuildAutoModSettings getAutoModSettings() {
		return autoModSettings;
	}

	public GuildPollsConfig getPollsConfig() {
		return pollsConfig;
	}

	public GuildRemindersConfig getRemindersConfig() {
		return remindersConfig;
	}

	public GuildPermissionManager getPermissionManager() {
		return permissionManager;
	}

	public GuildBackup createBackup(PublicKey encryptionKey, int messageCount, boolean autoBackup) {
		return GuildBackup.createNew(this, encryptionKey, messageCount, autoBackup);
	}

	public long getBackupCooldown() {
		return Math.max(0, GuildBackup.BACKUP_COOLDOWN - (System.currentTimeMillis() - lastBackup));
	}

	public boolean canCreateBackup() {
		return true; //TODO War mal Premium maybe change
	}

	public boolean canCreateAutoBackup() {
		return true; //TODO War mal Premium maybe change
	}

	public void deleteLastAutoBackup() {
		GuildBackup.deleteLastAutoBackup(this);
	}

	public GuildBackup getBackupByName(String name) {
		return GuildBackup.getBackupByName(this, name);
	}

	public List<GuildBackup> getBackups() {
		return GuildBackup.getBackups(this);
	}

	public void discardRole(Role role) {
		GraphiteRole r = getRoleRaw(role);
		if(r == null) return;
		discardRole(r);
	}

	private synchronized void discardRole(GraphiteRole r) {
		if(r == null) return;
		rolesConfig.removeRole(r.getID());
		permissionManager.discardRole(r.getID());
		cachedRoles.remove(r);
	}

	public void discardChannel(GuildChannel channel) {
		GraphiteGuildChannel ch = getGuildChannelRaw(channel);
		if(ch == null) return;
		discardChannel(ch);
	}

	public void discardChannel(GraphiteGuildChannel channel) {
		// NONBETA: always call discardChannel even if channel is not cached
		if(channel == null) return;
		channelsConfig.discardChannel(channel);
		redditConfig.discardChannel(channel);
		twitchConfig.discardChannel(channel);
		greeterConfig.discardChannel(channel);
		moderationConfig.discardChannel(channel);
		cachedChannels.remove(channel);
	}

	public void discardMember(Member member) {
		discardMemberByID(member.getId());
	}

	public synchronized void discardMemberByID(String id) {
		cachedMembers.removeIf(m -> m.getID().equals(id));
		permissionManager.discardMember(id);
	}

	public GraphiteQueue getResponsibleQueue() {
		return Graphite.getQueue();
	}

	public boolean isQueueBusy() {
		return getResponsibleQueue().isBusy(this);
	}

	public GraphiteMember getSelfMember() {
		return getMember(getJDAGuild().getSelfMember());
	}

	public GraphiteRole getSelfRole() {
		return getRole(getJDAGuild().getBotRole());
	}

	public boolean isAboveUserRoles() {
		List<GraphiteRole> roles = getRoles();
		List<GraphiteRole> gr = roles.subList(0, roles.indexOf(getSelfRole()));
		return gr.stream().noneMatch(r -> !r.isManaged());
	}

	public boolean isBanned(String userID) {
		try {
			return getJDAGuild().retrieveBan(UserSnowflake.fromId(userID)).complete() != null;
		}catch(ErrorResponseException e) {
			if(e.getErrorResponse() == ErrorResponse.UNKNOWN_BAN) return false;
			throw e;
		}
	}

	public void addRoleToMember(GraphiteMember member, GraphiteRole role) {
		getJDAGuild().addRoleToMember(member.getMember(), role.getJDARole()).queue();
	}

	public void removeRoleFromMember(GraphiteMember member, GraphiteRole role) {
		getJDAGuild().removeRoleFromMember(member.getMember(), role.getJDARole()).queue();
	}

	@Override
	public GuildMusic getMusic() {
		return music;
	}

	public GuildRecorder getRecorder() {
		return recorder;
	}

	public GuildScripts getScripts() {
		return scripts;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof GraphiteGuild)) return false;
		return getID().equals(((GraphiteGuild)o).getID());
	}

	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("id", getID());
		object.put("name", getName());
		object.put("iconURL", getIconURL());
	}

	@JavaScriptFunction(calling = "getSelectedGuild", returning = "guild", withGuild = true)
	public static void getSelectedGuild() {};

}
