package me.eglp.gv2.util.webinterface.js;

import me.mrletsplay.mrcore.json.JSONObject;

public interface WebinterfaceObject {

	public default Object toWebinterfaceObject() {
		return ObjectSerializer.serialize(this);
	}
	
	public default boolean isEnum() {
		return Enum.class.isAssignableFrom(getClass());
	}
	
	public default void preSerializeWI(JSONObject object) {};
	
	public default void preDeserializeWI(JSONObject object) {};
	
}
