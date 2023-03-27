package me.eglp.gv2.util.emote.unicode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;

public class GraphiteUnicode {
	
	private static List<UnicodeCodePoint> codePoints;
	
	static {
		codePoints = new ArrayList<>();
		loadEmojis("/include/emoji-sequences.txt");
		loadEmojis("/include/emoji-zwj-sequences.txt");
	}
	
	private static void loadEmojis(String path) {
		try {
			InputStream uIn = Graphite.class.getResourceAsStream(path);
			BufferedReader r = new BufferedReader(new InputStreamReader(uIn));
			String line;
			while((line = r.readLine()) != null) {
				if(line.startsWith("#") || line.trim().isEmpty()) continue;
				
				String spl0 = line.split("#")[0];
				String[] spl = spl0.split(";");
				String codePoint = spl[0].trim();
				String desc = spl[2].trim();
				
				if(codePoint.contains(" ")) {
					codePoints.add(new UnicodeCodePoint(Arrays.stream(codePoint.split(" "))
						.map(c -> new String(Character.toChars(Integer.parseInt(c, 16))))
						.collect(Collectors.joining()), desc));
				}else if(codePoint.contains("..")) {
					String[] bounds = codePoint.split("\\.\\.");
					for(int i = Integer.parseInt(bounds[0], 16); i <= Integer.parseInt(bounds[1], 16); i++) {
						codePoints.add(new UnicodeCodePoint(new String(Character.toChars(i)), desc));
					}
				}else {
					codePoints.add(new UnicodeCodePoint(new String(Character.toChars(Integer.parseInt(codePoint, 16))), desc));
				}
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static List<UnicodeCodePoint> getCodePoints() {
		return codePoints;
	}
	
	public static List<String> getRawCodePoints() {
		return codePoints.stream()
				.map(c -> c.getUnicode())
				.collect(Collectors.toList());
	}
	
}
