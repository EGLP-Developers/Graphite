package me.eglp.gv2.util.scripting;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.scripting.object.JSGraphite;
import me.eglp.gv2.util.scripting.object.JSVars;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;

@JavaScriptClass(name = "Script")
public class GraphiteScript implements WebinterfaceObject {

	private GraphiteGuild owner;
	private String name;
	private String content;
	private Scriptable globalScope;

	public GraphiteScript(GraphiteGuild owner, String name, String content) throws IOException {
		this.owner = owner;
		this.name = name;
		this.content = content;
		
		Context cx = enterContext();
		this.globalScope = cx.initSafeStandardObjects(null, true);
		ScriptableObject.putProperty(globalScope, "graphite", JSGraphite.INSTANCE);
		ScriptableObject.putProperty(globalScope, "vars", JSVars.INSTANCE);
		cx.evaluateString(globalScope, content, "<" + name + ">", 1, null);
		exitContext();
	}
	
	@JavaScriptGetter(name = "getName", returning = "name")
	public String getName() {
		return name;
	}
	
	public String getContent() {
		return content;
	}

	public GraphiteGuild getOwner() {
		return owner;
	}

	@JavaScriptGetter(name = "getOwnerID", returning = "ownerID")
	public String getOwnerID() {
		return owner.getID();
	}

	public Scriptable getGlobalScope() {
		return globalScope;
	}

	public void call(String method, Object... args) {
		ScriptableObject.callMethod(enterContext(), getGlobalScope(), method, args);
		exitContext();
	}
	
	public Context enterContext() {
		Context c = Context.enter();
		c.setLanguageVersion(Context.VERSION_ES6);
		c.putThreadLocal("script", this);
		c.putThreadLocal("executionTimes", new HashMap<>());
		c.putThreadLocal("messageCount", 0);
		return c;
	}
	
	public void exitContext() {
		Context.exit();
	}
	
	public static Scriptable createJSArray(Object[] array) {
		Context cx = Context.getCurrentContext();
		GraphiteScript sc = (GraphiteScript) cx.getThreadLocal("script");
		return Context.getCurrentContext().newArray(sc.getGlobalScope(), Arrays.stream(array).toArray(Object[]::new));
	}
	
	@SuppressWarnings("unchecked")
	public static void runRatelimitedAction(String key, long delay, Runnable action) {
		Context c = Context.getCurrentContext();
		Map<String, Long> map = (Map<String, Long>) c.getThreadLocal("executionTimes");
		long lte = map.getOrDefault(key, 0L);
		long d = System.currentTimeMillis() - lte;
		if(d < delay) {
			try {
				Thread.sleep(delay - d);
			} catch (InterruptedException e) {
				GraphiteDebug.log(DebugCategory.SCRIPTING, e);
			}
		}
		action.run();
		map.put(key, System.currentTimeMillis());
	}
	
	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("name", getName());
		object.put("guildID", getOwner().getID());
	}

	@JavaScriptFunction(name = "getScripts", calling = "getScripts", returning = "scripts", withGuild = true)
	public static void getScripts() {};

	@JavaScriptFunction(name = "uploadScript", calling = "uploadScript", returning = "script", withGuild = true)
	public static void uploadScript(@JavaScriptParameter(name = "script_name") String scN, @JavaScriptParameter(name = "content") String scC) {};

	@JavaScriptFunction(name = "deleteGuildScript", calling = "deleteGuildScript", withGuild = true)
	public static void deleteGuildScript(@JavaScriptParameter(name = "script_name") String scN) {};

}
