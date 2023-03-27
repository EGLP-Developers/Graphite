package me.eglp.gv2.util.stats.element.line;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.util.stats.GraphiteStatistic;
import me.eglp.gv2.util.stats.element.StatisticsElementPointFrequency;
import me.eglp.gv2.util.stats.element.StatisticsElementSettings;
import me.eglp.gv2.util.stats.element.StatisticsElementTimeframe;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.ObjectSerializer;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.eglp.gv2.util.webinterface.js.settings.JavaScriptMapSetting;
import me.eglp.gv2.util.webinterface.js.settings.JavaScriptSetting;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class LineChartSettings extends StatisticsElementSettings implements WebinterfaceObject {

	@JavaScriptSetting(name = "timeframe", friendlyName = "Timeframe", order = 2)
	@JSONValue
	@JavaScriptValue(getter = "getTimeframe", setter = "setTimeframe")
	private StatisticsElementTimeframe timeframe;

	@JavaScriptSetting(name = "pointFrequency", friendlyName = "Point frequency", order = 3)
	@JSONValue
	@JavaScriptValue(getter = "getPointFrequency", setter = "setPointFrequency")
	private StatisticsElementPointFrequency pointFrequency;

	@JavaScriptSetting(name = "colors", friendlyName = "Colors", order = 1)
	@JavaScriptMapSetting(keyType = GraphiteStatistic.class, keyFriendlyName = "Statistic", valueType = Color.class, valueFriendlyName = "Color")
	private Map<GraphiteStatistic, Color> colors;

	@JavaScriptSetting(name = "fillBelowCurve", friendlyName = "Fill below curve", order = 4)
	@JSONValue
	@JavaScriptValue(getter = "isFillBelowCurve", setter = "setFillBelowCurve")
	private boolean fillBelowCurve;

	@JavaScriptSetting(name = "stackLines", friendlyName = "Stack lines", order = 5)
	@JSONValue
	@JavaScriptValue(getter = "isStackLines", setter = "setStackLines")
	private boolean stackLines;
	
	@JSONConstructor
	public LineChartSettings() {
		this.colors = new LinkedHashMap<>();
		this.timeframe = StatisticsElementTimeframe.ONE_WEEK;
		this.pointFrequency = StatisticsElementPointFrequency.DAILY;
		this.fillBelowCurve = false;
	}
	
	public void addStatistics(GraphiteStatistic... statistic) {
		statistics.addAll(Arrays.asList(statistic));
	}
	
	public void removeStatistic(GraphiteStatistic statistic) {
		statistics.remove(statistic);
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
	
	public void putColor(GraphiteStatistic statistic, Color color) {
		colors.put(statistic, color);
	}
	
	public Color getColor(GraphiteStatistic statistic) {
		return colors.getOrDefault(statistic, Color.decode("0x38bf77"));
	}
	
	@JavaScriptGetter(name = "getColors", returning = "colors")
	public Map<GraphiteStatistic, Color> getColors() {
		return colors;
	}
	
	public void setFillBelowCurve(boolean fillBelowCurve) {
		this.fillBelowCurve = fillBelowCurve;
	}
	
	public boolean isFillBelowCurve() {
		return fillBelowCurve;
	}
	
	public void setStackLines(boolean stackLines) {
		this.stackLines = stackLines;
	}
	
	public boolean isStackLines() {
		return stackLines;
	}

	@Override
	public boolean isValid() {
		return super.isValid()
			&& timeframe != null
			&& pointFrequency != null
			&& colors != null
			&& statistics.stream().allMatch(s -> s.hasCategories() || colors.containsKey(s))
			&& pointFrequency.getRawTimeframe() <= timeframe.getRawTimeframe()
			&& timeframe.getRawTimeframe() / pointFrequency.getRawTimeframe() < 50;
	}
	
	@Override
	public void preSerialize(JSONObject object) {
		super.preSerialize(object);
		JSONObject o = new JSONObject();
		colors.entrySet().forEach(en -> o.put(en.getKey().name(), en.getValue().getRGB()));
		object.put("colors", o);
	}
	
	@Override
	public void preDeserialize(JSONObject object) {
		super.preDeserialize(object);
		JSONObject o = object.getJSONObject("colors");
		for(String k : o.keySet()) {
			colors.put(GraphiteStatistic.valueOf(k), new Color(o.getInt(k)));
		}
	}
	
	@Override
	public void preSerializeWI(JSONObject object) {
		super.preSerializeWI(object);
		JSONObject o = new JSONObject();
		colors.entrySet().forEach(en -> o.put(en.getKey().name(), en.getValue().getRGB()));
		object.put("colors", o);
	}
	
	@Override
	public void preDeserializeWI(JSONObject object) {
		super.preDeserializeWI(object);
		statistics = object.getJSONArray("statistics").stream().map(s -> (GraphiteStatistic) ObjectSerializer.deserialize(s)).collect(Collectors.toList());
		JSONObject o = object.getJSONObject("colors");
		for(String k : o.keySet()) {
			colors.put(GraphiteStatistic.valueOf(k), new Color(o.getInt(k)));
		}
		insertMissingColors(statistics, colors);
	}

}
