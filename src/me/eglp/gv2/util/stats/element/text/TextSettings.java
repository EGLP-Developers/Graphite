package me.eglp.gv2.util.stats.element.text;

import java.util.Arrays;

import me.eglp.gv2.util.stats.GraphiteStatistic;
import me.eglp.gv2.util.stats.element.StatisticsElementSettings;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.settings.JavaScriptSetting;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class TextSettings extends StatisticsElementSettings {
	
	@JavaScriptSetting(name = "fontSize", friendlyName = "Font Size", order = 1)
	@JSONValue
	@JavaScriptValue(getter = "getFontSize", setter = "setFontSize")
	private double fontSize;
	
	@JSONConstructor
	public TextSettings() {
		this.fontSize = 50;
	}
	
	public void addStatistics(GraphiteStatistic... statistic) {
		statistics.addAll(Arrays.asList(statistic));
	}
	
	public void removeStatistic(GraphiteStatistic statistic) {
		statistics.remove(statistic);
	}
	
	public double getFontSize() {
		return fontSize;
	}
	
	@Override
	public boolean isValid() {
		return super.isValid()
			&& statistics.stream().noneMatch(s -> s.isCumulative());
	}

}
