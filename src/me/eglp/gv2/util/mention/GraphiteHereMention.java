package me.eglp.gv2.util.mention;

public class GraphiteHereMention extends GraphiteMention {

	public GraphiteHereMention() {
		super(MentionType.HERE);
	}
	
	@Override
	public boolean isAmbiguous() {
		return false;
	}
	
	@Override
	public boolean isValid() {
		return true;
	}

}
