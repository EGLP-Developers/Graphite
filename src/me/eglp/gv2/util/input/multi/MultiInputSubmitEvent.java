package me.eglp.gv2.util.input.multi;

import java.util.List;

public class MultiInputSubmitEvent {

	private MultiInput input;
	private ButtonPressedEvent buttonEvent;
	
	public MultiInputSubmitEvent(MultiInput input, ButtonPressedEvent buttonEvent) {
		this.input = input;
		this.buttonEvent = buttonEvent;
	}

	public MultiInput getInput() {
		return input;
	}
	
	public ButtonPressedEvent getButtonEvent() {
		return buttonEvent;
	}
	
	public List<String> getSelectMenuValues(String id) {
		return input.getSelectMenuValues(id);
	}

}
