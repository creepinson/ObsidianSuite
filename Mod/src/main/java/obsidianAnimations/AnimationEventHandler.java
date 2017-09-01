package obsidianAnimations;

import obsidianAPI.event.AnimationEvent;
import obsidianAPI.event.AnimationEvent.AnimationEventType;
import obsidianAPI.event.AnimationEventListener;
import obsidianAnimations.entity.saiga.EntitySaiga;

public class AnimationEventHandler {
	
	@AnimationEventListener(type = AnimationEventType.END, entityName = "Saiga", animationName = "Call")
	public void onSaigaCallEnd(AnimationEvent event) {
		((EntitySaiga) event.entity).setCalling(false);
	}
	
}