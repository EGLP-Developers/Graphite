package me.eglp.gv2.util;

import java.io.File;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;

public class FileManager {
	
	private static final DateTimeFormatter LOG_FILE_FORMAT = DateTimeFormatter.ofPattern("z_yyyy-MM-dd_HH-mm-ss");

	private File parentFolder;
	
	public FileManager(File parentFolder) {
		this.parentFolder = parentFolder;
	}
	
	public File getParentFolder() {
		return parentFolder;
	}

	public File getLogFile() {
		return new File(Graphite.getMainBotInfo().getFileLocation(), "logs/" + LOG_FILE_FORMAT.format(Instant.now().atZone(ZoneOffset.systemDefault())) + ".log");
	}
	
	public File getDebugLogFile(DebugCategory category) {
		return new File(Graphite.getMainBotInfo().getFileLocation(), "logs/debug/" + category.toString().toLowerCase() + ".log");
	}
	
	public File getEmojiFolder() {
		return new File(Graphite.getMainBotInfo().getFileLocation(), "emoji/");
	}
	
}
