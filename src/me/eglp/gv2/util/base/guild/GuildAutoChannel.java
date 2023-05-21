package me.eglp.gv2.util.base.guild;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.multiplex.ContextHandle;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

@JavaScriptClass(name = "AutoChannel")
public class GuildAutoChannel implements WebinterfaceObject {

	private GraphiteGuild guild;
	private GraphiteVoiceChannel channel;
	private GraphiteCategory category;

	public GuildAutoChannel(GraphiteVoiceChannel channel, GraphiteCategory category) {
		this.guild = channel.getGuild();
		this.channel = channel;
		this.category = category;
	}

	public GraphiteGuild getGuild() {
		return guild;
	}

	@JavaScriptGetter(name = "getChannel", returning = "channel")
	public GraphiteVoiceChannel getChannel() {
		return channel;
	}

	@JavaScriptGetter(name = "getCategory", returning = "category")
	public GraphiteCategory getCategory() {
		return category;
	}

	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("channel", channel.toWebinterfaceObject());
		object.put("category", (category == null ? null : category.toWebinterfaceObject()));
	}

	public CompletableFuture<GraphiteVoiceChannel> createAutoChannel() {
		int n = 1;
		List<VoiceChannel> chs = null;
		while(!(chs = guild.getJDAGuild().getVoiceChannelsByName(channel.getName() + " #" + n, false)).isEmpty()) {
			if(chs.stream().allMatch(c -> !guild.getChannelsConfig().isAutoCreatedChannel(guild.getVoiceChannel(c)) || c.getMembers().isEmpty())) break;
			n++;
		}
		ChannelAction<VoiceChannel> vc = guild.getJDAGuild().createVoiceChannel(channel.getName() + " #" + n);
		VoiceChannel jdaChannel = channel.getJDAChannel();
		if(category != null) vc.setParent(category.getJDACategory());

		vc.setBitrate(Math.min(jdaChannel.getGuild().getMaxBitrate(), jdaChannel.getBitrate()));
		vc.setPosition(jdaChannel.getPosition());
		vc.setUserlimit(jdaChannel.getUserLimit());

		for(PermissionOverride o : jdaChannel.getPermissionOverrides()) {
			long id = o.getIdLong();
			if(o.isMemberOverride()) {
				vc.addMemberPermissionOverride(id, o.getAllowedRaw(), o.getDeniedRaw());
			}else {
				vc.addRolePermissionOverride(id, o.getAllowedRaw(), o.getDeniedRaw());
			}
		}

		CompletableFuture<GraphiteVoiceChannel> vcn = new CompletableFuture<>();
		ContextHandle h = GraphiteMultiplex.handle();
		vc.queue(v -> {
			h.reset();
			try {
				GraphiteVoiceChannel ch = guild.getVoiceChannel(v);
				guild.getChannelsConfig().addAutoCreatedChannel(ch);
				vcn.complete(ch);
			}catch(Exception e) {
				GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Error when creating autochannel (1)", e);
			}
		}, t -> {
			GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Error when creating autochannel (2)", t);
			vcn.completeExceptionally(t);
		});
		return vcn;
	}

	public void delete() {
		guild.getChannelsConfig().removeAutoChannel(getChannel());
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof GuildAutoChannel)) return false;
		GuildAutoChannel u = (GuildAutoChannel) o;
		return channel.equals(u.channel);
	}

	@JavaScriptFunction(calling = "setAutoChannelCategory", withGuild = true)
	public static void setAutoChannelCategory(@JavaScriptParameter(name = "channel_id") String channelID, @JavaScriptParameter(name = "category_id") String categoryID) {};

	@JavaScriptFunction(calling = "getAutoChannels", returning = "channels", withGuild = true)
	public static void getAutoChannels() {};

	@JavaScriptFunction(calling = "addAutoChannel", returning = "channel", withGuild = true)
	public static void addAutoChannel(@JavaScriptParameter(name = "channel_id") String channelID) {};

	@JavaScriptFunction(calling = "removeAutoChannel", withGuild = true)
	public static void removeAutoChannel(@JavaScriptParameter(name = "channel_id") String channelID) {};

}
