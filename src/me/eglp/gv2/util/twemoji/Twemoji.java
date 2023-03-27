package me.eglp.gv2.util.twemoji;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import me.eglp.gv2.main.Graphite;
import me.mrletsplay.mrcore.http.HttpRequest;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class Twemoji {
	
	public static String getTwemojiName(String emoji) {
		String raw = emoji.contains("\u200d") ? emoji : emoji.replace("\ufe0f", ""); // Weird Twemoji stuff: https://github.com/twitter/twemoji/issues/419
		return raw.codePoints().mapToObj(cp -> Integer.toHexString(cp)).collect(Collectors.joining("-"));
	}
	
	private static void downloadEmoji() {
		Graphite.log("Loading emoji");
		File emojiFolder = Graphite.getFileManager().getEmojiFolder();
		File zipFile = new File(emojiFolder, "twemoji.zip");
		try {
			Graphite.log("Downloading");
			HttpRequest.createGet("https://api.github.com/repos/twitter/twemoji/zipball/master").execute().transferTo(zipFile);
			Graphite.log("Extracting");
			
			try(FileSystem fs = FileSystems.newFileSystem(zipFile.toPath())) {
				Path root = Files.list(fs.getPath("/")).findFirst().get(); // Parent folder of the repo
				Path pngAssets = fs.getPath(root.toString(), "assets/72x72/");
				Files.walk(pngAssets).forEach(f -> {
					if(Files.isDirectory(f)) return;
					try {
						Files.copy(f, Path.of(emojiFolder.toPath().toString(), pngAssets.relativize(f).toString()));
					} catch (IOException e) {
						throw new FriendlyException(e);
					}
				});
			}
			zipFile.delete();
			Graphite.log("Done");
		} catch (IOException e) {
			throw new FriendlyException(e);
		}
	}
	
	public static BufferedImage loadEmoji(String unicode) {
		File emojiFolder = Graphite.getFileManager().getEmojiFolder();
		if(!emojiFolder.exists()) downloadEmoji();
		File emojiFile = new File(emojiFolder, getTwemojiName(unicode) + ".png");
		if(!emojiFile.exists()) return null;
		try {
			return ImageIO.read(emojiFile);
		} catch (IOException e) {
			throw new FriendlyException(e);
		}
	}

}
