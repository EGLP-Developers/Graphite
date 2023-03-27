package me.eglp.gv2.util.webinterface.js.settings;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import me.eglp.gv2.util.base.guild.GraphiteCategory;
import me.eglp.gv2.util.base.guild.GraphiteMember;
import me.eglp.gv2.util.base.guild.GraphiteRole;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.base.guild.GraphiteVoiceChannel;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptEnum;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

@JavaScriptEnum
@JavaScriptClass(name = "SettingType")
public enum JavaScriptSettingType implements WebinterfaceObject {
	
	STRING(String.class),
	INTEGER(Integer.class, int.class),
	BOOLEAN(Boolean.class, boolean.class),
	DOUBLE(Double.class, double.class, Float.class, float.class),
	COLOR(Color.class),
	ROLE(GraphiteRole.class),
	MEMBER(GraphiteMember.class),
	TEXT_CHANNEL(GraphiteTextChannel.class),
	VOICE_CHANNEL(GraphiteVoiceChannel.class),
	CATEGORY(GraphiteCategory.class),
	MAP(Map.class),
	LIST(List.class),
	ENUM(Enum.class);
	
	private Class<?>[] javaTypes;

	private JavaScriptSettingType(Class<?>... javaTypes) {
		this.javaTypes = javaTypes;
	}
	
	public Class<?>[] getJavaTypes() {
		return javaTypes;
	}
	
	public static JavaScriptSettingType getByJavaType(Class<?> javaType) {
		return Arrays.stream(values())
				.filter(e -> Arrays.stream(e.javaTypes).anyMatch(j -> j.isAssignableFrom(javaType)))
				.findFirst().orElse(null);
	}

}
