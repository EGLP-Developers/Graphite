package me.eglp.gv2.util.stats.element.donut;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.stats.StatisticValue;
import me.eglp.gv2.util.stats.element.GuildStatisticsElement;
import me.eglp.gv2.util.stats.element.Label;
import me.eglp.gv2.util.stats.element.StatisticsElementSettings;
import me.eglp.gv2.util.stats.element.StatisticsRenderer;

public class DonutChartRenderer implements StatisticsRenderer {
	
	public static final DonutChartRenderer INSTANCE = new DonutChartRenderer();
	
	@Override
	public void renderElement(Graphics2D g2d, GuildStatisticsElement element, int width, int height) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		DonutChartSettings settings = (DonutChartSettings) element.getSettings();
		
		int spaceRight = 10;
		int labelSpace = 20;
		int labelSize = 16;
		int labelRowSize = 30;
		
		double cutoutRatio = settings.getCutoutRatio();
		float spaceWidth = (float) settings.getSpacing();
		
		List<StatisticValue> values = new ArrayList<>(settings.getStatistics().stream()
				.flatMap(s -> Graphite.getStatistics().getLastStatisticValues(element.getGuild(), s, element.isPreviewMode(), settings.getStatistics().indexOf(s) + 1, settings.getStatistics().size()).stream())
				.sorted(Comparator.<StatisticValue>comparingInt(v -> v.getValue()).reversed())
				.collect(Collectors.toList()));
		
		int sum = values.stream().mapToInt(v -> v.getValue()).sum();
		
		int circleSize = Math.min(width * 3 / 4, height * 3 / 4);
		
		// Area
		int cIdx = 0;
		double angleSum = 90;
		for(StatisticValue v : values) {
			Color c = v.getCategory() == null ? settings.getColor(v.getStatistic()) : StatisticsElementSettings.getUnassignedColor(settings.getColors(), cIdx++);
			double angle = v.getValue() / (double) sum * 360;
			Arc2D.Double d = new Arc2D.Double(height / 2 - circleSize / 2, height / 2 - circleSize / 2, circleSize, circleSize, angleSum, -angle, Arc2D.PIE);
			Arc2D.Double d2 = new Arc2D.Double(height / 2 - circleSize / 2 * cutoutRatio, height / 2 - circleSize / 2 * cutoutRatio, circleSize * cutoutRatio, circleSize * cutoutRatio, angleSum, -angle, Arc2D.PIE);
			g2d.setColor(c);
			
			Area a = new Area(d);
			a.subtract(new Area(d2));
			
			g2d.fill(a);
			angleSum -= angle;
		}
		
		// Transparency
		angleSum = 90;
		for(StatisticValue v : values) {
			double angle = v.getValue() / (double) sum * 360;
			
			double startX = height / 2 + Math.cos(Math.toRadians(-angleSum)) * (circleSize / 2) * cutoutRatio;
			double startY = height / 2 + Math.sin(Math.toRadians(-angleSum)) * (circleSize / 2) * cutoutRatio;
			double endX = height / 2 + Math.cos(Math.toRadians(-angleSum)) * circleSize / 2;
			double endY = height / 2 + Math.sin(Math.toRadians(-angleSum)) * circleSize / 2;
			
			if(settings.getStatistics().size() > 1) {
				Line2D.Double ln = new Line2D.Double(startX, startY, endX, endY);
				g2d.setStroke(new BasicStroke(spaceWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
				g2d.setComposite(AlphaComposite.Src);
				g2d.setColor(new Color(0, 0, 0, 0));
				g2d.draw(ln);
			}
			
			angleSum -= angle;
		}

		g2d.setComposite(AlphaComposite.SrcOver);
		g2d.setFont(StatisticsRenderer.DEFAULT_FONT);

		List<Label> labels = values.stream()
				.map(v -> Label.parse(g2d, label(element.getGuild(), v), EMOJI_SIZE))
				.collect(Collectors.toList());
		
		int maxWidth = labels.stream()
				.mapToInt(l -> (int) l.getBounds().getWidth())
				.max().orElse(0);
		
		int boxHeight = values.size() * 30;
		int i = 0;
		int cIdx2 = 0;
		for(StatisticValue v : values) {
			Label lbl = labels.get(i);
			Color c = v.getCategory() == null ? settings.getColor(v.getStatistic()) : StatisticsElementSettings.getUnassignedColor(settings.getColors(), cIdx2++);
			
			StatisticsRenderer.drawLabel(g2d, lbl, width - maxWidth - spaceRight, height / 2 - boxHeight / 2 + i * labelRowSize + labelRowSize / 2);
			g2d.setColor(c);
			g2d.fillRect(width - maxWidth - spaceRight - labelSpace - labelSize / 2, height / 2 - boxHeight / 2 + i * labelRowSize + labelRowSize / 2 - labelSize / 2, labelSize, labelSize);
			i++;
		}
	}
	
	// TODO: Improve label rendering to allow mixing text with emoji
	private String label(GraphiteGuild guild, StatisticValue value) {
		return value.getCategory() == null ? value.getStatistic().getFriendlyName(guild) + " (" + value.getValue() + ")" : value.getCategory();
	}
	
}
