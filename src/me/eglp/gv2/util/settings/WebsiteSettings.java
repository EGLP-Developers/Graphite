package me.eglp.gv2.util.settings;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class WebsiteSettings implements JSONConvertible {
	
	@JSONValue
	private String
		baseURL,
		webinterfaceURL,
		faqURL,
		multiplexURL;
	
	@JSONValue
	private int
		websocketPort,
		websiteEndpointPort;
	
	@JSONValue
	private String websiteEndpointKey;
	
	@JSONConstructor
	private WebsiteSettings() {}
	
	public String getBaseURL() {
		return baseURL;
	}
	
	public String getWebinterfaceURL() {
		return webinterfaceURL.replace("{baseURL}", baseURL);
	}
	
	public String getFAQURL() {
		return faqURL.replace("{baseURL}", baseURL);
	}
	
	public String getMultiplexURL() {
		return multiplexURL.replace("{baseURL}", baseURL);
	}
	
	public int getWebsocketPort() {
		return websocketPort;
	}
	
	public int getWebsiteEndpointPort() {
		return websiteEndpointPort;
	}
	
	public String getWebsiteEndpointKey() {
		return websiteEndpointKey;
	}
	
	public static WebsiteSettings createDefault() {
		WebsiteSettings s = new WebsiteSettings();
		s.baseURL = "https://my-bot.example.com";
		s.webinterfaceURL = "{baseURL}/webinterface";
		s.faqURL = "{baseURL}/faq";
		s.multiplexURL = "{baseURL}/multiplex";
		s.websocketPort = 8746;
		s.websiteEndpointPort = 8846;
		s.websiteEndpointKey = "Website Endpoint Key";
		return s;
	}

}
