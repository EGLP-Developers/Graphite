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
import club.minnced.discord.webhook.send.WebhookMessage;
import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
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

public class MessagesData implements JSONConvertible {

	private Map<String, List<BackupMessage>> messageHistory;
	
	@JSONConstructor
	private MessagesData() {
		this.messageHistory = new HashMap<>();
	}
	
	public MessagesData(GraphiteGuild guild, int messageCount) {
		this();
		guild.getTextChannels().forEach(t -> {
			List<BackupMessage> ms = new ArrayList<>(t.getJDAChannel().getHistory().retrievePast(messageCount).complete().stream().map(BackupMessage::new).collect(Collectors.toList()));
			if(ms.isEmpty()) return;
			Collections.reverse(ms);
			messageHistory.put(t.getID(), ms);
		});
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
		List<CompletableFuture<?>> futures = new ArrayList<>();
		
		messageHistory.forEach((channelID, history) -> {
			String newChannelID = mappings.getNewID(channelID);
			GraphiteTextChannel t = guild.getTextChannelByID(newChannelID);
			Webhook hook = t.getJDAChannel().createWebhook("gr_" + System.currentTimeMillis())
					.reason("Restore fancy chat history")
					.complete();
			webhooks.add(hook);
			
			WebhookClient cl = new WebhookClientBuilder(hook.getUrl())
					.setWait(false)
					.build();
			webhookClients.add(cl);
			
			for(BackupMessage m : history) {
				WebhookMessage msg = m.createMessage();
				if(msg == null) continue;
				futures.add(cl.send(msg));
			}
		});
		
		try {
			for(CompletableFuture<?> f : futures) {
				try {
					if(QueueTask.isCurrentCancelled()) {
						f.cancel(true);
						continue; // Cancel all other futures as well
					}
					
					f.get();
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
