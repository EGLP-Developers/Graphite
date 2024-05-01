package me.eglp.gv2.util.stats.element.donut;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.util.stats.GraphiteStatistic;
import me.eglp.gv2.util.stats.element.StatisticsElementSettings;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.ObjectSerializer;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.eglp.gv2.util.webinterface.js.settings.JavaScriptMapSetting;
import me.eglp.gv2.util.webinterface.js.settings.JavaScriptSetting;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class DonutChartSettings extends StatisticsElementSettings implements JSONConvertible, WebinterfaceObject{

	@JavaScriptSetting(name = "cutoutRatio", friendlyName = "Cutout ratio", order = 2)
	@JSONValue
	@JavaScriptValue(getter = "getCutoutRatio", setter = "setCutoutRatio")
	private double cutoutRatio;

	@JavaScriptSetting(name = "spacing", friendlyName = "Spacing", order = 3)
	@JavaScriptValue(getter = "getSpacing", setter = "setSpacing")
	private double spacing;

	@JavaScriptSetting(name = "colors", friendlyName = "Colors", order = 1)
	@JavaScriptMapSetting(keyType = GraphiteStatistic.class, keyFriendlyName = "Statistic", valueType = Color.class, valueFriendlyName = "Color")
	private Map<GraphiteStatistic, Color> colors;

	@JSONConstructor
	public DonutChartSettings() {
		this.colors = new LinkedHashMap<>();
		this.cutoutRatio = 0.5d;
		this.spacing = 10f;
	}

	public void addStatistics(GraphiteStatistic... statistic) {
		statistics.addAll(Arrays.asList(statistic));
	}

	public void removeStatistic(GraphiteStatistic statistic) {
		statistics.remove(statistic);
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

	public void setCutoutRatio(double cutoutRatio) {
		this.cutoutRatio = cutoutRatio;
	}

	public double getCutoutRatio() {
		return cutoutRatio;
	}

	public void setSpacing(float spacing) {
		this.spacing = spacing;
	}

	public double getSpacing() {
		return spacing;
	}

	@Override
	public boolean isValid() {
		return super.isValid()
			&& cutoutRatio >= 0
			&& cutoutRatio <= 1
			&& spacing >= 0
			&& spacing <= 50
			&& colors != null
			&& statistics.stream().allMatch(s -> s.hasCategories() || colors.containsKey(s))
			&& statistics.stream().noneMatch(s -> s.isCumulative());
	}

	@Override
	public void preSerialize(JSONObject object) {
		super.preSerialize(object);
		object.put("spacing", spacing);
		JSONObject o = new JSONObject();
		colors.entrySet().forEach(en -> o.put(en.getKey().name(), en.getValue().getRGB()));
		object.put("colors", o);
	}

	@Override
	public void preDeserialize(JSONObject object) {
		super.preDeserialize(object);
		spacing = object.getFloat("spacing");
		JSONObject o = object.getJSONObject("colors");
		for(String k : o.keys()) {
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
		for(String k : o.keys()) {
			colors.put(GraphiteStatistic.valueOf(k), new Color(o.getInt(k)));
		}
		insertMissingColors(statistics, colors);
	}

}
