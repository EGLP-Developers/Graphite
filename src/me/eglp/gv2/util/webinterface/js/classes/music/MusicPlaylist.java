package me.eglp.gv2.util.webinterface.js.classes.music;

import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONArray;

public class MusicPlaylist implements WebinterfaceObject{
	
	@JavaScriptValue(getter = "getName")
	private String name;
	
	@JavaScriptValue(getter = "getTracks")
	private JSONArray tracks;
	
	public MusicPlaylist(String name, JSONArray tracks) {
		this.name = name;
		this.tracks = tracks;
	}

}
