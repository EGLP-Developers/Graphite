package me.eglp.gv2.util.game.impl.rpg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.game.GlobalMinigameInstance;
import me.eglp.gv2.util.game.GraphiteMinigame;
import me.eglp.gv2.util.game.impl.rpg.enemy.RPGEnemyType;
import me.eglp.gv2.util.game.output.GameOutput;
import me.eglp.gv2.util.input.GraphiteInput;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.io.IOUtils;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.mrcore.misc.Probability;

@SQLTable(
	name = "global_rpg_data",
	columns = {
		"`Key` varchar(255) NOT NULL",
		"State LONGBLOB NOT NULL",
		"PRIMARY KEY (`Key`)"
	}
)
public class RPG implements GlobalMinigameInstance, JSONConvertible {
	
	public static final RPG INSTANCE;
	
	static {
		if(!Graphite.getScheduler().isShutdown()) {
			INSTANCE = loadGame();
			Graphite.getScheduler().scheduleAtFixedRate("rpg-enemy-respawn", () -> {
				int x = INSTANCE.random.nextInt(RPGMap.MAP_SIZE), y = INSTANCE.random.nextInt(RPGMap.MAP_SIZE);
				RPGLocation l = INSTANCE.map.getLocation(x, y);
				RPGEnemyType t = Probability.chooseValue(l.getType().getEnemyProbabilities(), INSTANCE.random);
				if(t == null) return;
				if(l.getPlayers().isEmpty() && l.getEnemies().isEmpty()) l.addEnemy(t.createEnemy(x, y));
			}, 1000);
	
			Graphite.getScheduler().scheduleWithFixedDelay("rpg-autosave", () -> RPG.INSTANCE.saveGame(), 5 * 60000);
		}else {
			INSTANCE = null;
		}
	}
	
	private Random random;
	
	@JSONValue
	@JSONComplexListType(RPGPlayer.class)
	private List<RPGPlayer> players;
	
	@JSONValue
	private RPGMap map;
	
	@JSONConstructor
	public RPG() {
		this.players = new ArrayList<>();
		this.random = new Random();
		this.map = new RPGMap();
	}
	
	public Random getRandom() {
		return random;
	}
	
	public RPGMap getMap() {
		return map;
	}

	@Override
	public List<GraphiteInput> getActiveInputs() {
		return players.stream().map(RPGPlayer::getInput).collect(Collectors.toList());
	}

	@Override
	public List<GameOutput> getActiveOutputs() {
		return players.stream().map(RPGPlayer::getOutput).collect(Collectors.toList());
	}

	@Override
	public GraphiteMinigame getGame() {
		return GraphiteMinigame.GAME_RPG;
	}
	
	@Override
	public boolean isJoinable() {
		return true;
	}

	@Override
	public void addUser(GraphiteUser user) {
		RPGPlayer p = players.stream().filter(pl -> pl.getUserID().equals(user.getID())).findFirst().orElse(null);
		if(p == null) {
			p = new RPGPlayer(user, RPGMap.MAP_SIZE /  2, RPGMap.MAP_SIZE / 2);
			players.add(p);
			return;
		}
		p.setUser(user);
	}

	public List<RPGPlayer> getPlayers() {
		return players;
	}
	
	@Override
	public List<GraphiteUser> getPlayingUsers() {
		return players.stream().map(RPGPlayer::getUser).filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	public void onUserLeave(GraphiteUser user) {
		players.stream().filter(u -> u.getUserID().equals(user.getID())).forEach(u -> u.setUser(null));
	}
	
	private static RPG loadGame() {
		byte[] in = Graphite.getMySQL().query(byte[].class, null, "SELECT State FROM global_rpg_data")
				.orElseThrowOther(e -> new FriendlyException("Failed to load data from MySQL", e));
		if(in == null || in.length == 0) return new RPG();
		try {
			InputStream inp = new InflaterInputStream(new ByteArrayInputStream(in));
			byte[] b = IOUtils.readAllBytes(inp);
			if(b.length == 0) return new RPG();
			return JSONConverter.decodeObject(new JSONObject(new String(b, StandardCharsets.UTF_8)), RPG.class);
		}catch(IOException e) {
			throw new FriendlyException(e);
		}
	}

	@Override
	public void saveGame() {
		byte[] b = INSTANCE.toJSON().toString().getBytes(StandardCharsets.UTF_8);
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		try {
			DeflaterOutputStream dOut = new DeflaterOutputStream(bOut);
			dOut.write(b);
			dOut.finish();
		}catch(IOException e) {
			throw new FriendlyException("Failed to save RPG", e);
		}
		Graphite.getMySQL().run(con -> {
			try(PreparedStatement stm = con.prepareStatement("INSERT INTO global_rpg_data(`Key`, State) VALUES(?, ?) ON DUPLICATE KEY UPDATE State = VALUES(State)")) {
				stm.setString(1, "TheKey");
				byte[] bs = bOut.toByteArray();
				stm.setBinaryStream(2, new ByteArrayInputStream(bs), bs.length);
				stm.executeUpdate();
			}
		});
	}
	
	public static void saveGlobalGame() {
		if(INSTANCE == null) return;
		INSTANCE.saveGame();
	}
	
}
