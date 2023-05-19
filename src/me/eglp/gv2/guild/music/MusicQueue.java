package me.eglp.gv2.guild.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.eglp.gv2.util.music.GraphiteTrack;

public class MusicQueue {

	private List<GraphiteTrack> queue;
	private int currentIndex;

	public MusicQueue() {
		this.queue = new ArrayList<>(100);
		this.currentIndex = -1;
	}

	public void add(GraphiteTrack track) {
		queue.add(track);
	}

	public GraphiteTrack removeRelative(int relativeIndex) {
		return removeAbsolute(currentIndex + relativeIndex);
	}

	public GraphiteTrack removeAbsolute(int absoluteIndex) {
		if(absoluteIndex < 0 || absoluteIndex >= queue.size()) return null;
		GraphiteTrack track = queue.remove(absoluteIndex);
		if(absoluteIndex <= currentIndex) currentIndex--;
		return track;
	}

	public void shuffle() {
		List<GraphiteTrack> future = getFutureQueue();
		Collections.shuffle(future);
	}

	public void clear() {
		currentIndex = -1;
		queue.clear();
	}

	public void playFromTop() {
		currentIndex = -1;
	}

	public GraphiteTrack next() {
		return next(1);
	}

	public GraphiteTrack next(int amount) {
		this.currentIndex += amount;

		if(currentIndex >= queue.size()) {
			currentIndex = queue.size();
			return null;
		}

		return queue.get(currentIndex);
	}

	public GraphiteTrack jump(int absoluteIndex) {
		if(queue.size() == 0) {
			currentIndex = -1;
			return null;
		}

		if(absoluteIndex < 0) absoluteIndex = 0;
		if(absoluteIndex >= queue.size()) absoluteIndex = queue.size() - 1;
		this.currentIndex = absoluteIndex - 1;

		return next();
	}

	public GraphiteTrack previous() {
		return previous(1);
	}

	public GraphiteTrack previous(int amount) {
		this.currentIndex -= amount;

		if(currentIndex < 0) {
			currentIndex = -1;
			return null;
		}

		return queue.get(currentIndex);
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public List<GraphiteTrack> getFutureQueue() {
		if(queue.isEmpty()) return queue;
		return queue.subList(currentIndex + 1, queue.size());
	}

	public List<GraphiteTrack> getPastQueue() {
		if(queue.isEmpty()) return queue;
		return queue.subList(0, currentIndex);
	}

	public List<GraphiteTrack> getFullQueue() {
		if(queue.isEmpty()) return queue;
		return queue;
	}

	public boolean hasReachedEnd() {
		return currentIndex >= queue.size();
	}

}
