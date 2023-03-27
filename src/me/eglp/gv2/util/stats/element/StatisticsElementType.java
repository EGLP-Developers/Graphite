package me.eglp.gv2.util.stats.element;

import java.lang.reflect.InvocationTargetException;

import me.eglp.gv2.util.stats.element.bar.BarChartRenderer;
import me.eglp.gv2.util.stats.element.bar.BarChartSettings;
import me.eglp.gv2.util.stats.element.donut.DonutChartRenderer;
import me.eglp.gv2.util.stats.element.donut.DonutChartSettings;
import me.eglp.gv2.util.stats.element.line.LineChartRenderer;
import me.eglp.gv2.util.stats.element.line.LineChartSettings;
import me.eglp.gv2.util.stats.element.pie.PieChartRenderer;
import me.eglp.gv2.util.stats.element.pie.PieChartSettings;
import me.eglp.gv2.util.stats.element.text.TextRenderer;
import me.eglp.gv2.util.stats.element.text.TextSettings;
import me.eglp.gv2.util.webinterface.js.JavaScriptEnum;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;
import me.mrletsplay.mrcore.misc.FriendlyException;

@JavaScriptEnum
public enum StatisticsElementType implements WebinterfaceObject, JSONPrimitiveStringConvertible {
	
	PIE_CHART(PieChartSettings.class, PieChartRenderer.INSTANCE),
	DONUT_CHART(DonutChartSettings.class, DonutChartRenderer.INSTANCE),
	LINE_CHART(LineChartSettings.class, LineChartRenderer.INSTANCE),
	BAR_CHART(BarChartSettings.class, BarChartRenderer.INSTANCE),
	TEXT(TextSettings.class, TextRenderer.INSTANCE);
	
	private Class<? extends StatisticsElementSettings> settingsType;
	private StatisticsRenderer renderer;
	
	private StatisticsElementType(Class<? extends StatisticsElementSettings> settingsType, StatisticsRenderer renderer) {
		this.settingsType = settingsType;
		this.renderer = renderer;
	}
	
	public Class<? extends StatisticsElementSettings> getSettingsType() {
		return settingsType;
	}
	
	public StatisticsRenderer getRenderer() {
		return renderer;
	}
	
	public StatisticsElementSettings createDefaultSettings() {
		try {
			return settingsType.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new FriendlyException(e);
		}
	}
	
	@Override
	public String toJSONPrimitive() {
		return name();
	}
	
	public static StatisticsElementType decodePrimitive(Object value) {
		return valueOf((String) value);
	}
	
	@JavaScriptFunction(calling = "createDefaultSettings", returning = "settings", withGuild = true)
	public static void createDefaultSettings(@JavaScriptParameter(name = "chart_type") String chartType) {};
	
}
