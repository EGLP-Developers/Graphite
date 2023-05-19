package me.eglp.gv2.util.mention;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.guild.GraphiteCategory;

public class GraphiteCategoryMention extends GraphiteMention {

	private List<GraphiteCategory> possibleCategories;

	public GraphiteCategoryMention(List<GraphiteCategory> categories) {
		super(MentionType.CATEGORY);
		this.possibleCategories = categories;
	}

	public GraphiteCategoryMention(GraphiteCategory... categories) {
		this(Arrays.asList(categories));
	}

	public List<GraphiteCategory> getPossibleCategories() {
		return possibleCategories;
	}

	public GraphiteCategory getMentionedCategory() {
		if(!isValid() || isAmbiguous()) throw new IllegalStateException("Mention is ambiguous/invalid");
		return possibleCategories.get(0);
	}

	public boolean isAmbiguous() {
		return possibleCategories.size() != 1;
	}

	@Override
	public boolean isValid() {
		return possibleCategories != null && !possibleCategories.isEmpty();
	}

}
