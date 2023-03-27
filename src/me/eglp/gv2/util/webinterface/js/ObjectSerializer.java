package me.eglp.gv2.util.webinterface.js;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.GraphiteDebug;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class ObjectSerializer {
	
	private static final Reflections REFLECTIONS = new Reflections("me.eglp.gv2", Scanners.SubTypes);
	private static final Set<Class<? extends WebinterfaceObject>> JS_CLASSES = getJSClasses();

	private static Set<Class<? extends WebinterfaceObject>> getJSClasses() {
		return REFLECTIONS.getSubTypesOf(WebinterfaceObject.class);
	}
	
	public static List<JSONObject> generateClassDescriptors() {
		List<JSONObject> l = new ArrayList<>();
		for(Class<? extends WebinterfaceObject> cls : JS_CLASSES) {
			l.add(generateClassDescriptor(cls));
		}
		return l;
	}

	public static Object serialize(Object obj) {
		if(obj instanceof List<?>) {
			JSONArray arr = new JSONArray();
			for(Object o : (List<?>) obj) {
				arr.add(serialize(o));
			}
			return arr;
		}else if(obj instanceof WebinterfaceObject){
			WebinterfaceObject obj2 = (WebinterfaceObject) obj;
			JSONObject o = new JSONObject();
			if(!JS_CLASSES.contains(obj.getClass())) throw new IllegalStateException("Unregistered class: " + obj.getClass().getName());
			obj2.preSerializeWI(o);
			o.put("_jsClass", getClassName(obj2.getClass()));
			for(Field field : getFields(obj2.getClass())) {
				JavaScriptValue a = field.getAnnotation(JavaScriptValue.class);
				if(a == null) continue;
				field.setAccessible(true);
				try {
					Object val = field.get(obj2);
					val = serialize(val);
					o.put(field.getName(), val);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					GraphiteDebug.log(DebugCategory.WEBINTERFACE, e);
				}
			}
			if(obj2.isEnum()) {
				o.put("enum_value", ((Enum<?>)obj2).name());
			}
			return o;
		}else return obj;
	}

	public static Object deserialize(Object obj) {
		return deserialize(obj, null);
	}
	
	public static Object deserialize(Object obj, Class<?> clazz) {
		if(obj instanceof JSONArray) {
			List<Object> l = new ArrayList<>();
			for(Object o : (JSONArray) obj) {
				l.add(deserialize(o));
			}
			return l;
		}else if(obj instanceof JSONObject) {
			JSONObject o = (JSONObject) obj;
			if(!o.isOfType("_jsClass", JSONType.STRING)) return obj;
			String cName = o.getString("_jsClass");
			Class<? extends WebinterfaceObject> cls = JS_CLASSES.stream().filter(c -> getClassName(c).equals(cName)).findFirst().orElse(null);
			if(cls == null) throw new FriendlyException("Unregistered class: " + cName);
			if(cls.isAnnotationPresent(JavaScriptEnum.class)) {
				try {
					return cls.getMethod("valueOf", String.class).invoke(null, o.getString("enum_value"));
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					throw new FriendlyException("Failed to get enum value", e);
				}
			}else {
				try {
					Constructor<? extends WebinterfaceObject> cn = cls.getDeclaredConstructor();
					cn.setAccessible(true);
					WebinterfaceObject n = cn.newInstance();
					n.preDeserializeWI(o);
					for(Field field : getFields(cls)) {
						JavaScriptValue a = field.getAnnotation(JavaScriptValue.class);
						if(a == null) continue;
						field.setAccessible(true);
						try {
							if(o.has(field.getName())) {
								Object vl = deserialize(o.get(field.getName()), field.getType());
								field.set(n, vl);
							}
						} catch (IllegalArgumentException | IllegalAccessException e) {
							GraphiteDebug.log(DebugCategory.WEBINTERFACE, e);
						}
					}
					return n;
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					throw new FriendlyException("Failed to create object", e);
				}
			}
		}else if(clazz != null) {
			try {
				return JSONType.castJSONValueTo(obj, clazz, false);
			}catch(Exception e) {
				return obj;
			}
		}else return obj;
	}
	
	public static String getClassName(Class<? extends WebinterfaceObject> clazz) {
		JavaScriptClass cls = clazz.getAnnotation(JavaScriptClass.class);
		if(cls != null && !cls.name().isEmpty()) {
			return cls.name();
		}else {
			return clazz.getSimpleName();
		}
	}
	
	public static JSONObject generateClassDescriptor(Class<? extends WebinterfaceObject> clazz) {
		JSONObject desc = new JSONObject();
		desc.put("name", getClassName(clazz));
		JavaScriptEnum eA = clazz.getAnnotation(JavaScriptEnum.class);
		if(eA != null) {
			desc.put("is_enum", true);
			Enum<?>[] vals;
			try {
				vals = (Enum<?>[]) clazz.getDeclaredMethod("values").invoke(null);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				throw new FriendlyException(e);
			}
			JSONObject values = new JSONObject();
			for(Enum<?> o : vals) {
				values.put(o.name(), ((WebinterfaceObject) o).toWebinterfaceObject());
			}
			desc.put("enum_values", values);
		}
		JSONObject instanceMethods = new JSONObject();
		JSONObject staticMethods = new JSONObject();
		for(Field f : getFields(clazz)) {
			JavaScriptValue a = f.getAnnotation(JavaScriptValue.class);
			if(a == null) continue;
			if(!a.getter().isEmpty()) {
				JSONObject o = new JSONObject();
				o.put("type", "getter");
				o.put("value", f.getName());
				instanceMethods.put(a.getter(), o);
			}
			if(!a.setter().isEmpty()) {
				JSONObject o = new JSONObject();
				o.put("type", "setter");
				o.put("value", f.getName());
				instanceMethods.put(a.setter(), o);
			}
		}
		for(Method m : getMethods(clazz)) {
			JavaScriptFunction a = m.getAnnotation(JavaScriptFunction.class);
			if(a != null) {
				String fName = a.name().isEmpty() ? m.getName() : a.name();
				JSONObject o = new JSONObject();
				o.put("type", "calling");
				o.put("calling", a.calling());
				o.put("returning", new JSONArray(Arrays.asList(a.returning())));
				o.put("with_guild", a.withGuild());
				JSONArray arr = new JSONArray();
				for(Parameter p : m.getParameters()) {
					JavaScriptParameter pa = p.getAnnotation(JavaScriptParameter.class);
					if(pa == null) throw new IllegalStateException("Missing JSParameter annotation for method " + m);
					JSONObject pO = new JSONObject();
					pO.put("name", pa.name());
					pO.put("type", WebinterfaceObject.class.isAssignableFrom(p.getType()) ? p.getType().getSimpleName() : null);
					arr.add(pO);
				}
				o.put("params", arr);
				if(!Modifier.isStatic(m.getModifiers())) {
					o.put("calling_params", new JSONArray(Arrays.asList(a.callingParameters())));
					instanceMethods.put(fName, o);
				}else {
					staticMethods.put(fName, o);
				}
			}
			
			JavaScriptGetter g = m.getAnnotation(JavaScriptGetter.class);
			if(g != null) {
				JSONObject o = new JSONObject();
				o.put("type", "getter");
				o.put("value", g.returning());
				instanceMethods.put(g.name().isEmpty() ? m.getName() : g.name(), o);
			}
			
			JavaScriptSetter s = m.getAnnotation(JavaScriptSetter.class);
			if(s != null) {
				JSONObject o = new JSONObject();
				o.put("type", "setter");
				o.put("value", s.setting());
				instanceMethods.put(s.name().isEmpty() ? m.getName() : s.name(), o);
			}
		}
		desc.put("instance_methods", instanceMethods);
		desc.put("static_methods", staticMethods);
		return desc;
	}
	
	private static Set<Field> getFields(Class<?> clz) {
		if(!WebinterfaceObject.class.isAssignableFrom(clz)) return Collections.emptySet();
		Set<Field> fs = new HashSet<>();
		Class<?> cls = clz;
		while(!cls.equals(Object.class)) {
			fs.addAll(Arrays.asList(cls.getDeclaredFields()));
			cls = cls.getSuperclass();
			if(cls == null) break;
		}
		return fs;
	}
	
	private static Set<Method> getMethods(Class<?> clz) {
		if(!WebinterfaceObject.class.isAssignableFrom(clz)) return Collections.emptySet();
		Set<Method> fs = new HashSet<>();
		Class<?> cls = clz;
		while(!cls.equals(Object.class)) {
			fs.addAll(Arrays.asList(cls.getDeclaredMethods()));
			for(Class<?> inf : cls.getInterfaces()) fs.addAll(getMethods(inf));
			cls = cls.getSuperclass();
			if(cls == null) break;
		}
		return fs;
	}
	
}
