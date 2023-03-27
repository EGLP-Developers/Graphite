package me.eglp.gv2.util.base.guild.music;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.eglp.gv2.util.music.GraphiteTrack;

public class MusicSearchResult {
	
	private List<GraphiteTrack> results;
	private boolean isPlaylist;
	
	public MusicSearchResult(List<GraphiteTrack> results, boolean isPlaylist) {
		this.results = results;
		this.isPlaylist = isPlaylist;
	}
	
	public MusicSearchResult(GraphiteTrack result) {
		this(Arrays.asList(result), false);
	}
	
	public MusicSearchResult() {
		this(Collections.emptyList(), false);
	}
	
	public List<GraphiteTrack> getResults() {
		return results;
	}
	
	public boolean isPlaylist() {
		return isPlaylist;
	}

}
