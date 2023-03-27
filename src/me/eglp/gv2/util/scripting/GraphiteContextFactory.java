package me.eglp.gv2.util.scripting;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

public class GraphiteContextFactory extends ContextFactory {
	
	private static class GraphiteContext extends Context {
		
		private long startTime;
		
		public GraphiteContext(ContextFactory factory) {
			super(factory);
		}

		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}
		
		public long getStartTime() {
			return startTime;
		}
		
	}
	
	@Override
	protected Context makeContext() {
		GraphiteContext c = new GraphiteContext(this);
		c.setOptimizationLevel(-1);
		c.setInstructionObserverThreshold(10);
		c.setMaximumInterpreterStackDepth(1000);
		return c;
	}
	
	protected void observeInstructionCount(Context cx, int instructionCount) {
		GraphiteContext c = (GraphiteContext) cx;
		if(System.currentTimeMillis() - c.getStartTime() > 2500) { // Execution time > 2.5s
			throw new ScriptTimeoutError("Script timed out"); // Throw Error instance to ensure that script will never get control back through catch or finally
		}
	}
	
	@Override
	protected Object doTopCall(Callable callable, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		GraphiteContext c = (GraphiteContext) cx;
		c.setStartTime(System.currentTimeMillis());
		return super.doTopCall(callable, cx, scope, thisObj, args);
	}
	
}
