package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.command.text.argument.CommandArgument;
import me.eglp.gv2.util.command.text.argument.MentionArgument;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.scripting.ScriptExecutionException;

public class JSCommandArgument {
	
	private GraphiteLocalizable localizable;
	private CommandArgument arg;
	private int idx;
	
	public JSCommandArgument(GraphiteLocalizable localizable, CommandArgument arg, int idx) {
		this.localizable = localizable;
		this.arg = arg;
		this.idx = idx;
	}
	
	/**
	 * Returns whether this argument represents a valid boolean value
	 * @return Whether this argument represents a valid boolean value
	 * @see #asBoolean()
	 */
	public boolean isBoolean() {
		return arg.isBoolean(localizable);
	}
	
	/**
	 * Returns this argument interpreted as a valid boolean value
	 * @return This argument interpreted as a valid boolean value
	 * @throws ScriptExecutionException If this argument does not represent a valid boolean value
	 * @see #isBoolean()
	 */
	public boolean asBoolean() throws ScriptExecutionException {
		if(!isBoolean()) wrongType("boolean");
		return arg.asBoolean(localizable);
	}
	
	/**
	 * Returns whether this argument is a valid double value
	 * @return Whether this argument is a valid double value
	 * @see #asDouble()
	 */
	public boolean isDouble() {
		return arg.isDouble();
	}
	
	/**
	 * Returns this argument interpreted as a valid double value
	 * @return This argument interpreted as a valid double value
	 * @throws ScriptExecutionException If this argument does not represent a valid double value
	 * @see #isDouble()
	 */
	public double asDouble() throws ScriptExecutionException {
		if(!arg.isDouble()) wrongType("double");
		return arg.asDouble();
	}

	/**
	 * Returns whether this argument is a valid integer value
	 * @return Whether this argument is a valid integer value
	 * @see #asInt()
	 */
	public boolean isInt() {
		return arg.isInt();
	}

	/**
	 * Returns this argument interpreted as a valid integer value
	 * @return This argument interpreted as a valid integer value
	 * @throws ScriptExecutionException If this argument does not represent a valid integer value
	 * @see #isInt()
	 */
	public int asInt() throws ScriptExecutionException {
		if(!arg.isInt()) wrongType("integer");
		return arg.asInt();
	}

	/**
	 * Returns whether this argument is a valid long value
	 * @return Whether this argument is a valid long value
	 * @see #asLong()
	 */
	public boolean isLong() {
		return arg.isLong();
	}
	
	/**
	 * Returns this argument interpreted as a valid long value
	 * @return This argument interpreted as a valid long value
	 * @throws ScriptExecutionException If this argument does not represent a valid long value
	 * @see #isLong()
	 */
	public long asLong() throws ScriptExecutionException {
		if(!arg.isLong()) wrongType("long");
		return arg.asLong();
	}
	
	/**
	 * Returns the raw text of this argument, e.g. textual representations of mentions
	 * @return The raw text of this argument
	 */
	public String getRaw() {
		return arg.getRaw();
	}

	/**
	 * Returns whether this argument is a valid mention
	 * @return Whether this argument is a valid mention
	 * @see #asMention()
	 */
	public boolean isMention() {
		return arg.asMention() != null;
	}
	
	/**
	 * Returns this argument interpreted as a valid mention
	 * @return This argument interpreted as a valid mention
	 * @throws ScriptExecutionException If this argument does not represent a valid mention
	 * @see #isMention()
	 */
	@SuppressWarnings("null")
	public JSMention asMention() throws ScriptExecutionException {
		MentionArgument m = arg.asMention();
		if(m == null || !m.isValid()) wrongType("mention");
		return new JSMention(m.getMention());
	}
	
	private void wrongType(String requiredType) {
		throw new ScriptExecutionException(DefaultLocaleString.ERROR_SCRIPT_INVALID_ARG_TYPE.getFor(localizable, "index", String.valueOf(idx), "type", requiredType));
	}
	
	@Override
	public String toString() {
		return "[JS Command Argument: " + arg.getRaw() + "]";
	}

}
