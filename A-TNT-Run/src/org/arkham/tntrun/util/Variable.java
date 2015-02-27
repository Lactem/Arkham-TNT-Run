package org.arkham.tntrun.util;

import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.google.common.collect.Lists;

import net.galaxygaming.dispenser.GameDispenser;
import net.galaxygaming.dispenser.team.Spectator;
import net.minecraft.util.com.google.common.collect.Maps;

// Only works if there's one game (which there is in this case).
/*
   If you ever change to multiple game instances running on one
   server, then you'll need to make these not be static and have a
   variable instance in the TNTRun class.
*/

public class Variable {
	private Object value;
	private Object[] valueArray;
	
	public static Variable
		winner 			= new Variable(),						// Tracks winner (of type Player)
		mapName			= new Variable(),						// Stores the name of the current map
		lastPlayerCount	= new Variable(0),						// Tracks the amount of players (for resetting scoreboards)
		lastTimeCount	= new Variable(0),						// Tracks how much time has passed (for resetting scoreboards)
		playersNeeded	= new Variable(),						// Tracks the minimum amount of players needed to start the game
		alive			= new Variable(0),						// Tracks how many players are left alive (gets set to -1 after game ends)
		total			= new Variable(0),						// The amount of players the game started with (doesn't count spectators who join after the game is already started)
		graceCounter	= new Variable(0),						// The seconds left in the grace period
		startingIn		= new Variable(0),						// The seconds left until the game starts
		starting		= new Variable(),						// The scoreboard title composed of "starting in + <time left until game starts>"
		map				= new Variable(0),						// The index of the current game
		currentGame		= new Variable(),						// The current game (of type TNTRun)
		spectator		= new Variable(new Spectator()),		// The spectator team
		games			= new Variable(Lists.newArrayList()),	// List of TNTRun games
		times			= new Variable(Maps.newHashMap()),		// Tracks how many seconds have passed since a player moved (Map<Player, Integer>)
		wins			= new Variable(Maps.newHashMap()),		// Tracks how many times a player has won in a row (to award the Bomberman title)
		needed			= new Variable();						// The team to add FAKE_PLAYER to (to get the scoreboard length longer)
	
	@SuppressWarnings("deprecation")
	public static final Variable
		FAKE_PLAYER	= new Variable(Bukkit.getOfflinePlayer(ChatColor.translateAlternateColorCodes('&', "&c&lPlayers"))), // See needed variable above.
		SPACES		= new Variable(new int[] {14, 12, 9, 6, 3}), // Scores of the spaces on the scoreboard
		SPACES_PRE	= new Variable(new int[] {9, 6, 3});		 // Scores of the spaces on the pre-game scoreboard
	
	public Variable() {}
	
	public Variable(Object value) {
		this.value = value;
	}
	
	public Variable(Class<?> clazz) {
		try {
			value = clazz.newInstance();
		} catch (Exception e) {
			GameDispenser.getInstance().getLogger().log(Level.WARNING, "Error creating a new Variable instance from " + clazz.getName() + ":");
			e.printStackTrace();
		}
	}
	
	public Variable(Object[] valueArray) {
		this.valueArray = valueArray;
	}
	
	public Variable(Class<?>[] classes) {
		for (int i = 0; i < classes.length; i++) {
			try {
				valueArray[i] = classes[i].newInstance();
			} catch (Exception e) {
				GameDispenser.getInstance().getLogger().log(Level.WARNING, "Error creating a new Variable instance from " + classes[i].getName() + ":");
				e.printStackTrace();
			}
		}
	}
	
	// value() methods return a pre-casted value so that you don't have to
	public Object value() {
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T value(T t) {
		if (value == null) return null;
		return (T) value;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T value(Class<T> t) {
		if (value == null) return null;
		return (T) value;
	}
	
	@SuppressWarnings("unchecked")
	public <K, V> Map<K, V> value(Class<K> k, Class<V> v) {
		if (value == null) return null;
		return (Map<K, V>) value;
	}
	
	public Object[] value(Object[]... arrays) {
		return valueArray;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T[] value(T[] t) {
		if (valueArray == null) return null;
		return (T[]) valueArray;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T[] value(Class<T>[] t) {
		if (valueArray == null) return null;
		return (T[]) valueArray;
	}
	
	public void set(Object value) {
		this.value = value;
	}
	
	public void set(Object[] valueArray) {
		this.valueArray = valueArray;
	}
	
	// Increment and decrement functions are made so that less has to be typed in other classes.
	public void increment() {
		incrementValue(value);
	}
	
	public void decrement() {
		decrementValue(value);
	}
	
	public void incrementAll() {
		for (int i = 0; i < valueArray.length; i++)
			incrementIndex(i);
	}
	
	public void decrementAll() {
		for (int i = 0; i < valueArray.length; i++)
			incrementIndex(i);
	}
	
	public void incrementIndex(int index) {
		incrementValue(valueArray[index]);
	}
	
	public void decrementIndex(int index) {
		decrementValue(valueArray[index]);
	}
	
	private void incrementValue(Object value) {
		if (value instanceof Integer) {
			this.value = (int) value + 1;
		} else if (value instanceof Double) {
			this.value = (double) value + 1;
		} else if (value instanceof Short) {
			this.value = (short) value + 1;
		} else if (value instanceof Byte) {
			this.value = (byte) value + 1;
		} else if (value instanceof Long) {
			this.value = (long) value + 1;
		}
	}
	
	private void decrementValue(Object value) {
		if (value instanceof Integer) {
			this.value = (int) value - 1;
		} else if (value instanceof Double) {
			this.value = (double) value - 1;
		} else if (value instanceof Short) {
			this.value = (short) value - 1;
		} else if (value instanceof Byte) {
			this.value = (byte) value - 1;
		} else if (value instanceof Long) {
			this.value = (long) value - 1;
		}
	}
	
	// Resets all variables for the next game to start
	public static void reset() {
		winner = new Variable();
		lastPlayerCount = new Variable(0);
		lastTimeCount = new Variable(0);
		alive = new Variable(0);
		spectator = new Variable(new Spectator());
		times = new Variable(Maps.newHashMap());
		mapName = new Variable();
	}
}