package me.eglp.gv2.util.stats.element.bar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.TextStyle;
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
import me.eglp.gv2.util.stats.element.StatisticsElementTimeframe;
import me.eglp.gv2.util.stats.element.StatisticsRenderer;

public class BarChartRenderer implements StatisticsRenderer {
	
	public static final BarChartRenderer INSTANCE = new BarChartRenderer();
	
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###.#");
	
	@Override
	public void renderElement(Graphics2D g2d, GuildStatisticsElement element, int width, int height) {
		List<GraphiteStatistic> stats = element.getSettings().getStatistics();
		GraphiteStatistic s = stats.get(0);
		BarChartSettings settings = (BarChartSettings) element.getSettings();
		
		StatisticsElementTimeframe timeframe = settings.getTimeframe();
		
		int labelSpace = 30;
		int spaceLeft = 60;
		int spaceRight = 10;
		int spaceBottom = 50 + labelSpace;
		int spaceTop = 10;
		
		int chartHeight = height - spaceTop - spaceBottom;
		int chartWidth = width - spaceLeft - spaceRight;
		
		int barCount = timeframe.getAmount(settings.getPointFrequency());
		float totalBarWidth = chartWidth / (float) barCount;
		int barSpace = 10;
		int barWidth = (int) (totalBarWidth - barSpace);
		long startTime = System.currentTimeMillis() - timeframe.getRawTimeframe();
		
		List<PartialStatistics> ss = Graphite.getStatistics().getPartialStatistics(element.getGuild(), s, timeframe, settings.getPointFrequency(), 0, element.isPreviewMode());
		PartialStatistics p = ss.isEmpty() ? null : ss.get(0); // Will be empty for stats with categories when there are no categories in the given timeframe
		
		int maxValuePos = p == null ? 0 : p.maxValue(1);
		int maxValueNeg = p == null ? 0 : p.maxValue(-1);
		int maxNormalizedPos = getNormalizedMaximum(maxValuePos, 1);
		int maxNormalizedNeg = getNormalizedMaximum(maxValueNeg, -1);
		
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

		g2d.setStroke(new BasicStroke(3));
		g2d.setFont(StatisticsRenderer.DEFAULT_FONT);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		List<Long> ts = settings.getPointFrequency().getUTCTimestampsBetween(element.getGuild(), startTime, System.currentTimeMillis());
		for(int i = 0; i < barCount; i++) {
			long time = ts.get(ts.size() - barCount + i);
			int x = (int) (spaceLeft + i * totalBarWidth);
			
			if(p != null) {
				int val = p.getValueAt(time);
				int barHeight = (int) ((val) / (double) chartRange * chartHeight);
				
				// Bars
				g2d.setColor(settings.getBarColor());
				g2d.fillRect((int) (x + barSpace / 2), zeroY - (barHeight < 0 ? 0 : barHeight), barWidth, Math.abs(barHeight));
			}
			
			if(time == markerFrequency.getCorrespondingUTCTimestamp(element.getGuild(), time)) {
				// x-Axis Labels
				g2d.setColor(Color.WHITE);
				g2d.drawLine((int) (x + totalBarWidth / 2), height - spaceBottom + 6, (int) (x + totalBarWidth / 2), height - spaceBottom - 6);
				
				String label = label(element.getGuild(), time, markerFrequency);
				Rectangle2D r = g2d.getFontMetrics().getStringBounds(label, g2d);
				g2d.drawString(label, (int) Math.min(x + totalBarWidth / 2 - r.getCenterX(), width - r.getWidth()), (int) (height - spaceBottom + r.getHeight()));
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
		
		// Label
		int circleSize = 20;
		int spTB = (labelSpace - circleSize) / 2;
		g2d.setColor(settings.getBarColor());
		g2d.fillOval(spaceLeft, height - labelSpace + spTB, circleSize, circleSize);
		
		Label lbl = Label.parse(g2d, p == null || p.getCategory() == null ? s.getFriendlyName(element.getGuild()) : GraphiteUtil.truncateToLength(p.getCategory(), 20, true), StatisticsRenderer.EMOJI_SIZE);
		StatisticsRenderer.drawLabel(g2d, lbl, spaceLeft + 10 + circleSize, height - labelSpace / 2);
	}
	
	private int getNormalizedMaximum(int maxValue, int sign) {
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
		if(value >= 1000000) {
			return NUMBER_FORMAT.format(new BigDecimal(value).setScale(2).divide(new BigDecimal(1000000), RoundingMode.HALF_UP)) + "M";
		}
		
		if(value >= 1000) {
			return NUMBER_FORMAT.format(new BigDecimal(value).setScale(2).divide(new BigDecimal(1000), RoundingMode.HALF_UP)) + "k";
		}
		
		return String.valueOf(value);
	}

}
