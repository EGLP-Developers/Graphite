package me.eglp.gv2.util.backup.data.channels;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteTextChannel;
import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.backup.data.channels.BackupPermissionOverride.Type;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
public class BackupTextChannel implements JSONConvertible, WebinterfaceObject, BackupChannel {

	@JavaScriptValue(getter = "getID")
	@JSONValue
	private String id;

	@JavaScriptValue(getter = "getName")
	@JSONValue
	private String name;

	@JavaScriptValue(getter = "getPosition")
	@JSONValue
	private int position = -1;

	@JavaScriptValue(getter = "getTopic")
	@JSONValue
	private String topic;

	@JavaScriptValue(getter = "isNSFW")
	@JSONValue
	private boolean nsfw;

	@JavaScriptValue(getter = "getSlowmode")
	@JSONValue
	private int slowmode;

	@JSONValue
	@JSONComplexListType(BackupPermissionOverride.class)
	private List<BackupPermissionOverride> permissionOverrides;

	@JSONConstructor
	private BackupTextChannel() {}

	public BackupTextChannel(GraphiteTextChannel graphiteChannel) {
		TextChannel ch = graphiteChannel.getJDAChannel();
		if(ch == null) throw new IllegalStateException("Unknown channel or invalid context");

		this.id = graphiteChannel.getID();
		this.name = ch.getName();
		this.position = ch.getPosition();
		this.topic = ch.getTopic();
		this.nsfw = ch.isNSFW();
		this.slowmode = ch.getSlowmode();
		this.permissionOverrides = new ArrayList<>(ch.getPermissionOverrides().stream().map(BackupPermissionOverride::new).collect(Collectors.toList()));
	}

	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public int getPosition() {
		return position;
	}

	public String getTopic() {
		return topic;
	}

	public boolean isNsfw() {
		return nsfw;
	}

	public int getSlowmode() {
		return slowmode;
	}

	public List<BackupPermissionOverride> getPermissionOverrides() {
		return permissionOverrides;
	}

	@Override
	public void restore(GraphiteGuild guild, Category parent, IDMappings mappings) {
		ChannelAction<TextChannel> c = guild.getJDAGuild().createTextChannel(name, parent);
		if(position >= 0) c.setPosition(position);
		c.setTopic(topic);
		c.setNSFW(nsfw);
		c.setSlowmode(slowmode);

		permissionOverrides.stream()
			.filter(o -> o.getType() == Type.MEMBER)
			.forEach(o -> c.addMemberPermissionOverride(Long.parseLong(o.getID()), o.getAllowed(), o.getDenied()));

		permissionOverrides.stream()
			.filter(o -> o.getType() == Type.ROLE)
			.forEach(o -> {
				String newRoleID = mappings.getNewID(o.getID());
				if(newRoleID == null) return; // Because of the role rate limit, the role might not have been restored
				c.addRolePermissionOverride(Long.parseLong(newRoleID), o.getAllowed(), o.getDenied());
			});

		mappings.put(id, c.complete().getId());
	}

}
