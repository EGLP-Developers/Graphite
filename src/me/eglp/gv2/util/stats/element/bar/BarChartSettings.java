package me.eglp.gv2.util.stats.element.bar;

import java.awt.Color;
import java.util.Collections;

import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.stats.GraphiteStatistic;
import me.eglp.gv2.util.stats.element.StatisticsElementPointFrequency;
import me.eglp.gv2.util.stats.element.StatisticsElementSettings;
import me.eglp.gv2.util.stats.element.StatisticsElementTimeframe;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.JavaScriptSetter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.settings.JavaScriptSetting;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class BarChartSettings extends StatisticsElementSettings {
	
	@JavaScriptSetting(name = "timeframe", friendlyName = "Timeframe", order = 1)
	@JSONValue
	@JavaScriptValue(getter = "getTimeframe", setter = "setTimeframe")
	private StatisticsElementTimeframe timeframe;

	@JavaScriptSetting(name = "pointFrequency", friendlyName = "Point frequency", order = 2)
	@JSONValue
	@JavaScriptValue(getter = "getPointFrequency", setter = "setPointFrequency")
	private StatisticsElementPointFrequency pointFrequency;
	
	@JavaScriptSetting(name = "barColor", friendlyName = "Bar Color", order = 3)
	private Color barColor;
	
	@JSONConstructor
	public BarChartSettings() {
		this.timeframe = StatisticsElementTimeframe.ONE_WEEK;
		this.pointFrequency = StatisticsElementPointFrequency.DAILY;
		this.barColor = GraphiteUtil.getDistinctColor(3);
	}
	
	public void setStatistic(GraphiteStatistic statistic) {
		this.statistics = Collections.singletonList(statistic);
	}
	
	public GraphiteStatistic getStatistic() {
		return statistics.isEmpty() ? null : statistics.get(0);
	}
	
	public void setTimeframe(StatisticsElementTimeframe timeframe) {
		this.timeframe = timeframe;
	}
	
	public StatisticsElementTimeframe getTimeframe() {
		return timeframe;
	}
	
	public void setPointFrequency(StatisticsElementPointFrequency pointFrequency) {
		this.pointFrequency = pointFrequency;
	}
	
	public StatisticsElementPointFrequency getPointFrequency() {
		return pointFrequency;
	}

	@JavaScriptSetter(name = "setColor", setting = "color")
	public void setBarColor(Color barColor) {
		this.barColor = barColor;
	}

	@JavaScriptGetter(name = "getColor", returning = "color")
	public Color getBarColor() {
		return barColor;
	}

	@Override
	public boolean isValid() {
		return super.isValid()
			&& statistics.size() == 1
			&& timeframe != null
			&& pointFrequency != null
			&& pointFrequency.getRawTimeframe() <= timeframe.getRawTimeframe()
			&& timeframe.getRawTimeframe() / pointFrequency.getRawTimeframe() < 50;
	}
	
	@Override
	public void preSerialize(JSONObject object) {
		super.preSerialize(object);
		object.put("barColor", barColor.getRGB());
	}
	
	@Override
	public void preDeserialize(JSONObject object) {
		super.preDeserialize(object);
		if(object.has("barColor")) barColor = new Color(object.getInt("barColor"));
	}
	
	@Override
	public void preSerializeWI(JSONObject object) {
		super.preSerializeWI(object);
		object.put("barColor", barColor.getRGB());
	}
	
	@Override
	public void preDeserializeWI(JSONObject object) {
		super.preDeserializeWI(object);
		if(object.has("barColor")) barColor = new Color(object.getInt("barColor"));
	}
	
}
