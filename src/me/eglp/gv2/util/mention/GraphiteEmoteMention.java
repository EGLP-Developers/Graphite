package me.eglp.gv2.util.mention;

import me.eglp.gv2.main.Graphite;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class GraphiteEmoteMention extends GraphiteMention {

	private String name, id;
	private Emoji jdaEmote;
	
	public GraphiteEmoteMention(String name, String id) {
		super(MentionType.EMOTE);
		this.name = name;
		this.id = id;
		this.jdaEmote = Graphite.getGlobalJDAEmote(id);
	}
	
	public String getEmoteName() {
		return name;
	}
	
	public String getEmoteID() {
		return id;
	}
	
	public Emoji getJDAEmote() {
		if(!isValid()) throw new IllegalStateException("Mention is invalid");
		return jdaEmote;
	}
	
	@Override
	public boolean isAmbiguous() {
		return false;
	}
	
	@Override
	public boolean isValid() {
		return jdaEmote != null;
	}

}
