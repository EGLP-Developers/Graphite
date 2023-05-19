package me.eglp.gv2.util.stats.element.text;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.stats.StatisticValue;
import me.eglp.gv2.util.stats.element.GuildStatisticsElement;
import me.eglp.gv2.util.stats.element.Label;
import me.eglp.gv2.util.stats.element.StatisticsRenderer;

public class TextRenderer implements StatisticsRenderer {

	public static final TextRenderer INSTANCE = new TextRenderer();

	@Override
	public void renderElement(Graphics2D g2d, GuildStatisticsElement element, int width, int height) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		TextSettings settings = (TextSettings) element.getSettings();

		float fontSize = (float) settings.getFontSize();
		int textSpacing = (int) (fontSize + 10);

		List<StatisticValue> values = settings.getStatistics().stream()
				.flatMap(s -> Graphite.getStatistics().getLastStatisticValues(element.getGuild(), s, element.isPreviewMode(), settings.getStatistics().indexOf(s) + 1, settings.getStatistics().size()).stream())
				.collect(Collectors.toList());

		int boxHeight = values.size() * textSpacing;
		g2d.setFont(StatisticsRenderer.DEFAULT_FONT.deriveFont(fontSize));

		List<Label> labels = values.stream()
				.map(v -> Label.parse(g2d, label(element.getGuild(), v), EMOJI_SIZE))
				.collect(Collectors.toList());

		int maxWidth = labels.stream()
				.mapToInt(l -> (int) l.getBounds().getWidth())
				.max().orElse(0);

		int i = 0;
		for(StatisticValue v : values) {
			Label lbl = labels.get(i);
			StatisticsRenderer.drawLabel(g2d, lbl, 10, height / 2 - boxHeight / 2 + i * textSpacing + textSpacing / 2);

			Rectangle2D r2d1 = g2d.getFontMetrics().getStringBounds(": ", g2d);
			g2d.setColor(Color.WHITE);
			g2d.drawString(": ", 10 + maxWidth, (int) (height / 2 - boxHeight / 2 + i * textSpacing + textSpacing / 2 - r2d1.getCenterY()));

			String valLabel = String.valueOf(v.getValue());
			Rectangle2D r2d2 = g2d.getFontMetrics().getStringBounds(valLabel, g2d);
			g2d.setColor(Color.WHITE);
			g2d.drawString(valLabel, 10 + maxWidth + (int) r2d1.getWidth(), (int) (height / 2 - boxHeight / 2 + i * textSpacing + textSpacing / 2 - r2d2.getCenterY()));
			i++;
		}
	}

	private String label(GraphiteGuild guild, StatisticValue value) {
		return value.getCategory() == null ? value.getStatistic().getFriendlyName(guild) : value.getCategory();
	}

}
