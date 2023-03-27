package me.eglp.gv2.util.settings;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class MySQLSettings implements JSONConvertible {
	
	@JSONValue
	private String
		url,
		username,
		password,
		database;
	
	@JSONConstructor
	private MySQLSettings() {}

	public String getURL() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getDatabase() {
		return database;
	}
	
	public static MySQLSettings createDefault() {
		MySQLSettings s = new MySQLSettings();
		s.url = "jdbc:mariadb://localhost/";
		s.username = "mybotuser";
		s.password = "password";
		s.database = "mybot";
		return s;
	}
	
}
