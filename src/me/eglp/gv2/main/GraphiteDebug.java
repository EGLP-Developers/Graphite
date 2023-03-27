package me.eglp.gv2.main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import me.mrletsplay.mrcore.io.IOUtils;

public class GraphiteDebug {
	
	private static Map<DebugCategory, GraphiteLogger> loggers;
	
	public static void init() {
		try {
			loggers = new HashMap<>();
			
			for(DebugCategory cat : DebugCategory.values()) {
				File f = Graphite.getFileManager().getDebugLogFile(cat);
				IOUtils.createFile(f);
				loggers.put(cat, new GraphiteLogger(f));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static GraphiteLogger getLogger(DebugCategory cat) {
		return loggers.get(cat);
	}
	
	public static void log(DebugCategory cat, String message, Throwable t) {
		log(cat, message);
		log(cat, t);
	}
	
	public static void log(DebugCategory cat, String message) {
		getLogger(cat).log(String.format("[%s] %s", cat, message));
	}
	
	public static void log(DebugCategory cat, Throwable t) {
		log(cat, t.toString());
		logStackTrace(cat, t);
		if(t.getCause() != null) logCause(cat, t.getCause());
	}
	
	private static void logCause(DebugCategory cat, Throwable t) {
		log(cat, "Caused by: " + t.toString());
		logStackTrace(cat, t);
		if(t.getCause() != null) logCause(cat, t.getCause());
	}
	
	private static void logStackTrace(DebugCategory cat, Throwable t) {
		for(StackTraceElement e : t.getStackTrace()) {
			log(cat, "\t" + e.toString());
		}
	}

}
