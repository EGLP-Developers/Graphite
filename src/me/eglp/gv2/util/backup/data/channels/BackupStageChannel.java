package me.eglp.gv2.util.backup.data.channels;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.backup.data.channels.BackupPermissionOverride.Type;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteStageChannel;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

public class BackupStageChannel implements JSONConvertible, WebinterfaceObject, BackupChannel {

	@JavaScriptValue(getter = "getID")
	@JSONValue
	private String id;

	@JavaScriptValue(getter = "getName")
	@JSONValue
	private String name;

	@JavaScriptValue(getter = "getPosition")
	@JSONValue
	private int position = -1;

	@JavaScriptValue(getter = "getBitrate")
	@JSONValue
	private int bitrate;

	@JavaScriptValue(getter = "getRegion")
	@JSONValue
	private String region;

	@JSONValue
	@JSONComplexListType(BackupPermissionOverride.class)
	private List<BackupPermissionOverride> permissionOverrides;

	@JSONConstructor
	private BackupStageChannel() {}

	public BackupStageChannel(GraphiteStageChannel graphiteChannel) {
		StageChannel ch = graphiteChannel.getJDAChannel();
		if(ch == null) throw new IllegalStateException("Unknown channel or invalid context");

		this.id = graphiteChannel.getID();
		this.name = ch.getName();
		this.position = ch.getPosition();
		this.bitrate = ch.getBitrate();
		this.region = ch.getRegionRaw();
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

	public int getBitrate() {
		return bitrate;
	}

	public String getRegion() {
		return region;
	}

	public List<BackupPermissionOverride> getPermissionOverrides() {
		return permissionOverrides;
	}

	@Override
	public void restore(GraphiteGuild guild, Category parent, IDMappings mappings) {
		Guild g = guild.getJDAGuild();
		if(!g.getFeatures().contains("COMMUNITY")) return;

		ChannelAction<StageChannel> c = g.createStageChannel(name, parent);
		if(position >= 0) c.setPosition(position);
		c.setBitrate(Math.min(g.getMaxBitrate(), bitrate));

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

		StageChannel ch = c.complete();
		if(region != null) {
			Region r = Region.fromKey(region);
			if(r != Region.UNKNOWN) ch.getManager().setRegion(r).complete();
		}

		mappings.put(id, ch.getId());
	}

}
