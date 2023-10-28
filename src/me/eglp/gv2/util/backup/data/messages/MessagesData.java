package me.eglp.gv2.util.backup.data.messages;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.receive.ReadonlyMessage;
import club.minnced.discord.webhook.send.WebhookMessage;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.queue.QueueTask;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.SerializationOption;
import me.mrletsplay.mrcore.misc.FriendlyException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel.AutoArchiveDuration;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class MessagesData implements JSONConvertible {

	private Map<String, List<BackupMessage>> messageHistory;
//	private Map<String, List<BackupForumPost>> forumPosts;

	@JSONConstructor
	private MessagesData() {
		this.messageHistory = new HashMap<>();
	}

	public MessagesData(GraphiteGuild guild, int messageCount) {
		this();

		List<GraphiteGuildMessageChannel> channels = new ArrayList<>();
		channels.addAll(guild.getTextChannels());
		channels.addAll(guild.getNewsChannels());
		channels.addAll(guild.getVoiceChannels());
		channels.addAll(guild.getStageChannels());
		channels.forEach(t -> {
			List<BackupMessage> ms = retrieveHistory(t.getJDAChannel(), messageCount);
			if(ms.isEmpty()) return;
			messageHistory.put(t.getID(), ms);
		});

		guild.getJDAGuild().getThreadChannels().forEach(t -> {
			List<BackupMessage> ms = retrieveHistory(t, messageCount);
			if(ms.isEmpty()) return;
			messageHistory.put(t.getId(), ms);
		});
	}

	private List<BackupMessage> retrieveHistory(GuildMessageChannel channel, int messageCount) {
		List<BackupMessage> ms = new ArrayList<>(channel.getHistory().retrievePast(messageCount).complete().stream().map(BackupMessage::new).toList());
		if(ms.isEmpty()) return ms;
		Collections.reverse(ms);
		return ms;
	}

	public void storeMessageHistory(String channelGraphiteID, List<Message> messages) {
		messageHistory.put(channelGraphiteID, messages.stream().map(BackupMessage::new).collect(Collectors.toList()));
	}

	public List<BackupMessage> getMessageHistory(String channelGraphiteID) {
		return messageHistory.getOrDefault(channelGraphiteID, Collections.emptyList());
	}

	public Map<String, List<BackupMessage>> getMessageHistory() {
		return messageHistory;
	}

	@Override
	public void preSerialize(JSONObject object) {
		messageHistory.forEach((id, history) -> {
			object.put(id, new JSONArray(history.stream().map(m -> m.toJSON(SerializationOption.DONT_INCLUDE_CLASS)).collect(Collectors.toList())));
		});
	}

	@Override
	public void preDeserialize(JSONObject object) {
		object.forEach((id, history) -> {
			messageHistory.put(id, ((JSONArray) history).stream().map(o -> JSONConverter.decodeObject((JSONObject) o, BackupMessage.class)).collect(Collectors.toList()));
		});
	}

	public byte[] getEncrypted(SecretKey aesKey) {
		String json = toJSON(SerializationOption.DONT_INCLUDE_CLASS).toString();

		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, aesKey);

			return cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));
		}catch(NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			Graphite.log("Encryption error");
			GraphiteDebug.log(DebugCategory.BACKUP, e);
			throw new FriendlyException("Failed to encrypt messages");
		}
	}

	public void restore(GraphiteGuild guild, IDMappings mappings) {
		List<WebhookClient> webhookClients = new ArrayList<>();
		List<Webhook> webhooks = new ArrayList<>();

		record RestoreFuture(GuildMessageChannel channel, BackupMessage message, CompletableFuture<ReadonlyMessage> future) {}

		List<RestoreFuture> futures = new ArrayList<>();

		messageHistory.forEach((channelID, history) -> {
			String newChannelID = mappings.getNewID(channelID);
			if(newChannelID == null) return; // Might be a thread channel which has not been restored yet, will restore later

			GuildMessageChannel t = guild.getGuildMessageChannelByID(newChannelID).getJDAChannel();
			if(t instanceof ThreadChannel || !(t instanceof IWebhookContainer)) return; // Can't restore messages FIXME: use another method

			Webhook hook = ((IWebhookContainer) t).createWebhook("gr_" + System.currentTimeMillis())
					.reason("Restore fancy chat history")
					.complete();
			webhooks.add(hook);

			WebhookClient cl = new WebhookClientBuilder(hook.getUrl())
					.setWait(true)
					.build();
			webhookClients.add(cl);

			for(BackupMessage m : history) {
				WebhookMessage msg = m.createMessage();
				if(msg == null) continue;
				futures.add(new RestoreFuture(t, m, cl.send(msg)));
			}
		});

		try {
			for(RestoreFuture f : futures) {
				try {
					if(QueueTask.isCurrentCancelled()) {
						f.future.cancel(true);
						continue; // Cancel all other futures as well
					}

					ReadonlyMessage msg = f.future.get();
					if(f.message.getStartedThread() != null) {
						Message jdaMsg = f.channel.retrieveMessageById(msg.getId()).complete();
						BackupThread thread = f.message.getStartedThread();

						ThreadChannel ch = jdaMsg.createThreadChannel(thread.getName())
							.setAutoArchiveDuration(AutoArchiveDuration.valueOf(thread.getAutoArchiveDuration()))
							.complete(); // Invitable is not applicable here, as this can never be a private thread

						ch.getManager()
							.setLocked(thread.isLocked())
							.setArchived(thread.isArchived())
							.setSlowmode(thread.getSlowmode())
							.complete();

						mappings.put(thread.getID(), ch.getId());
					}
				}catch(ExecutionException ignore) {}
			}
		}catch(InterruptedException e) {
			throw new FriendlyException("Failed to restore chat history", e);
		}finally {
			// Always make sure to remove webhooks, even when terminating forcefully
			webhookClients.forEach(WebhookClient::close);
			webhooks.forEach(w -> w.delete().queue());
		}
	}

	public static MessagesData load(String json) {
		return JSONConverter.decodeObject(new JSONObject(json), MessagesData.class);
	}

}
