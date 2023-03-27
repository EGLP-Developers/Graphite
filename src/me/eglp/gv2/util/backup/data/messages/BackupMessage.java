package me.eglp.gv2.util.backup.data.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.eglp.gv2.util.GraphiteUtil;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.Message;

public class BackupMessage implements JSONConvertible {
	
	@JSONValue
	private String
		content,
		authorId,
		authorName,
		authorAvatarUrl,
		referencedMessageId;
	
	@JSONValue
	@JSONComplexListType(BackupMessageEmbed.class)
	private List<BackupMessageEmbed> embeds;
	
	@JSONConstructor
	private BackupMessage() {}
	
	public BackupMessage(Message jdaMessage) {
		this.content = jdaMessage.getContentRaw();
		this.authorId = jdaMessage.getAuthor().getId();
		this.authorName = jdaMessage.getAuthor().getName();
		this.authorAvatarUrl = jdaMessage.getAuthor().getEffectiveAvatarUrl();
		this.embeds = new ArrayList<>(jdaMessage.getEmbeds().stream().map(BackupMessageEmbed::new).collect(Collectors.toList()));
		
		if(jdaMessage.getReferencedMessage() != null) {
			this.referencedMessageId = jdaMessage.getReferencedMessage().getId();
		}
	}

	public String getContent() {
		return content;
	}

	public String getAuthorId() {
		return authorId;
	}

	public String getAuthorName() {
		return authorName;
	}

	public String getAuthorAvatarUrl() {
		return authorAvatarUrl;
	}

	public List<BackupMessageEmbed> getEmbeds() {
		return embeds;
	}
	
	public WebhookMessage createMessage() {
		WebhookMessageBuilder b = new WebhookMessageBuilder();
		b.setContent(GraphiteUtil.truncateToLength(content, Message.MAX_CONTENT_LENGTH, true));
		b.setUsername(authorName);
		b.setAvatarUrl(authorAvatarUrl);
		embeds.forEach(e -> {
			WebhookEmbed em = e.createEmbed();
			if(em != null) b.addEmbeds(em);
		});
		if(b.isEmpty()) return null;
		return b.build();
	}
	
}
