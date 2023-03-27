package me.eglp.gv2.util.premium;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.apis.patreon.GraphitePatron;
import me.eglp.gv2.util.apis.patreon.PatreonPledge;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.mysql.SQLTable;

@SQLTable(
	name = "global_premium_keys",
	columns = {
		"`Key` varchar(255) NOT NULL",
		"Type varchar(255) DEFAULT NULL",
		"Owner varchar(255) DEFAULT NULL",
		"RedeemedGuild varchar(255) DEFAULT NULL",
		"Level varchar(255) DEFAULT NULL",
		"ExpiresAt bigint DEFAULT NULL",
		"PRIMARY KEY(`Key`)"
	}
)
public class GraphitePremium {

	private List<PremiumKey> premiumKeys;
	
	public GraphitePremium() {
		premiumKeys = Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM global_premium_keys")) {
				try(ResultSet r = s.executeQuery()) {
					List<PremiumKey> keys = new ArrayList<>();
					while(r.next()) {
						PremiumKey key = null;
						
						PremiumKeyType type = PremiumKeyType.valueOf(r.getString("Type"));
						switch(type) {
							case PATREON:
							{
								if(Graphite.getPatreon() == null) continue;
								PatreonPledge pl = Graphite.getPatreon().getPledgeByPatron(r.getString("Owner"));
								if(pl != null) {
									key = new PatreonPremiumKey(r.getString("Key"), pl.getPatron(), pl.getPremiumLevel());
								}
								break;
							}
							case PERMANENT:
							{
								GraphiteUser u = Graphite.getUser(r.getString("Owner"));
								PremiumLevel p = PremiumLevel.valueOf(r.getString("Level"));
								key = new PermanentPremiumKey(r.getString("Key"), u, p);
								break;
							}
							case TEMPORARY:
							{
								GraphiteUser u = Graphite.getUser(r.getString("Owner"));
								PremiumLevel p1 = PremiumLevel.valueOf(r.getString("Level"));
								long expiresAt = r.getLong("ExpiresAt");
								key = new TemporaryPremiumKey(r.getString("Key"), u, p1, expiresAt);
								break;
							}
							default:
								continue;
						}
						
						if(key != null) {
							String guild = r.getString("RedeemedGuild");
							if(guild != null) key.setRedeemedGuild(Graphite.getGuild(guild));
							keys.add(key);
						}
					}
					
					return keys;
				}
			}
		}).orElse(Collections.emptyList());
		
		
		Graphite.getScheduler().scheduleAtFixedRate("premium/key-refresh", () -> {
			for(PremiumKey k : new ArrayList<>(premiumKeys)) {
				if(k.hasExpired()) {
					unsetKey(k);
					premiumKeys.remove(k);
				}
			}
		}, 10000);
	}
	
	public void saveKey(PremiumKey key) {
		Graphite.getMySQL().query("INSERT IGNORE INTO global_premium_keys(`Key`, `Type`, `Owner`, `RedeemedGuild`, `Level`, `ExpiresAt`) VALUES(?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Owner = VALUES(Owner), RedeemedGuild = VALUES(RedeemedGuild)", 
				key.getID(),
				key.getKeyType().name(),
				key.getOwner() != null ? key.getOwner().getID() : null,
				key.getRedeemedGuild() != null ? key.getRedeemedGuild().getID() : null,
				key.getPremiumLevel().name(),
				(key instanceof TemporaryPremiumKey ? ((TemporaryPremiumKey) key).getExpiresAt() : 0));
	}
	
	public void unsetKey(PremiumKey key) {
		Graphite.getMySQL().query("DELETE FROM global_premium_keys WHERE Key = ?", key.getID());
	}
	
	public List<PremiumKey> getPremiumKeys() {
		return premiumKeys;
	}
	
	public List<PremiumKey> getKeys(GraphiteUser owner) {
		return premiumKeys.stream().filter(k -> k.getOwner() != null && k.getOwner().equals(owner)).collect(Collectors.toList());
	}
	
	public void updateKeys(PatreonPledge pledge, GraphiteUser owner) {
		if(pledge == null) {
			premiumKeys.removeIf(k -> {
				boolean b = k.getOwner() != null && k.getOwner().equals(owner);
				if(b) unsetKey(k);
				return b;
			});
		}else {
			PremiumLevel l = pledge.getPremiumLevel();
			List<PatreonPremiumKey> ks = getKeys(owner).stream().filter(k -> k instanceof PatreonPremiumKey).map(k -> (PatreonPremiumKey) k).collect(Collectors.toList());
			while(ks.size() > l.getPatreonUserKeysAmount()) {
				PremiumKey k = ks.remove(ks.size() - 1);
				premiumKeys.remove(k);
				unsetKey(k);
			}
			while(ks.size() < l.getPatreonUserKeysAmount()) {
				ks.add(generatePatreonKey(pledge.getPatron()));
			}
			ks.forEach(k -> {
				k.setPremiumLevel(l);
				if(!premiumKeys.contains(k)) ks.add(k);
				saveKey(k);
			});
		}
	}
	
	public PremiumKey getRedeemedKey(GraphiteGuild guild) {
		return premiumKeys.stream().filter(k -> k.isInUse() && k.getRedeemedGuild().isAvailable() && k.getRedeemedGuild().equals(guild)).findFirst().orElse(null);
	}
	
	public PremiumKey getKey(String id) {
		return premiumKeys.stream().filter(k -> k.getID().equals(id)).findFirst().orElse(null);
	}
	
	public PatreonPremiumKey generatePatreonKey(GraphitePatron owner) {
		PatreonPremiumKey key = new PatreonPremiumKey(UUID.randomUUID().toString(), owner, owner.getPledge().getPremiumLevel());
		premiumKeys.add(key);
		saveKey(key);
		return key;
	}
	
	public PermanentPremiumKey generatePermanentKey(GraphiteUser owner, PremiumLevel level) {
		PermanentPremiumKey key = new PermanentPremiumKey(UUID.randomUUID().toString(), owner, level);
		premiumKeys.add(key);
		saveKey(key);
		return key;
	}
	
	public PermanentPremiumKey generatePermanentKey(PremiumLevel level) {
		PermanentPremiumKey key = new PermanentPremiumKey(UUID.randomUUID().toString(), null, level);
		premiumKeys.add(key);
		saveKey(key);
		return key;
	}
	
	public TemporaryPremiumKey generateTemporaryKey(GraphiteUser owner, PremiumLevel level, long expiresAt) {
		TemporaryPremiumKey key = new TemporaryPremiumKey(UUID.randomUUID().toString(), owner, level, expiresAt);
		premiumKeys.add(key);
		saveKey(key);
		return key;
	}
	
	public TemporaryPremiumKey generateTemporaryKey(PremiumLevel level, long expiresAt) {
		TemporaryPremiumKey key = new TemporaryPremiumKey(UUID.randomUUID().toString(), null, level, expiresAt);
		premiumKeys.add(key);
		saveKey(key);
		return key;
	}
	
}
