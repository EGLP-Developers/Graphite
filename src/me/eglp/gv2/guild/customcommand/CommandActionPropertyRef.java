package me.eglp.gv2.guild.customcommand;

import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class CommandActionPropertyRef implements WebinterfaceObject, JSONConvertible {

	@JSONValue
	@JavaScriptValue(getter = "getForProperty", setter = "setForProperty")
	private String forProperty;

	@JSONValue
	@JavaScriptValue(getter = "getIsArgument", setter = "setIsArgument")
	private boolean isArgument;

	@JSONValue
	@JavaScriptValue(getter = "getArgumentName", setter = "setArgumentName")
	private String argumentName;

	@JSONValue
	@JavaScriptValue(getter = "getValue", setter = "setValue")
	private Object value;

	@JSONConstructor
	@JavaScriptConstructor
	private CommandActionPropertyRef() {}

	private CommandActionPropertyRef(CommandActionProperty forProperty) {
		this.forProperty = forProperty == null ? null : forProperty.getName();
	}

	public CommandActionPropertyRef(CommandActionProperty forProperty, String argumentName) {
		this(forProperty);
		this.argumentName = argumentName;
		this.isArgument = true;
	}

	public CommandActionPropertyRef(CommandActionProperty forProperty, Object value) {
		this(forProperty);
		this.value = value;
	}

	public String getForProperty() {
		return forProperty;
	}

	public boolean isArgument() {
		return isArgument;
	}

	public String getArgumentName() {
		return argumentName;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

}
