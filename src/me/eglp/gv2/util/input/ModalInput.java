package me.eglp.gv2.util.input;

import me.eglp.gv2.util.event.AnnotationEventHandler;
import me.eglp.gv2.util.event.EventHandler;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class ModalInput implements GraphiteInput, AnnotationEventHandler {
	
	@EventHandler
	public void onModal(ModalInteractionEvent event) {
		
	}

	@Override
	public void remove() {
		
	}

}
