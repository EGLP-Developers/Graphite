
package me.eglp.gv2.util.game.impl.rpg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.game.impl.rpg.dialog.DialogAction;
import me.eglp.gv2.util.game.impl.rpg.enemy.RPGEnemy;
import me.eglp.gv2.util.game.impl.rpg.npc.RPGNPC;
import me.eglp.gv2.util.game.impl.rpg.object.RPGObject;
import me.eglp.gv2.util.game.impl.rpg.object.RPGObjectCategory;
import me.eglp.gv2.util.game.impl.rpg.object.RPGObjectType;
import me.eglp.gv2.util.game.impl.rpg.quest.RPGQuest;
import me.eglp.gv2.util.game.output.MessageOutput;
import me.eglp.gv2.util.game.output.renderer.MessageGraphics;
import me.eglp.gv2.util.input.MessageInput;
import me.eglp.gv2.util.input.SelectInput;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.lang.LocalizedMessage;
import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONListType;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import me.mrletsplay.mrcore.misc.MiscUtils;

public class RPGPlayer implements JSONConvertible {

	private static final int
		INVENTORY_SIZE = 5,
		INITIAL_MAX_HEALTH = 100;
	
	private GraphiteUser user;
	private MessageOutput output, helpOutput;
	private MessageInput input;
	private SelectInput<Integer> helpInput;
	private boolean helpDisplayed;
	
	private String tmpMsg = "";
	
	private DialogAction dialogAction;
	
	@JSONValue
	private String userID;
	
	@JSONValue
	private int x, y, money, health, maxHealth;
	
	@JSONValue
	@JSONComplexListType(RPGObject.class)
	private List<RPGObject> inventory;
	
	@JSONValue
	@JSONListType(JSONType.INTEGER)
	private List<Integer> explored;
	
	@JSONValue
	@JSONComplexListType(RPGQuest.class)
	private List<RPGQuest> activeQuests;
	
	@JSONValue
	private RPGObject equippedItem;
	
	@JSONConstructor
	private RPGPlayer() {}
	
	public RPGPlayer(GraphiteUser user, int x, int y) {
		this.x = x;
		this.y = y;
		this.health = this.maxHealth = INITIAL_MAX_HEALTH;
		this.inventory = new ArrayList<>(INVENTORY_SIZE);
		this.explored = new ArrayList<>();
		this.activeQuests = new ArrayList<>();
		explored.add(RPG.INSTANCE.getMap().xyp(x, y));
		setUser(user);
	}
	
	private void onInput(String msg) {
		if(dialogAction != null) {
			int idx = dialogAction.getIndex(msg);
			if(idx == -1) {
				send("They don't seem to understand what you mean");
				pushMsg();
				return;
			}
			dialogAction.respond(idx);
			dialogAction = null;
			pushMsg();
			return;
		}
		Map.Entry<RPGAction, String> ac = Arrays.stream(RPGAction.values()).map(a -> {
			String t = a.getStartingTrigger(msg);
			if(t == null) return null;
			return MiscUtils.newMapEntry(a, t);
		}).filter(Objects::nonNull).findFirst().orElse(null);
		if(ac == null) {
			send("You don't know how to " + msg);
			pushMsg();
			return;
		}
		
		RPGLocation l = getLocation();
		
		for(RPGEnemy en : l.getEnemies()) {
			int ad = en.onAttack(this);
			if(ad < 0) continue;
			send("The *" + en.getName() + "* attacks you, dealing *" + ad + "* damage");
			if(health == 0) {
				send("You faint");
				inventory.forEach(l::addObject);
				if(equippedItem != null) l.addObject(equippedItem);
				inventory.clear();
				equippedItem = null;
				health = maxHealth;
				int[] pt = RPGMap.getRandomSpotNear(x, y, 5);
				x = pt[0];
				y = pt[1];
				send("As you regain your consciousness, you find yourself at (" + x + "/" + y + ")");
				pushMsg();
				return;
			}
		}
		
		String subject = msg.substring(ac.getValue().length()).trim();
		o: switch(ac.getKey()) {
			case INSPECT:
			{
				if(!requireSubject(subject, "You can't quite seem to decide what to inspect")) break;
				RPGNPC npc = l.getNPC(subject);
				if(npc != null) {
					send("You stare at *" + npc.getName() + "* until they uncomfortably turn away from you");
					break;
				}
				RPGObject obj = l.getObject(subject);
				if(obj != null) {
					send(obj.getDescription());
					break;
				}
				send("You can't see a " + subject);
				break;
			}
			case INTERACT:
			{
				if(!requireSubject(subject, "You can't quite seem to decide what to use")) break;
				RPGObject obj = getInventoryItem(subject);
				if(obj == null) {
					send("You don't seem to be carrying a *" + subject + "*");
					break;
				}
				if(obj.onUsed(this)) inventory.remove(obj);
				break;
			}
			case MOVE:
			{
				if(!requireSubject(subject, "You can't quite seem to decide where to move")) break;
				int nx = x, ny = y;
				switch(subject.toLowerCase()) {
					case "north": ny--; break;
					case "south": ny++; break;
					case "west": nx--; break;
					case "east": nx++; break;
					default:
					{
						send("You don't know what direction *" + subject + "* is supposed to be");
						break o;
					}
				}
				if(RPG.INSTANCE.getMap().isOOB(nx, ny)) {
					send("There seems to be some kind of invisible force stopping you from walking that way. Weird.");
					break;
				}
				x = nx;
				y = ny;
				explored.add(RPG.INSTANCE.getMap().xyp(nx, ny));
				sendLocationInfo();
				break;
			}
			case TAKE:
			{
				if(!requireSubject(subject, "You can't quite seem to decide what to take")) break;
				RPGObject obj = l.getObject(subject);
				if(obj == null) {
					send("You can't see a " + subject);
					break;
				}
				if(isInventoryFull()) {
					send("You can't carry anything more");
					break;
				}
				if(!obj.onPickup(this)) {
					send("You try to take the *" + obj.getName() + "* but it doesn't seem to want to move");
					break;
				}
				l.removeObject(obj);
				inventory.add(obj);
				send("You pick up the *" + obj.getName() + "*");
				break;
			}
			case TALK_TO:
			{
				if(!requireSubject(subject, "You can't quite seem to decide who to talk to")) break;
				RPGNPC npc = l.getNPC(subject);
				if(npc != null) {
					npc.onInteract(this);
					break;
				}
				RPGObject obj = l.getObject(subject);
				if(obj != null) {
					send("You try to talk to the *" + obj.getName() + "* but it obviously doesn't respond");
					break;
				}
				send("You can't see a " + subject);
				break;
			}
			case LOOK_AROUND:
			{
				sendLocationInfo();
				break;
			}
			case MAP:
			{
				sendMap();
				break;
			}
			case INVENTORY:
			{
				
				String invS = "You are currently at " + health + "/" + maxHealth + " health\n\n";
				invS += "You have " + money + "$\n\n";
				invS += (equippedItem == null ? "Your hands are currently empty" : "You are currently holding a *" + equippedItem.getName() + "* in your hands") + "\n\n";
				invS += inventory.isEmpty() ? "Your inventory is empty" : inventory.stream().map(RPGObject::getDescription).collect(Collectors.joining("\n\n\n"));
				send(invS);
				break;
			}
			case DROP:
			{
				if(!requireSubject(subject, "You can't quite seem to decide what to drop")) break;
				RPGObject obj = getInventoryItem(subject);
				if(obj == null) {
					send("You don't seem to be carrying a *" + subject + "*");
					break;
				}
				inventory.remove(obj);
				l.addObject(obj);
				send("You drop the " + obj.getName() + " on the ground");
				break;
			}
			case HELP:
			{
				String s = Arrays.stream(RPGAction.values()).map(a -> a.getTriggers().get(0)).collect(Collectors.joining(", "));
				send("As you scream for help, you suddenly feel as if you now know more about what you can and can't do.\nYou now know that you can: " + s);
				break;
			}
			case QUESTS:
			{
				String qS = activeQuests.isEmpty() ? "You currently don't have any quests" : activeQuests.stream().map(RPGQuest::getDescription).collect(Collectors.joining("\n- ", "- ", ""));
				send(qS);
				break;
			}
			case EQUIP:
			{
				if(!requireSubject(subject, "You can't quite seem to decide what to equip")) break;
				RPGObject obj = getInventoryItem(subject);
				if(obj == null) {
					send("You don't seem to be carrying a *" + subject + "*");
					break;
				}
				inventory.remove(obj);
				if(equippedItem != null) inventory.add(equippedItem);
				equippedItem = obj;
				send("You wield the *" + obj.getName() + "*");
				break;
			}
			case DEQUIP:
			{
				if(equippedItem == null) {
					send("Your hands are empty");
					break;
				}
				if(isInventoryFull()) {
					send("You can't carry anything more");
					break;
				}
				inventory.add(equippedItem);
				send("You put the *" + equippedItem.getName() + "* back in your pockets");
				equippedItem = null;
				break;
			}
			case ATTACK:
			{
				if(!requireSubject(subject, "You can't quite seem to decide what to attack")) break;
				RPGEnemy en = l.getEnemy(subject);
				if(en == null) {
					send("You can't see a " + subject);
					break;
				}
				int ad = equippedItem == null ? 3 : equippedItem.getType().getAttackDamage();
				if(!en.canBeAttacked(this)) {
					send("You try to attack the *" + en.getName() + "* but it doesn't seem like it even noticed you");
					break;
				}
				send("You attack the *" + en.getName() + "*" + (equippedItem != null ? " with the *" + equippedItem.getName() + "*" : "") + ", dealing *" + ad + "* damage");
				en.onAttacked(this, ad);
				break;
			}
			default:
				send("Not implemented");
				break;
		}
		
		pushMsg();
	}
	
	private boolean requireSubject(String subj, String message) {
		if(subj != null && !subj.isEmpty()) return true;
		send(message);
		return false;
	}
	
	private void pushMsg() {
		if(tmpMsg.isEmpty()) return;
		output.update(tmpMsg, true);
		tmpMsg = "";
	}
	
	public RPGLocation getLocation() {
		return RPG.INSTANCE.getMap().getLocation(x, y);
	}
	
	public List<RPGQuest> getActiveQuests() {
		return activeQuests;
	}
	
	public void setDialogAction(DialogAction dialogAction) {
		this.dialogAction = dialogAction;
	}
	
	public DialogAction getDialogAction() {
		return dialogAction;
	}
	
	public RPGObject getInventoryItem(String name) {
		return RPGLocation.find(inventory, name, RPGObject::getName);
	}
	
	public boolean hasObject(RPGObjectType type) {
		return (equippedItem != null && equippedItem.getType().equals(type)) || inventory.stream().anyMatch(o -> o.getType().equals(type));
	}
	
	public boolean hasObjectCategory(RPGObjectCategory type) {
		return (equippedItem != null && equippedItem.getType().getCategory().equals(type)) || inventory.stream().anyMatch(o -> o.getType().getCategory().equals(type));
	}
	
	public boolean isInventoryFull() {
		return inventory.size() >= INVENTORY_SIZE;
	}
	
	private void sendLocationInfo() {
		RPGLocation l = getLocation();
		String st = LocalizedMessage.formatMessage(l.getType().getLocationDescription(), "x", ""+x, "y", ""+y);
		List<String> s = new ArrayList<>();
		l.getNPCs().forEach(o -> s.add(o.getName()));
		l.getObjects().forEach(o -> s.add(o.getName()));
		l.getEnemies().forEach(o -> s.add(o.getName()));
		l.getPlayers().stream().filter(RPGPlayer::isPlaying).filter(p -> !p.getUserID().equals(userID)).forEach(o -> s.add(o.getUser().getName()));
		if(s.isEmpty()) {
			send(st + "\n\nThere doesn't seem to be anything of interest here");
			return;
		}
		send(st + "\n\nYou see: " + s.stream().collect(Collectors.joining(", ")));
	}
	
	private void sendMap() {
		MessageGraphics g = new MessageGraphics();
		for(int rx = -3; rx <= 3; rx++) {
			for(int ry = -3; ry <= 3; ry++) {
				int ax = x + rx, ay = y + ry;
				RPGLocation loc = RPG.INSTANCE.getMap().getLocation(ax, ay);
				g.setSymbol(explored.contains(RPG.INSTANCE.getMap().xyp(ax, ay)) ? (loc == null ? JDAEmote.BLACK_LARGE_SQUARE : loc.getLocationIcon(this)) : JDAEmote.BLACK_LARGE_SQUARE);
				g.point(rx, ry);
			}
		}
		g.setSymbol(JDAEmote.RED_CIRCLE);
		g.point(0, 0);
		send(g);
	}
	
	public void send(String msg) {
		tmpMsg += (tmpMsg.isEmpty() ? "" : "\n\n") + msg;
	}
	
	private void send(MessageGraphics msg) {
		send(msg.render());
	}
	
	public void setUser(GraphiteUser user) {
		if(user == null) {
			this.user = null;
			this.output.remove();
			this.helpOutput.remove();
			this.helpInput.remove();
			this.input.remove();
			return;
		}
		this.user = user;
		this.userID = user.getID();
		this.output = new MessageOutput(user.openPrivateChannel(), true);
		this.helpOutput = new MessageOutput(user.openPrivateChannel());
		this.input = new MessageInput(user, false, this::onInput);
		input.apply(user.openPrivateChannel());
		sendLocationInfo();
		pushMsg();
		
		this.helpInput = new SelectInput<Integer>(user, it -> {
			helpDisplayed = !helpDisplayed;
			if(helpDisplayed) {
				helpOutput.update(DefaultMessage.MINIGAME_RPG_HELP.createEmbed(user));
			}else {
				helpOutput.remove();
			}
		})
		.autoRemove(false)
		.removeMessage(false);
		
		helpInput.addOption(JDAEmote.QUESTION, 0);
		helpInput.apply(output.getMessage());
	}
	
	public boolean isPlaying() {
		return user != null;
	}
	
	public GraphiteUser getUser() {
		return user;
	}
	
	public MessageOutput getOutput() {
		return output;
	}
	
	public MessageOutput getHelpOutput() {
		return helpOutput;
	}
	
	public MessageInput getInput() {
		return input;
	}
	
	public SelectInput<Integer> getHelpInput() {
		return helpInput;
	}
	
	public String getUserID() {
		return userID;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void addHealth(int amount) {
		health = Math.min(health + amount, maxHealth);
	}
	
	public void reduceHealth(int amount) {
		health = Math.max(health - amount, 0);
	}
	
	public int getHealth() {
		return health;
	}
	
	public int getMaxHealth() {
		return maxHealth;
	}
	
	public void setMoney(int money) {
		this.money = money;
	}
	
	public void addMoney(int amount) {
		this.money += amount;
	}
	
	public int getMoney() {
		return money;
	}
	
	public List<RPGObject> getInventory() {
		return inventory;
	}
	
}
