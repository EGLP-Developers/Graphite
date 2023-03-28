package me.eglp.gv2.util.settings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import me.mrletsplay.mrcore.io.IOUtils;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.SerializationOption;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class GraphiteSettings {
	
	// TODO: validation for most settings

	private boolean defaultCreated;
	
	private MainBotInfo mainBotInfo;
	
	public GraphiteSettings(String path) {
		try {
			File settingsFile = new File(path);
			IOUtils.createFile(settingsFile);
			if(settingsFile.length() == 0) {
				try(BufferedWriter w = new BufferedWriter(new FileWriter(settingsFile))) {
					w.write(MainBotInfo.createDefault().toJSON(SerializationOption.DONT_INCLUDE_CLASS).toFancyString());
					w.newLine();
				}catch(IOException e) {
					throw new FriendlyException(e);
				}
				
				defaultCreated = true;
				return;
			}
			JSONObject j = new JSONObject(new String(Files.readAllBytes(settingsFile.toPath()), StandardCharsets.UTF_8));
			mainBotInfo = JSONConverter.decodeObject(j, MainBotInfo.class);
		}catch(Exception e) {
			throw new FriendlyException("Failed to load bot settings", e);
		}
	}
	
	public MainBotInfo getMainBotInfo() {
		return mainBotInfo;
	}
	
	public boolean isDefaultCreated() {
		return defaultCreated;
	}
	
	public List<String> validate() {
		return mainBotInfo.validate();
	}

}
