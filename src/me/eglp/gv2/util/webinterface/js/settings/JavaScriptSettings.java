package me.eglp.gv2.util.webinterface.js.settings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.ObjectSerializer;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.ClassUtils;
import me.mrletsplay.mrcore.misc.FriendlyException;

public interface JavaScriptSettings extends WebinterfaceObject {
	
	public static List<JavaScriptSettingDescriptor> getSettingDescriptors(Class<? extends JavaScriptSettings> settingsClass) {
		List<JavaScriptSettingDescriptor> descriptors = new ArrayList<>();
		for(Field f : ClassUtils.getFields(settingsClass)) {
			JavaScriptSetting s = f.getAnnotation(JavaScriptSetting.class);
			if(s == null) continue;
			JavaScriptSettingType type = JavaScriptSettingType.getByJavaType(f.getType());
			if(type == null) throw new FriendlyException("Invalid type for field " + f.getName() + " in class " + settingsClass.getName());
			
			switch(type) {
				case LIST:
				{
					JavaScriptListSetting ls = f.getAnnotation(JavaScriptListSetting.class);
					if(ls == null) throw new FriendlyException("List type not annotated for field " + f.getName() + " in class " + settingsClass.getName());
					JavaScriptSettingType valueType = JavaScriptSettingType.getByJavaType(ls.value());
					if(valueType == JavaScriptSettingType.LIST || valueType == JavaScriptSettingType.MAP) throw new FriendlyException("Bruder, viel zu komplex. Chill mal");
					JavaScriptSettingDescriptor valueDesc;
					if(valueType == JavaScriptSettingType.ENUM) {
						valueDesc = JavaScriptSettingDescriptor.ofEnum(null, ls.valueFriendlyName(), ObjectSerializer.getClassName(ls.value().asSubclass(WebinterfaceObject.class)), 0);
					}else {
						valueDesc = JavaScriptSettingDescriptor.ofValue(valueType, null, ls.valueFriendlyName(), 0);
					}
					descriptors.add(JavaScriptSettingDescriptor.ofList(s.name(), s.friendlyName(), valueDesc, s.order()));
					break;
				}
				case MAP:
				{
					JavaScriptMapSetting ls = f.getAnnotation(JavaScriptMapSetting.class);
					if(ls == null) throw new FriendlyException("Map type not annotated for field " + f.getName() + " in class " + settingsClass.getName());
					JavaScriptSettingType keyType = JavaScriptSettingType.getByJavaType(ls.keyType());
					if(keyType == JavaScriptSettingType.LIST || keyType == JavaScriptSettingType.MAP) throw new FriendlyException("Bruder, viel zu komplex. Chill mal");
					JavaScriptSettingDescriptor keyDesc;
					if(keyType == JavaScriptSettingType.ENUM) {
						keyDesc = JavaScriptSettingDescriptor.ofEnum(null, ls.keyFriendlyName(), ObjectSerializer.getClassName(ls.keyType().asSubclass(WebinterfaceObject.class)), 0);
					}else {
						keyDesc = JavaScriptSettingDescriptor.ofValue(keyType, null, ls.keyFriendlyName(), 0);
					}
					
					JavaScriptSettingType valueType = JavaScriptSettingType.getByJavaType(ls.valueType());
					if(valueType == JavaScriptSettingType.LIST || valueType == JavaScriptSettingType.MAP) throw new FriendlyException("Bruder, viel zu komplex. Chill mal");
					JavaScriptSettingDescriptor valueDesc;
					if(valueType == JavaScriptSettingType.ENUM) {
						valueDesc = JavaScriptSettingDescriptor.ofEnum(null, ls.valueFriendlyName(), ObjectSerializer.getClassName(ls.valueType().asSubclass(WebinterfaceObject.class)), 0);
					}else {
						valueDesc = JavaScriptSettingDescriptor.ofValue(valueType, null, ls.valueFriendlyName(), 0);
					}
					
					descriptors.add(JavaScriptSettingDescriptor.ofMap(s.name(), s.friendlyName(), keyDesc, valueDesc, s.order()));
					break;
				}
				case ENUM:
				{
					descriptors.add(JavaScriptSettingDescriptor.ofEnum(s.name(), s.friendlyName(), ObjectSerializer.getClassName(f.getType().asSubclass(WebinterfaceObject.class)), s.order()));
					break;
				}
				default:
				{
					descriptors.add(JavaScriptSettingDescriptor.ofValue(type, s.name(), s.friendlyName(), s.order()));
					break;
				}
			}
		}
		
		return descriptors.stream()
				.sorted(Comparator.comparingInt(d -> d.getOrder()))
				.collect(Collectors.toList());
	}
	
	@JavaScriptGetter(name = "getSettingDescriptors", returning = "descriptors")
	public default void getSettingDescriptors() {};
	
	@Override
	default void preSerializeWI(JSONObject object) {
		object.put("descriptors", getSettingDescriptors(getClass()).stream()
				.map(d -> d.toWebinterfaceObject())
				.collect(Collectors.toCollection(JSONArray::new)));
	}

}
