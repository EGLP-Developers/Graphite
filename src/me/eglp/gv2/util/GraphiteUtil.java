package me.eglp.gv2.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import me.eglp.gv2.util.emote.unicode.GraphiteUnicode;
import me.eglp.gv2.util.emote.unicode.UnicodeCodePoint;

public class GraphiteUtil {
	
	// Source: https://sashamaps.net/docs/resources/20-colors/
	private static final List<Color> DISTINCT_COLORS = Collections.unmodifiableList(Arrays.asList(
			Color.decode("#e6194B"), // Red
			Color.decode("#f58231"), // Orange
			Color.decode("#ffe119"), // Yellow
//			Color.decode("#bfef45"), // Lime
			Color.decode("#3cb44b"), // Green
			Color.decode("#42d4f4"), // Cyan
			Color.decode("#4363d8"), // Blue
//			Color.decode("#911eb4"), // Purple
			Color.decode("#f032e6"), // Magenta
			Color.decode("#a9a9a9"), // Grey
			Color.decode("#800000"), // Maroon
			Color.decode("#9A6324"), // Brown
//			Color.decode("#808000"), // Olive
			Color.decode("#469990"), // Teal
			Color.decode("#000075"), // Navy
			Color.decode("#000000"), // Black
			Color.decode("#fabed4"), // Pink
//			Color.decode("#ffd8b1"), // Apricot
			Color.decode("#fffac8"), // Beige
			Color.decode("#aaffc3"), // Mint
			Color.decode("#dcbeff"), // Lavender
			Color.decode("#ffffff")  // White
		));
	
	public static final Pattern CUSTOM_EMOJI_PATTERN = Pattern.compile("<(?<a>a?):(?<name>.+?):(?<id>\\d+?)>");
	
	public static String randomShortID() {
		return Long.toHexString(System.nanoTime());
	}
	
	public static List<Color> getDistinctColors() {
		return DISTINCT_COLORS;
	}
	
	public static Color getDistinctColor(int index) {
		return DISTINCT_COLORS.get(index % DISTINCT_COLORS.size());
	}
	
	public static List<UnicodeCodePoint> extractUnicodeEmoji(String text) {
		List<UnicodeCodePoint> codePoints = new ArrayList<>();
		
		int idx = 0;
		while(idx < text.length()) {
			final int idx2 = idx;
			UnicodeCodePoint em = GraphiteUnicode.getCodePoints().stream()
					.filter(e -> text.startsWith(e.getUnicode(), idx2))
					.max(Comparator.comparingInt(e -> e.getUnicode().length())).orElse(null);
			if(em != null) {
				idx += em.getUnicode().length();
				codePoints.add(em);
			}else {
				idx++;
			}
		}
		
		return codePoints;
	}
	
	public static String truncateToLength(String string, int maxLength, boolean dots) {
		return string.length() > maxLength ?
				string.substring(0, dots ? maxLength - 3 : maxLength) + (dots ? "..." : "")
				: string;
	}
	
	public static int simpleSignum(int val) {
		return val >= 0 ? 1 : -1;
	}
	
}
