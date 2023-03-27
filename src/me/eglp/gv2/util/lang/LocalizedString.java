package me.eglp.gv2.util.lang;

import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.base.GraphiteLocalizedObject;

public interface LocalizedString extends GraphiteLocalizedObject<String> {
	
	@Override
	public default String getFor(GraphiteLocalizable localized, String... params) {
		String msg = localized == null ? getFallback() : localized.getLocale().getString(this, params);
		for(me.eglp.gv2.util.emote.JDAEmote e : me.eglp.gv2.util.emote.JDAEmote.values()) {
			msg = msg.replace("{emote_" + e.name().toLowerCase() + "}", e.getUnicode());
		}
		return msg;
	}
	
	public String getMessagePath();
	
	public String getFallback();
	
}
