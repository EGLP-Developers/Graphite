package me.eglp.gv2.util.stats.element;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.stats.GraphiteStatistic;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.settings.JavaScriptListSetting;
import me.eglp.gv2.util.webinterface.js.settings.JavaScriptSetting;
import me.eglp.gv2.util.webinterface.js.settings.JavaScriptSettings;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;

public abstract class StatisticsElementSettings implements JSONConvertible, JavaScriptSettings {

	@JavaScriptSetting(name = "statistics", friendlyName = "Statistics")
	@JavaScriptListSetting(value = GraphiteStatistic.class, valueFriendlyName = "Statistic")
	@JavaScriptValue(getter = "getStatistics")
	protected List<GraphiteStatistic> statistics;

	protected StatisticsElementSettings() {
		this.statistics = new ArrayList<>();
	}

	public List<GraphiteStatistic> getStatistics() {
		return statistics;
	}

	@Override
	public void preSerialize(JSONObject object) {
		object.put("statistics", new JSONArray(statistics.stream()
				.map(o -> o.name())
				.toList()));
	}

	@Override
	public void preDeserialize(JSONObject object) {
		statistics = object.getJSONArray("statistics").stream()
				.map(o -> GraphiteStatistic.valueOf((String) o))
				.collect(Collectors.toList());
	}

	public boolean isValid() {
		return statistics != null
			&& statistics.size() >= 1;
	}

	public static void insertMissingColors(List<GraphiteStatistic> statistics, Map<GraphiteStatistic, Color> colors) {
		int i = 0;
		for(GraphiteStatistic s : statistics) {
			if(colors.containsKey(s) || s.hasCategories()) continue; // Categories always use distinct colors. TODO: duplicate colors
			colors.put(s, GraphiteUtil.getDistinctColor(i++));
		}
	}

	public static Color getUnassignedColor(Map<GraphiteStatistic, Color> colors, int colorIndex) {
		return GraphiteUtil.getDistinctColors().stream()
				.filter(c -> !colors.containsValue(c))
				.skip(colorIndex)
				.findFirst().orElse(GraphiteUtil.getDistinctColor(colorIndex));
	}

}
