package me.eglp.gv2.util.backup.data.channels;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteForumChannel;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.backup.data.emoji.BackupEmojiRef;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer.SortOrder;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel.Layout;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

public class BackupForumChannel implements JSONConvertible, WebinterfaceObject, BackupChannel {

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

	@JSONValue
	private boolean tagRequired;

	@JSONValue
	@JSONComplexListType(BackupForumTag.class)
	private List<BackupForumTag> tags;

	@JSONValue
	private BackupEmojiRef defaultReaction;

	@JSONValue
	private String sortOrder;

	@JSONValue
	private String layout;

	@JSONValue
	@JSONComplexListType(BackupPermissionOverride.class)
	private List<BackupPermissionOverride> permissionOverrides;

	@JSONConstructor
	private BackupForumChannel() {}

	public BackupForumChannel(GraphiteForumChannel graphiteChannel) {
		ForumChannel ch = graphiteChannel.getJDAChannel();
		if(ch == null) throw new IllegalStateException("Unknown channel or invalid context");

		this.id = graphiteChannel.getID();
		this.name = ch.getName();
		this.position = ch.getPosition();
		this.topic = ch.getTopic();
		this.nsfw = ch.isNSFW();
		this.tagRequired = ch.isTagRequired();
		this.tags = ch.getAvailableTags().stream().map(BackupForumTag::new).toList();
		this.defaultReaction = ch.getDefaultReaction() == null ? null : new BackupEmojiRef(ch.getDefaultReaction());
		this.sortOrder = ch.getDefaultSortOrder().name();
		this.layout = ch.getDefaultLayout().name();

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

	public List<BackupPermissionOverride> getPermissionOverrides() {
		return permissionOverrides;
	}

	@Override
	public void restore(GraphiteGuild guild, Category parent, IDMappings mappings) {
		Guild g = guild.getJDAGuild();
		if(!g.getFeatures().contains("COMMUNITY")) return;

		ChannelAction<ForumChannel> c = g.createForumChannel(name, parent);
		if(position >= 0) c.setPosition(position);
		c.setTopic(topic);
		c.setNSFW(nsfw);
		// tagRequired is set below
		c.setAvailableTags(tags.stream()
			.sorted(Comparator.comparing(t -> t.position))
			.map(t -> t.toData(guild, mappings))
			.toList());
		c.setDefaultReaction(defaultReaction == null ? null : defaultReaction.get(guild, mappings));
		c.setDefaultSortOrder(SortOrder.valueOf(sortOrder));
		c.setDefaultLayout(Layout.valueOf(layout));

		permissionOverrides.stream()
			.filter(o -> o.getType() == BackupPermissionOverride.Type.MEMBER)
			.forEach(o -> c.addMemberPermissionOverride(Long.parseLong(o.getID()), o.getAllowed(), o.getDenied()));

		permissionOverrides.stream()
			.filter(o -> o.getType() == BackupPermissionOverride.Type.ROLE)
			.forEach(o -> {
				String newRoleID = mappings.getNewID(o.getID());
				if(newRoleID == null) return; // Because of the role rate limit, the role might not have been restored
				c.addRolePermissionOverride(Long.parseLong(newRoleID), o.getAllowed(), o.getDenied());
			});

		ForumChannel ch = c.complete();
		mappings.put(id, ch.getId());

		int i = 0;
		for(ForumTag tag : ch.getAvailableTags()) {
			mappings.put(tags.get(i).id, tag.getId()); // Should be fine since there should be no other tags yet
			i++;
		}

		ch.getManager().setTagRequired(tagRequired).complete();
	}

	private static class BackupForumTag implements JSONConvertible {

		@JSONValue
		private String id;

		@JSONValue
		private String name;

		@JSONValue
		private int position;

		@JSONValue
		private BackupEmojiRef emoji;

		@JSONValue
		private boolean moderated;

		@JSONConstructor
		private BackupForumTag() {}

		public BackupForumTag(ForumTag tag) {
			this.id = tag.getId();
			this.name = tag.getName();
			this.position = tag.getPosition();
			this.moderated = tag.isModerated();
			this.emoji = tag.getEmoji() == null ? null : new BackupEmojiRef(tag.getEmoji());
		}

		public ForumTagData toData(GraphiteGuild guild, IDMappings mappings) {
			return new ForumTagData(name)
				.setEmoji(emoji == null ? null : emoji.get(guild, mappings))
				.setModerated(moderated);
		}

	}

}
