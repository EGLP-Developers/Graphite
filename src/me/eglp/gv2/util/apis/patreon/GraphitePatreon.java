package me.eglp.gv2.util.apis.patreon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.patreon.PatreonAPI;
import com.patreon.resources.Pledge;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.main.task.GraphiteTask;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.multiplex.bot.GlobalBot;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.mysql.SQLTable;
import me.eglp.gv2.util.premium.PremiumLevel;
import me.eglp.gv2.util.settings.PatreonSettings;
import me.mrletsplay.mrcore.http.HttpRequest;
import me.mrletsplay.mrcore.http.HttpUtils;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "global_patreon",
	columns = {
		"RefreshToken varchar(255) NOT NULL"
	}
)
@SQLTable(
	name = "global_patreon_pledges",
	columns = {
		"PledgeId varchar(255) NOT NULL",
		"PatronId varchar(255) NOT NULL",
		"PatronFullName TEXT NOT NULL",
		"PatronDiscordId varchar(255) NOT NULL",
		"RewardId varchar(255) NOT NULL",
		"PremiumLevel varchar(255) NOT NULL",
		"Amount int NOT NULL",
		"Currency varchar(255) NOT NULL",
		"PRIMARY KEY (PledgeId)"
	}
)
public class GraphitePatreon {
	
	private static final String REFRESH_TOKEN_URL = "https://www.patreon.com/api/oauth2/token?grant_type=refresh_token";
	
	private static final DecimalFormatSymbols FORMAT_SYMBOLS;
	private static final DecimalFormat FORMAT;
	
	static {
		FORMAT_SYMBOLS = new DecimalFormatSymbols();
		FORMAT_SYMBOLS.setDecimalSeparator('.');
		FORMAT = new DecimalFormat("#0.00", FORMAT_SYMBOLS);
	}
	
	private PatreonAPI patreonAPI;
	private GraphiteTask task;
	
	public GraphitePatreon() {
		PatreonSettings settings = Graphite.getMainBotInfo().getPatreon();
		
		String tok = Graphite.getMySQL().query(String.class, null, "SELECT RefreshToken FROM global_patreon")
				.orElseThrowOther(e -> new FriendlyException("Failed to load Patreon refresh token from MySQL"));
		if(tok == null) throw new IllegalStateException("Patreon token is missing. Please change it and restart");
		
		JSONObject q = HttpRequest.createPost(REFRESH_TOKEN_URL
				+ "&refresh_token=" + HttpUtils.urlEncode(tok)
				+ "&client_id=" + HttpUtils.urlEncode(settings.getClientID())
				+ "&client_secret=" + HttpUtils.urlEncode(settings.getClientSecret()))
				.execute().asJSONObject();
		Graphite.getMySQL().query("DELETE FROM global_patreon WHERE RefreshToken = ?", tok);
		Graphite.getMySQL().query("INSERT INTO global_patreon(RefreshToken) VALUES(?)", q.getString("refresh_token"));
		patreonAPI = new PatreonAPI(q.getString("access_token"));
		
		task = Graphite.getScheduler().scheduleAtFixedRate("patreon-refresh-pledges", this::refreshPledges, 1000 * 60 * 5);
	}
	
	public GraphiteTask getTask() {
		return task;
	}
	
	private void refreshPledges() {
		try {
			PatreonSettings settings = Graphite.getMainBotInfo().getPatreon();
			
			GraphiteMultiplex.setCurrentBot(GlobalBot.INSTANCE);
			
			List<Pledge> newPledges = new ArrayList<>(patreonAPI.fetchAllPledges(settings.getCampaignID())).stream().filter(p -> {
				return p.getReward() != null
						&& p.getDeclinedSince() == null
						&& p.getPatron().getSocialConnections().getDiscord() != null
						&& p.getPatron().getSocialConnections().getDiscord().getUser_id() != null;
			}).collect(Collectors.toList());
			
			List<PatreonPledge>
				addedPledges = new ArrayList<>(),
				removedPledges = new ArrayList<>();
			
			Map<PatreonPledge, PatreonPledge> changedPledges = new HashMap<>();
			
			newPledges.forEach(pledge -> {
				PatreonPledge oldPledge = getPledge(pledge.getId()); // Look up existing pledge from MySQL
				
				if(oldPledge == null) {
					// New pledge
					PatreonPledge pl = PatreonPledge.of(this, pledge);
					addedPledges.add(pl);
					saveOrUpdatePledge(pl);
				}else if(!oldPledge.getRewardID().equals(pledge.getReward().getId())) {
					// Updated pledge
					PatreonPledge pl = PatreonPledge.of(this, pledge);
					changedPledges.put(oldPledge, pl);
					saveOrUpdatePledge(pl);
				}
			});
			
			getPledges().forEach(pledge -> {
				Pledge newPledge = newPledges.stream()
						.filter(pl -> pl.getId().equals(pledge.getID()))
						.findFirst().orElse(null);
				
				if(newPledge == null) {
					// Removed pledge
					removedPledges.add(pledge);
					removePledge(pledge.getID());
				}
			});
			
			addedPledges.forEach(this::onPledgeAdded);
			removedPledges.forEach(this::onPledgeRemoved);
			changedPledges.forEach(this::onPledgeChanged);
		} catch(Exception e) {
			GraphiteDebug.log(DebugCategory.PATREON, e);
		}
	}
	
	private void onPledgeAdded(PatreonPledge pledge) {
		Graphite.getPremium().updateKeys(pledge, pledge.getPatron().getDiscordUser());
		DefaultMessage.PATREON_PLEDGE_ADDED.sendMessage(pledge.getPatron().getDiscordUser().openPrivateChannel(),
				"rank", pledge.getPremiumLevel().getFriendlyName().getFor(pledge.getPatron().getDiscordUser()),
				"price", FORMAT.format(pledge.getAmount() / 100D),
				"currency", pledge.getCurrency(),
				"keys", String.valueOf(pledge.getPremiumLevel().getPatreonUserKeysAmount()));
	}
	
	private void onPledgeRemoved(PatreonPledge pledge) {
		Graphite.getPremium().updateKeys(null, pledge.getPatron().getDiscordUser());
		DefaultMessage.PATREON_PLEDGE_REMOVED.sendMessage(pledge.getPatron().getDiscordUser().openPrivateChannel(),
				"discord_url", Graphite.getMainBotInfo().getLinks().getDiscord());
	}
	
	private void onPledgeChanged(PatreonPledge oldPledge, PatreonPledge pledge) {
		Graphite.getPremium().updateKeys(pledge, pledge.getPatron().getDiscordUser());
		DefaultMessage.PATREON_PLEDGE_CHANGED.sendMessage(pledge.getPatron().getDiscordUser().openPrivateChannel(),
				"old_pledge", oldPledge.getPremiumLevel().getFriendlyName().getFor(pledge.getPatron().getDiscordUser()),
				"new_pledge", pledge.getPremiumLevel().getFriendlyName().getFor(pledge.getPatron().getDiscordUser()),
				"price", FORMAT.format(pledge.getAmount() / 100D),
				"currency", pledge.getCurrency(),
				"keys", String.valueOf(pledge.getPremiumLevel().getPatreonUserKeysAmount()),
				"prefix", Graphite.getBotInfo().getDefaultPrefix());
	}
	
	public GraphitePatron getUser(String discordID) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT PatronId, PatronFullName, PatronDiscordId FROM global_patreon_pledges WHERE PatronDiscordId = ?")) {
				s.setString(1, discordID);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return null;
					return new GraphitePatron(r.getString("PatronId"), r.getString("PatronFullName"), r.getString("PatronDiscordId"));
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load patreon user from MySQL", e));
	}
	
	public GraphitePatron getUser(GraphiteUser user) {
		return getUser(user.getID());
	}
	
	public PatreonPledge getPledgeByPatron(GraphitePatron user) {
		return getPledgeByPatron(user.getPatreonID());
	}
	
	public PatreonPledge getPledgeByPatron(String patronId) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM global_patreon_pledges WHERE PatronId = ?")) {
				s.setString(1, patronId);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return null;
					return getPledge(r);
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load pledge from MySQL", e));
	}
	
	public PatreonPledge getPledge(String id) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM global_patreon_pledges WHERE PledgeId = ?")) {
				s.setString(1, id);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return null;
					return getPledge(r);
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load pledge from MySQL", e));
	}
	
	private PatreonPledge getPledge(ResultSet r) throws SQLException {
		GraphitePatron patron = new GraphitePatron(r.getString("PatronId"), r.getString("PatronFullName"), r.getString("PatronDiscordId"));
		if(patron.getDiscordUser() == null) return null;
		return new PatreonPledge(this, patron, r.getString("PledgeId"), r.getString("RewardId"), PremiumLevel.valueOf(r.getString("PremiumLevel")), r.getInt("Amount"), r.getString("Currency"));
	}
	
	public List<PatreonPledge> getPledges() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM global_patreon_pledges")) {
				try(ResultSet r = s.executeQuery()) {
					List<PatreonPledge> pledges = new ArrayList<>();
					while(r.next()) {
						pledges.add(getPledge(r));
					}
					return pledges;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load pledges from MySQL", e));
	}
	
	public void saveOrUpdatePledge(PatreonPledge pledge) {
		Graphite.getMySQL().query("INSERT INTO global_patreon_pledges(PledgeId, PatronId, PatronFullName, PatronDiscordId, RewardId, PremiumLevel, Amount, Currency) VALUES(?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE PatronId = VALUES(PatronId), PatronFullName = VALUES(PatronFullName), PatronDiscordId = VALUES(PatronDiscordId), RewardId = VALUES(RewardId), PremiumLevel = VALUES(PremiumLevel), Amount = VALUES(Amount), Currency = VALUES(Currency)", // Longest query ever
				pledge.getID(),
				pledge.getPatron().getPatreonID(),
				pledge.getPatron().getFullName(),
				pledge.getPatron().getDiscordUser().getID(),
				pledge.getRewardID(),
				pledge.getPremiumLevel().name(),
				pledge.getAmount(),
				pledge.getCurrency());
	}
	
	public void removePledge(String pledgeID) {
		Graphite.getMySQL().query("DELETE FROM global_patreon_pledges WHERE PledgeId = ?", pledgeID);
	}
	
}
