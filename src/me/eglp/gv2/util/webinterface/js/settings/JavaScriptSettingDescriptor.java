package me.eglp.gv2.util.webinterface.js.settings;

import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

@JavaScriptClass(name = "SettingDescriptor")
public class JavaScriptSettingDescriptor implements WebinterfaceObject {
	
	@JavaScriptValue(getter = "getType")
	private JavaScriptSettingType type;
	
	@JavaScriptValue(getter = "getName")
	private String name;

	@JavaScriptValue(getter = "getFriendlyName")
	private String friendlyName;

	@JavaScriptValue(getter = "getKeyDescriptor")
	private JavaScriptSettingDescriptor keyDescriptor; // for Maps
	
	@JavaScriptValue(getter = "getValueDescriptor")
	private JavaScriptSettingDescriptor valueDescriptor; // for Lists, Maps
	
	@JavaScriptValue(getter = "getEnumName")
	private String enumName;
	
	private int order;
	
	private JavaScriptSettingDescriptor(JavaScriptSettingType type, String name, String friendlyName, JavaScriptSettingDescriptor keyDescriptor, JavaScriptSettingDescriptor valueDescriptor, String enumName, int order) {
		this.type = type;
		this.name = name;
		this.friendlyName = friendlyName;
		this.keyDescriptor = keyDescriptor;
		this.valueDescriptor = valueDescriptor;
		this.enumName = enumName;
		this.order = order;
	}

	public JavaScriptSettingType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public String getFriendlyName() {
		return friendlyName;
	}
	
	public JavaScriptSettingDescriptor getKeyDescriptor() {
		return keyDescriptor;
	}
	
	public JavaScriptSettingDescriptor getValueDescriptor() {
		return valueDescriptor;
	}
	
	public String getEnumName() {
		return enumName;
	}
	
	public int getOrder() {
		return order;
	}
	
	public static JavaScriptSettingDescriptor ofValue(JavaScriptSettingType type, String name, String friendlyName, int order) {
		return new JavaScriptSettingDescriptor(type, name, friendlyName, null, null, null, order);
	}
	
	public static JavaScriptSettingDescriptor ofList(String name, String friendlyName, JavaScriptSettingDescriptor valueDescriptor, int order) {
		return new JavaScriptSettingDescriptor(JavaScriptSettingType.LIST, name, friendlyName, null, valueDescriptor, null, order);
	}
	
	public static JavaScriptSettingDescriptor ofMap(String name, String friendlyName, JavaScriptSettingDescriptor keyDescriptor, JavaScriptSettingDescriptor valueDescriptor, int order) {
		return new JavaScriptSettingDescriptor(JavaScriptSettingType.MAP, name, friendlyName, keyDescriptor, valueDescriptor, null, order);
	}
	
	public static JavaScriptSettingDescriptor ofEnum(String name, String friendlyName, String enumName, int order) {
		return new JavaScriptSettingDescriptor(JavaScriptSettingType.ENUM, name, friendlyName, null, null, enumName, order);
	}
	
}
