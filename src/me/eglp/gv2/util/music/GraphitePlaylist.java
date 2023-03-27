package me.eglp.gv2.util.music;

import java.util.List;

import me.eglp.gv2.util.base.GraphiteMusical;

public class GraphitePlaylist {

	private GraphiteMusical owner;
	private String name;
	private List<GraphiteTrack> tracks;
	
	public GraphitePlaylist(GraphiteMusical owner, String id, List<GraphiteTrack> tracks) {
		this.owner = owner;
		this.name = id;
		this.tracks = tracks;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String newName) {
		owner.getMusic().renamePlaylist(name, newName);
		this.name = newName;
	}
	
	public List<GraphiteTrack> getTracks() {
		return tracks;
	}
	
	public GraphiteMusical getOwner() {
		return owner;
	}

	public void delete() {
		owner.getMusic().deletePlaylist(this);
	}
	
}
