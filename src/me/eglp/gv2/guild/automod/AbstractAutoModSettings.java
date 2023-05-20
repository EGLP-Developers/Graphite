package me.eglp.gv2.guild.automod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.GraphiteRole;
import me.eglp.gv2.guild.GraphiteTextChannel;
import me.eglp.gv2.guild.automod.autoactions.AutoModAutoAction;
import me.eglp.gv2.guild.automod.autoactions.AutoModPunishment;
import me.eglp.gv2.guild.modlog.ModLogEntry;
import me.eglp.gv2.guild.modlog.ModLogEntryType;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONListType;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.EmbedBuilder;

public abstract class AbstractAutoModSettings implements JSONConvertible {

	@JSONValue
	@JavaScriptValue(getter = "getType")
	private String type;

	@JavaScriptValue(getter = "getFriendlyName")
	private String friendlyName;

	@JSONValue
	@JavaScriptValue(getter = "getAction", setter = "setAction")
	private AutoModAction action;

	@JSONValue
	@JSONListType(JSONType.STRING)
	@JavaScriptValue(getter = "getChannels", setter = "setChannels")
	private List<String> channels;

	@JSONValue
	@JSONListType(JSONType.STRING)
	@JavaScriptValue(getter = "getRoles", setter = "setRoles")
	private List<String> roles;

	@JSONValue
	@JavaScriptValue(getter = "isPunishable", setter = "setPunishable")
	private boolean punishable;

	public AbstractAutoModSettings(String type, String friendlyName) {
		this.type = type;
		this.friendlyName = friendlyName;
		this.action = AutoModAction.DISABLED;
		this.channels = new ArrayList<>();
		this.roles = new ArrayList<>();
		this.punishable = true;
	}

	public abstract String getWarnReason();

	public String getType() {
		return type;
	}

	public void setAction(AutoModAction action) {
		this.action = action;
	}

	public AutoModAction getAction() {
		return action;
	}

	public void setChannels(List<String> channels) {
		this.channels = channels;
	}

	public List<String> getChannels() {
		return channels;
	}

	public List<GraphiteTextChannel> getChannels(GraphiteGuild guild) {
		return channels.stream()
				.map(id -> guild.getTextChannelByID(id))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public List<String> getRoles() {
		return roles;
	}

	public List<GraphiteRole> getRoles(GraphiteGuild guild) {
		return channels.stream()
				.map(id -> guild.getRoleByID(id))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public void setPunishable(boolean punishable) {
		this.punishable = punishable;
	}

	public boolean isPunishable() {
		return punishable;
	}

	public void sendWarningMessage(GraphiteMessageChannel<?> channel, GraphiteMember member) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setAuthor(member.getName() + "#" + member.getDiscriminator(), null, member.getMember().getAvatarUrl());
		eb.setDescription("Warned! Reason: **" + getWarnReason() + "**");
		channel.sendMessage(eb.build());

		GraphiteTextChannel ch = member.getGuild().getChannelsConfig().getModLogChannel();
		if(ch != null) ch.sendMessage(eb.build());
	}

	public void addInfraction(GraphiteMember member) {
		if(punishable) {
			AutoModMySQL.addInfraction(member.getGuild().getID(), member.getID(), getType());
			member.getGuild().getModerationConfig().addModLogEntry(new ModLogEntry(ModLogEntryType.WARNING, -1, member.getID(), member.getGuild().getSelfMember().getID(), friendlyName + " (Automod)"));
		}

		AutoModPunishment punishment = null;
		long punishmentDuration = 0;
		List<Long> timestamps = AutoModMySQL.getInfractionTimestamps(member.getGuild().getID(), member.getID());
		for(AutoModAutoAction aa : member.getGuild().getAutoModSettings().getAutoActions()) {
			int count = (int) timestamps.stream()
					.filter(t -> t > System.currentTimeMillis() - aa.getTimeframe())
					.count();

			// NONBETA: correct order?
			if(count >= aa.getMinCount() && ((punishment == null || aa.getPunishment().ordinal() > punishment.ordinal()) || (punishment == aa.getPunishment() && aa.getPunishmentDuration() > punishmentDuration))) {
				punishment = aa.getPunishment();
				punishmentDuration = aa.getPunishmentDuration();
			}
		}

		GraphiteGuild guild = member.getGuild();
		if(punishment != null) {
			if(!guild.getSelfMember().canInteract(member)) return; // Don't punish members the bot can't interact with
			switch(punishment) {
				case BAN:
					guild.getJDAGuild().ban(member.getMember(), 0, TimeUnit.SECONDS).queue();
					guild.getModerationConfig().addModLogEntry(new ModLogEntry(ModLogEntryType.BAN, -1, member.getID(), guild.getSelfMember().getID(), "Automod"));
					break;
				case KICK:
					guild.getJDAGuild().kick(member.getMember()).queue();
					guild.getModerationConfig().addModLogEntry(new ModLogEntry(ModLogEntryType.KICK, -1, member.getID(), guild.getSelfMember().getID(), "Automod"));
					break;
				case CHATMUTE:
					guild.getModerationConfig().createChatMute(member, guild.getSelfMember(), "Automod");
					break;
				case TEMP_BAN:
					guild.getTemporaryActionsConfig().tempBanMember(member, punishmentDuration, guild.getSelfMember(), "Automod");
					break;
				case TEMP_CHATMUTE:
					guild.getModerationConfig().createTempChatMute(member, punishmentDuration, guild.getSelfMember(), "Automod");
					break;
			}
		}
	}

}
