package me.eglp.gv2.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import me.mrletsplay.mrcore.io.IOUtils;

public class GraphiteLogger {
	
	private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("'['z dd.MM.yyyy HH:mm:ss']' ");

	private BufferedWriter writer;
	
	public GraphiteLogger(File file) throws IOException {
		IOUtils.createFile(file);
		this.writer = new BufferedWriter(new FileWriter(file));
	}
	
	public void log(String s) {
		try {
			if(s.trim().isEmpty()) return;
			writer.write(TIMESTAMP_FORMAT.format(Instant.now().atZone(ZoneOffset.systemDefault())) + s);
			if(!s.endsWith("\n")) writer.newLine();
			writer.flush();
		} catch (IOException ignored) {}
	}
	
	public void close() {
		try {
			writer.close();
		} catch (IOException ignored) {}
	}
	
}
