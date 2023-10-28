package me.eglp.gv2.util.backup.data.messages;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedAuthor;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedField;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedFooter;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedTitle;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class BackupMessageEmbed implements JSONConvertible {

	@JSONValue
	private String
		title,
		url,
		description,
		authorName,
		authorUrl,
		authorIconUrl,
		footerText,
		footerIconUrl,
		imageUrl,
		thumbnail;

	@JSONValue
	private int color;

	@JSONValue
	private long timestamp;

	@JSONValue
	@JSONComplexListType(BackupMessageEmbedField.class)
	private List<BackupMessageEmbedField> fields;

	@JSONConstructor
	private BackupMessageEmbed() {}

	public BackupMessageEmbed(MessageEmbed embed) {
		this.title = embed.getTitle();
		this.url = embed.getUrl();

		this.description = embed.getDescription();

		if(embed.getAuthor() != null) {
			this.authorName = embed.getAuthor().getName();
			this.authorUrl = embed.getAuthor().getUrl();
			this.authorIconUrl = embed.getAuthor().getIconUrl();
		}

		if(embed.getFooter() != null) {
			this.footerText = embed.getFooter().getText();
			this.footerIconUrl = embed.getFooter().getIconUrl();
		}

		this.color = embed.getColorRaw();

		if(embed.getTimestamp() != null) {
			this.timestamp = embed.getTimestamp().toInstant().toEpochMilli();
		}else {
			this.timestamp = -1;
		}

		this.fields = new ArrayList<>(embed.getFields().stream().map(BackupMessageEmbedField::new).collect(Collectors.toList()));
	}

	public String getTitle() {
		return title;
	}

	public String getURL() {
		return url;
	}

	public String getDescription() {
		return description;
	}

	public String getAuthorName() {
		return authorName;
	}

	public String getAuthorUrl() {
		return authorUrl;
	}

	public String getAuthorIconUrl() {
		return authorIconUrl;
	}

	public String getFooterText() {
		return footerText;
	}

	public String getFooterIconUrl() {
		return footerIconUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public int getColor() {
		return color;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public List<BackupMessageEmbedField> getFields() {
		return fields;
	}

	public WebhookEmbed createEmbed() {
		WebhookEmbedBuilder b = new WebhookEmbedBuilder();
		if(title != null) b.setTitle(new EmbedTitle(title, url));
		b.setDescription(description);
		if(authorName != null) b.setAuthor(new EmbedAuthor(authorName, authorIconUrl, authorUrl));
		if(footerText != null) b.setFooter(new EmbedFooter(footerText, footerIconUrl));
		b.setImageUrl(imageUrl);
		b.setThumbnailUrl(thumbnail);
		b.setColor(color);
		if(timestamp != -1) b.setTimestamp(Instant.ofEpochMilli(timestamp));

		fields.forEach(f -> {
			b.addField(new EmbedField(f.isInline(), f.getName(), f.getValue()));
		});

		if(b.isEmpty()) return null;

		return b.build();
	}

}
