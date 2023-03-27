package me.eglp.gv2.util.command.text.argument;

import me.eglp.gv2.util.mention.GraphiteMention;
import me.eglp.gv2.util.mention.MentionFinder.SearchResult;

public class MentionArgument extends CommandArgument {

	private SearchResult searchResult;
	
	public MentionArgument(String raw, SearchResult searchResult) {
		super(raw);
		this.searchResult = searchResult;
	}
	
	public GraphiteMention getMention() {
		return searchResult.getMention();
	}
	
	public SearchResult getSearchResult() {
		return searchResult;
	}
	
	public boolean isValid() {
		return searchResult.getMention().isValid();
	}

}
