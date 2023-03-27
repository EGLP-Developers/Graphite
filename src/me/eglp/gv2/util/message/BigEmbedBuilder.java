package me.eglp.gv2.util.message;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import me.mrletsplay.mrcore.misc.FriendlyException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Role;

/**
 * Allows for building "big" embeds of arbitrary size which will be split into multiple embeds to conform to Discord's character limits.<br>
 * Only the description and field values can be of any size!
 * @author MrLetsplay2003
 */
public class BigEmbedBuilder {
	
	public static final int
		SPLIT_SPACES = 0,
		SPLIT_NEWLINES = 1,
		SPLIT_ANYHWERE = 2;
	
	private String author, authorURL, authorIconURL;
	private String title, url;
	private int color;
	private String description;
	private List<BigField> fields;
	private String footer, footerIconURL;
    private OffsetDateTime timestamp;
    private String imageURL;
    private String thumbnailURL;
	
	public BigEmbedBuilder() {
		this.color = Role.DEFAULT_COLOR_RAW;
		this.fields = new ArrayList<>();
	}
	
	public BigEmbedBuilder setDescription(String description) {
		this.description = description;
		return this;
	}
	
	public BigEmbedBuilder addField(String name, String value, boolean inline) {
		this.fields.add(new BigField(name, value, inline));
		return this;
	}
	
	public BigEmbedBuilder setAuthor(String author, String authorURL, String authorIconURL) {
		this.author = author;
		this.authorURL = authorURL;
		this.authorIconURL = authorIconURL;
		return this;
	}
	
	public BigEmbedBuilder setAuthor(String author, String authorURL) {
		return setAuthor(author, authorURL, null);
	}
	
	public BigEmbedBuilder setAuthor(String author) {
		return setAuthor(author, null, null);
	}
	
	public BigEmbedBuilder setTitle(String title) {
		this.title = title;
		return this;
	}
	
	public BigEmbedBuilder setURL(String url) {
		this.url = url;
		return this;
	}
	
	public BigEmbedBuilder setFooter(String footer, String footerIconURL) {
		this.footer = footer;
		this.footerIconURL = footerIconURL;
		return this;
	}
	
	public BigEmbedBuilder setFooter(String footer) {
		return setFooter(footer, null);
	}
	
	public BigEmbedBuilder setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
		return this;
	}
	
	public BigEmbedBuilder setImageURL(String imageURL) {
		this.imageURL = imageURL;
		return this;
	}
	
	public BigEmbedBuilder setThumbnailURL(String imageURL) {
		this.imageURL = imageURL;
		return this;
	}
	
	public boolean isEmpty() {
		return description == null
				&& fields.isEmpty();
	}
	
	public List<MessageEmbed> build(int splitPolicy) {
		if(isEmpty()) return Collections.emptyList();
		
		List<MessageEmbed> embeds = new ArrayList<>();
		EmbedBuilder builder = newEmbedBuilder();
		builder.setAuthor(author, authorURL, authorIconURL);
		builder.setTitle(title, url);
		builder.setImage(imageURL);
		builder.setThumbnail(thumbnailURL);
		
		if(description != null) {
			List<String> descStr = splitToLength(description, MessageEmbed.DESCRIPTION_MAX_LENGTH, splitPolicy);
			String first = descStr.remove(0);
			builder.setDescription(first);
			for(String r : descStr) {
				for(String spl : splitToLength(r, MessageEmbed.VALUE_MAX_LENGTH, splitPolicy)) { // Split once again, because VALUE_MAX_LENGTH < DESCRIPTION_MAX_LENGTH
					if(builder.length() + spl.length() + 1 > MessageEmbed.EMBED_MAX_LENGTH_BOT) { // + 1 for title
						embeds.add(builder.build());
						builder = newEmbedBuilder();
					}
					
					builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, spl, false);
				}
			}
			embeds.add(builder.build());
		}
		
		while(!fields.isEmpty()) {
			List<BigField> row = new ArrayList<>();
			for(int i = 0; i < 3; i++) { // Max 3 fields per row
				if(!row.isEmpty() && !fields.get(0).isInline()) break;
				BigField field = fields.remove(0);
				row.add(field);
				if(!field.isInline() || fields.isEmpty()) break;
			}
			
			boolean inlineRow = row.get(0).isInline();
			
			List<List<Field>> grid = row.stream()
					.map(bf -> splitField(bf, splitPolicy))
					.collect(Collectors.toList());
			
			int width = inlineRow ? 3 : 1;
			int height = grid.stream().mapToInt(l -> l.size()).max().getAsInt();
			for(int y = 0; y < height; y++) {
				final int fY = y;
				int partialRowLength = IntStream.range(0, width)
						.map(x -> {
							Field f = getField(grid, x, fY);
							if(f == null) return 2; // Zero-width space for name and value
							return f.getName().length() + f.getValue().length();
						})
						.sum();
				
				if(builder.length() + partialRowLength > MessageEmbed.EMBED_MAX_LENGTH_BOT) {
					embeds.add(builder.build());
					builder = newEmbedBuilder();
				}
				
				for(int x = 0; x < width; x++) {
					Field field = getField(grid, x, y);
					if(field == null) {
						builder.addBlankField(row.get(0).isInline());
						continue;
					}
					
					builder.addField(grid.get(x).get(y));
				}
			}
		}
		
		builder.setThumbnail(thumbnailURL);
		builder.setTimestamp(timestamp);
		builder.setFooter(footer, footerIconURL);
		embeds.add(builder.build());
		
		return embeds;
	}
	
	private EmbedBuilder newEmbedBuilder() {
		EmbedBuilder b = new EmbedBuilder();
		b.setColor(color);
		return b;
	}
	
	private static Field getField(List<List<Field>> grid, int x, int y) {
		if(x >= grid.size()) return null;
		
		List<Field> fields = grid.get(x);
		if(y >= fields.size()) return null;
		
		return grid.get(x).get(y);
	}
	
	private static List<Field> splitField(BigField field, int splitPolicy) {
		List<Field> fields = new ArrayList<>();
		if(field.getName() == null || field.getName().isEmpty() || field.getValue() == null || field.getValue().isEmpty()) throw new FriendlyException("Name or value of field is not set");
		List<String> val = splitToLength(field.getValue(), MessageEmbed.VALUE_MAX_LENGTH, splitPolicy);
		fields.add(new Field(field.getName(), val.remove(0), field.isInline()));
		for(String v : val) fields.add(new Field(EmbedBuilder.ZERO_WIDTH_SPACE, v, field.isInline()));
		return fields;
	}
	
	private static List<String> splitToLength(String longString, int length, int splitPolicy) {
		switch(splitPolicy) {
			case SPLIT_ANYHWERE:
			{
				List<String> strings = new ArrayList<>();
				while(longString.length() > 0) {
					int subLen = Math.min(length, longString.length());
					String subStr = longString.substring(0, subLen);
					strings.add(subStr);
					longString = longString.substring(subLen);
				}
				
				return strings;
			}
			case SPLIT_SPACES:
			case SPLIT_NEWLINES:
			{
				List<String> strings = new ArrayList<>();
				while(longString.length() > 0) {
					int splitAt = 0;
					while(splitAt < longString.length()) {
						int nextCharacter = longString.indexOf(splitPolicy == SPLIT_SPACES ? ' ' : '\n', splitAt + 1); // Look for the next possible position to split
						if(nextCharacter == -1) nextCharacter = longString.length();
						if(nextCharacter > length) break;
						splitAt = nextCharacter;
					}
					
					if(splitAt == 0) splitAt = length; // There is no way for us to split at the requested character. Just cut it to the max length
					
					String subStr = longString.substring(0, splitAt);
					strings.add(subStr);
					longString = longString.substring(splitAt);
				}
				return strings;
			}
			default:
				throw new IllegalArgumentException("Invalid split policy");
		}
	}

}
