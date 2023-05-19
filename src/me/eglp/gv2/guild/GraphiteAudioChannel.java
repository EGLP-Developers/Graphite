package me.eglp.gv2.guild;

import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

public interface GraphiteAudioChannel extends GraphiteGuildChannel {

	public AudioChannel getJDAChannel();

	public default boolean isVoiceChannel() {
		return this instanceof GraphiteVoiceChannel;
	}

	public default boolean isStageChannel() {
		return this instanceof GraphiteStageChannel;
	}

}
