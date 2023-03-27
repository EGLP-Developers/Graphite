package me.eglp.gv2.util.stats.element.line;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.stats.GraphiteStatistic;
import me.eglp.gv2.util.stats.PartialStatistics;
import me.eglp.gv2.util.stats.element.GuildStatisticsElement;
import me.eglp.gv2.util.stats.element.Label;
import me.eglp.gv2.util.stats.element.StatisticsElementPointFrequency;
import me.eglp.gv2.util.stats.element.StatisticsElementSettings;
import me.eglp.gv2.util.stats.element.StatisticsElementTimeframe;
import me.eglp.gv2.util.stats.element.StatisticsRenderer;

public class LineChartRenderer implements StatisticsRenderer {
	
	public static final LineChartRenderer INSTANCE = new LineChartRenderer();
	
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###.#");
	
	@SuppressWarnings("null")
	@Override
	public void renderElement(Graphics2D g2d, GuildStatisticsElement element, int width, int height) {
		List<GraphiteStatistic> stats = element.getSettings().getStatistics();
		LineChartSettings settings = (LineChartSettings) element.getSettings();
		
		StatisticsElementTimeframe timeframe = settings.getTimeframe();
		
		List<PartialStatistics> pStats = new ArrayList<>();
		for(GraphiteStatistic s : stats) {
			pStats.addAll(Graphite.getStatistics().getPartialStatistics(element.getGuild(), s, timeframe, settings.getPointFrequency(), stats.indexOf(s) / (double) stats.size(), element.isPreviewMode()));
		}
		
		int labelSpace = 30;
		int circleSize = 20;
		int spaceLeft = 60;
		int spaceRight = 10;

		g2d.setFont(StatisticsRenderer.DEFAULT_FONT);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		int lines = 1;
		int lineW = 0;
		for(PartialStatistics p : pStats) {
			String label = label(element.getGuild(), p);
			Label lbl = Label.parse(g2d, label, EMOJI_SIZE);
			int labelWidth = (int) lbl.getBounds().getWidth() + 10 + circleSize + 10;
			lineW += labelWidth;
			if(lineW > width - spaceRight - spaceLeft) {
				lineW = labelWidth;
				lines++;
			}
		}
		
		int spaceBottom = 50 + lines * labelSpace;
		int spaceTop = 10;

		int spTB = (labelSpace - circleSize) / 2;
		lineW = 0;
		int line = 0;
		int cIdx = 0;
		for(PartialStatistics p : pStats) {
			String label = label(element.getGuild(), p);
			Label lbl = Label.parse(g2d, label, EMOJI_SIZE);
			int labelWidth = (int) lbl.getBounds().getWidth() + 10 + circleSize + 10;
			lineW += labelWidth;
			if(lineW > width - spaceRight - spaceLeft) {
				lineW = labelWidth;
				line++;
			}
			
			Color c = p.getCategory() == null ? settings.getColor(p.getStatistic()) : StatisticsElementSettings.getUnassignedColor(settings.getColors(), cIdx++);
			g2d.setColor(c);
			g2d.fillOval(spaceLeft + lineW - labelWidth, (int) (height - lines * labelSpace + line * labelSpace + spTB), circleSize, circleSize);
			
			StatisticsRenderer.drawLabel(g2d, lbl, spaceLeft + lineW - labelWidth + circleSize + 10, height - lines * labelSpace + line * labelSpace + labelSpace / 2);
		}
		
		int chartHeight = height - spaceTop - spaceBottom;
		int chartWidth = width - spaceLeft - spaceRight;
		
		boolean fillUnder = settings.isFillBelowCurve();
		int numPoints = timeframe.getAmount(settings.getPointFrequency()); // 4 points per day
		double pointSpacing = (double) chartWidth / numPoints;
		long startTime = System.currentTimeMillis() - timeframe.getRawTimeframe();
		
		int maxValuePos = getMaximum(settings, pStats, 1);
		int maxValueNeg = getMaximum(settings, pStats, -1);
		int maxNormalizedPos = getNormalizedMaximum(settings, pStats, 1);
		int maxNormalizedNeg = getNormalizedMaximum(settings, pStats, -1);
		
		boolean usePositive = maxNormalizedPos >= maxNormalizedNeg; // Determine which direction to draw the 5 (-1 because maximum) lines in
		int maximumNormalized = usePositive ? maxNormalizedPos : maxNormalizedNeg;
		int lineDifference = maximumNormalized / 5;
		
		// Note: Normalized value is always positive and divisible by 5!
		int boundPositive = usePositive ? maxNormalizedPos : ((Math.floorDiv(maxValuePos, lineDifference) + 1) * lineDifference);
		int boundNegative = !usePositive ? -maxNormalizedNeg : (maxValueNeg == 0 ? 0 : ((Math.floorDiv(maxValueNeg, -lineDifference) + 1) * -lineDifference));
		
		int chartRange = boundPositive - boundNegative;
		
		// Indicator lines

		g2d.setStroke(new BasicStroke(1));
		g2d.setColor(new Color(1f, 1f, 1f, 0.5f));
		
		for(int i = lineDifference; i < maxNormalizedPos; i+= lineDifference) {
			int y = height - spaceBottom - (int) (((i - boundNegative) / (double) chartRange) * chartHeight);
			g2d.drawLine(spaceLeft - 10, y, width - spaceRight, y);
		}

		for(int i = lineDifference; i < maxNormalizedNeg; i+= lineDifference) {
			int y = height - spaceBottom - (int) (((-i - boundNegative) / (double) chartRange) * chartHeight);
			g2d.drawLine(spaceLeft - 10, y, width - spaceRight, y);
		}

		g2d.setStroke(new BasicStroke(1.5f));
		g2d.setColor(new Color(1f, 1f, 1f, 1f));

		int zeroY = height - spaceBottom - (int) ((-boundNegative / (double) chartRange) * chartHeight);
		if(boundNegative != 0) g2d.drawLine(spaceLeft - 10, zeroY, width - spaceRight, zeroY);

		StatisticsElementPointFrequency markerFrequency = StatisticsElementPointFrequency.MONTHLY;
		for(int i = 0; i < StatisticsElementPointFrequency.values().length; i++) {
			markerFrequency = StatisticsElementPointFrequency.values()[i];
			if(timeframe.getAmount(markerFrequency) <= 12) break;
		}
		
		// Lines
		g2d.setStroke(new BasicStroke(3));
		List<Long> ts = settings.getPointFrequency().getUTCTimestampsBetween(element.getGuild(), startTime, System.currentTimeMillis());
		int cIdx2 = 0;
		for(PartialStatistics p : pStats) {
			int idx = pStats.indexOf(p);

			Color c = p.getCategory() == null ? settings.getColor(p.getStatistic()) : StatisticsElementSettings.getUnassignedColor(settings.getColors(), cIdx2++);
			
			g2d.setStroke(new BasicStroke(2));
			g2d.setColor(c);
			
			Polygon poly = fillUnder ? new Polygon() : null;
			int lastX = 0, lastY = 0;
			for(int i = 0; i <= numPoints; i++) {
				long time = ts.get(ts.size() - numPoints - 1 + i);
				
				int val;
				if(!settings.isStackLines()) {
					val = p.getValueAt(time);
				}else {
					val = pStats.stream().skip(idx)
						.mapToInt(s2 -> s2.getValueAt(time))
						.filter(v -> GraphiteUtil.simpleSignum(v) == GraphiteUtil.simpleSignum(p.getValueAt(time)))
						.sum();
				}
				
				int pointY = (int) ((val - boundNegative) / (double) chartRange * chartHeight);
				int x = (int) (spaceLeft + i * pointSpacing);
				int y = height - spaceBottom - pointY;
				
				if(fillUnder) {
					if(i == 0) poly.addPoint(x, zeroY);
					poly.addPoint(x, y);
					if(i == numPoints) poly.addPoint(x, zeroY);
				}else {
					if(lastX != 0 && lastY != 0) g2d.drawLine(lastX, lastY, x, y);
					lastX = x;
					lastY = y;
				}
			}
			
			if(fillUnder) g2d.fill(poly);
		}
		
		// x-Axis Labels
		for(int i = 1; i <= numPoints; i++) {
			long time = ts.get(ts.size() - numPoints - 1 + i);
			int x = (int) (spaceLeft + i * pointSpacing);
			if(time == markerFrequency.getCorrespondingUTCTimestamp(element.getGuild(), time)) {
				// x-Axis Labels
				g2d.setColor(Color.WHITE);
				g2d.drawLine(x, height - spaceBottom + 6, x, height - spaceBottom - 6);
				
				String label = label(element.getGuild(), time, markerFrequency);
				Rectangle2D r = g2d.getFontMetrics().getStringBounds(label, g2d);
				g2d.drawString(label, (int) Math.min(x - r.getCenterX(), width - r.getWidth()), (int) (height - spaceBottom + r.getHeight()));
			}
		}

		// X- & Y- Axis
		g2d.setColor(Color.WHITE);
		g2d.drawLine(spaceLeft, spaceTop, spaceLeft, height - spaceBottom);
		g2d.drawLine(spaceLeft, height - spaceBottom, width - spaceRight, height - spaceBottom);
		
		// y-Axis Labels
		g2d.setColor(Color.WHITE);
		
		for(int i = lineDifference; i < maxNormalizedPos; i+= lineDifference) {
			String label = shortenLabel(i);
			Rectangle2D r = g2d.getFontMetrics().getStringBounds(label, g2d);
			int y = height - spaceBottom - (int) (((i - boundNegative) / (double) chartRange) * chartHeight) - (int) r.getCenterY();
			
			g2d.drawString(label, 5, y);
		}

		for(int i = lineDifference; i < maxNormalizedNeg; i+= lineDifference) {
			String label = shortenLabel(-i);
			Rectangle2D r = g2d.getFontMetrics().getStringBounds(label, g2d);
			int y = height - spaceBottom - (int) (((-i - boundNegative) / (double) chartRange) * chartHeight) - (int) r.getCenterY();
			
			g2d.drawString(label, 5, y);
		}
	}
	
	private int getMaximum(LineChartSettings settings, List<PartialStatistics> pStats, int sign) {
		if(!settings.isStackLines()) {
			return pStats.stream()
					.mapToInt(p -> p.maxValue(sign))
					.map(v -> Math.abs(v))
					.max().orElse(0) * sign; // TODO: Fix stacked charts always using maximum of the values, regardless of whether the maxima are in the same place
		}else {
			return pStats.stream()
					.mapToInt(p -> p.maxValue(sign))
					.sum();
		}
	}
	
	private int getNormalizedMaximum(LineChartSettings settings, List<PartialStatistics> pStats, int sign) {
		int maxValue = getMaximum(settings, pStats, sign);
		
		maxValue *= sign; // To invert negative numbers
		
		if(maxValue == 0) return 0;
		
		int scaleThing = (int) Math.max(10, Math.pow(sign == -1.0f ? 0 : 10, Math.floor(Math.log10(maxValue))) / 2); // Calculate "ballpark" power of ten and then "double the precision"
		return Math.floorDiv(maxValue, scaleThing) * scaleThing + scaleThing;
	}
	
	private String label(GraphiteGuild guild, long timestamp, StatisticsElementPointFrequency pointFrequency) {
		LocalDate d = Instant.ofEpochMilli(timestamp).atZone(guild.getConfig().getTimezone()).toLocalDate();
		switch(pointFrequency) {
			case DAILY:
			case WEEKLY:
				return d.getMonth().getDisplayName(TextStyle.SHORT, Locale.US) + " " + d.getDayOfMonth();
			case MONTHLY:
				return d.getMonth().getDisplayName(TextStyle.SHORT, Locale.US);
			default:
				throw new UnsupportedOperationException("Not implemented");
		}
	}
	
	private String shortenLabel(int value) {
		if(Math.abs(value) >= 1000000) {
			return NUMBER_FORMAT.format(new BigDecimal(value).setScale(2).divide(new BigDecimal(1000000), RoundingMode.HALF_UP)) + "M";
		}
		
		if(Math.abs(value) >= 1000) {
			return NUMBER_FORMAT.format(new BigDecimal(value).setScale(2).divide(new BigDecimal(1000), RoundingMode.HALF_UP)) + "k";
		}
		
		return String.valueOf(value);
	}
	
	private String label(GraphiteGuild guild, PartialStatistics p) {
		return p.getCategory() == null ? p.getStatistic().getFriendlyName(guild) : p.getCategory();
	}
	
}
