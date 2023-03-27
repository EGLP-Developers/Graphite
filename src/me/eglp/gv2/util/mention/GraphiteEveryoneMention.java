package me.eglp.gv2.util.mention;

public class GraphiteEveryoneMention extends GraphiteMention {

	public GraphiteEveryoneMention() {
		super(MentionType.EVERYONE);
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
