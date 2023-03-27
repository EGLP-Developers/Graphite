package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.util.base.guild.customcommand.CustomCommandInvokedEvent;
import me.eglp.gv2.util.scripting.ScriptExecutionException;

public class JSCommandInvokedEvent {
	
	private CustomCommandInvokedEvent event;
	private String data;
	
	public JSCommandInvokedEvent(CustomCommandInvokedEvent event, String data) {
		this.event = event;
		this.data = data;
	}
	
//	/** TODO: Scripting API rework
//	 * Returns the command arguments the user passed in when executing the script<br>
//	 * The raw arguments can contain spaces, as valid types (such as mentions) will be automatically detected and interpreted as a single argument
//	 * @return The command arguments the user passed in when executing the script
//	 * @see JSCommandArgument
//	 */
//	public Scriptable getArgs() {
//		AtomicInteger i = new AtomicInteger(0);
//		return GraphiteScript.createJSArray(Arrays.stream(event.getCommandEvent().getArgs())
//				.skip(skipArgs)
//				.map(a -> new JSCommandArgument(event.getGuild(), a, i.getAndIncrement()))
//				.toArray(JSCommandArgument[]::new));
//	}
	
	/**
	 * Returns the data that was passed to the script, or null if no data was passed.
	 * @return The data that was passed to the script
	 */
	public String getData() {
		return data;
	}
	
	/**
	 * Returns the user who sent the command
	 * @return The user who sent the command
	 */
	public JSUser getAuthor() {
		return new JSUser(event.getCommandEvent().getAuthor());
	}
	
	/**
	 * Returns the channel the command was invoked in
	 * @return The channel the command was invoked in
	 */
	public JSMessageChannel getChannel() {
		return event.getCommandEvent().isFromGuild() ?
				new JSTextChannel(event.getCommandEvent().getTextChannel()) :
				new JSPrivateChannel(event.getCommandEvent().getPrivateChannel());
	}
	
	/**
	 * Returns the guild the command was invoked on
	 * @return The guild the command was invoked on
	 * @throws ScriptExecutionException If the command wasn't invoked on a guild
	 */
	public JSGuild getGuild() throws ScriptExecutionException {
		if(!event.getCommandEvent().isFromGuild()) throw new ScriptExecutionException("Event wasn't triggered on a guild");
		return new JSGuild(event.getGuild());
	}
	
//	/** TODO: Scripting API rework
//	 * Tells Graphite how many arguments the script expects (at least)<br>
//	 * This method will throw an exception if the length of {@link #getArgs()} is less than the required amount
//	 * @param count The amount of arguments required
//	 * @throws ScriptExecutionException If the amount of arguments is less than the required amount
//	 */
//	public void requireArgs(int count) throws ScriptExecutionException {
//		if(event.getCommandEvent().getArgs().length - skipArgs < count) throw new ScriptExecutionException("Script requires (at least) " + count + " arg(s)");
//	}
	
	@Override
	public String toString() {
		return "[JS Command Event]";
	}

}
