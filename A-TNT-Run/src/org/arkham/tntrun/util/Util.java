package org.arkham.tntrun.util;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.logging.Level;

import net.galaxygaming.dispenser.GameDispenser;
import net.galaxygaming.dispenser.game.Game;
import net.galaxygaming.dispenser.game.GameManager;
import net.galaxygaming.dispenser.game.GameState;
import net.galaxygaming.dispenser.game.InvalidGameException;
import net.galaxygaming.dispenser.task.GameRunnable;
import net.galaxygaming.dispenser.team.Spectator;

import org.arkham.tntrun.TNTRun;
import org.arkham.tntrun.board.ScoreboardManager;
import org.arkhamnetwork.permissions.ArkhamPermissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;

// This class contains various utility functions used throught the game.
public class Util {
	private static Util instance = new Util();

	// Force respawns a player (without killing)
	public void respawn(final Player player, final Location spawn) {
		new GameRunnable() {
			public void run() {
				TNTRun game = Variable.currentGame.value(TNTRun.class);
				if (player.getGameMode() != GameMode.ADVENTURE)
					player.setGameMode(GameMode.ADVENTURE);
				player.setAllowFlight(true);
				player.setCanPickupItems(false);
				if (game == null)
					return;
				if (game.getState() == GameState.ACTIVE)
					player.teleport(spawn);
				else
					player.teleport(player.getWorld().getSpawnLocation());
			}
		}.runTaskLater(5l);
	}
	
	// Performs all the functions necessary to make a player be a spectator
	public void setSpectator(Player player) {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (p == player)
				continue;
			p.hidePlayer(player);
		}
		Variable.spectator.value(Spectator.class).add(player);
		player.sendMessage("");
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&l* SPECTATING MODE ENABLED *"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&oYou will be able to play again next round!"));
		player.sendMessage("");
	}
	
	// Checks if there is only one player alive, sets that player as the winner, and ends the game
	public void checkGameOver(Game game, Player player) {
		if (Variable.alive.value(0) <= 1) {
			Spectator spectator = Variable.spectator.value(Spectator.class);
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				if (spectator.isOnTeam(p) || player == p)
					continue;
				Variable.winner.set(p);
			}
			game.end();
		}
	}
	
	// Spawns all players for when the game ends, and updates things like gamemode for them
	public void respawnAllPlayers() {
		new GameRunnable() {
			@Override
			public void run() {
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					player.teleport(player.getWorld().getSpawnLocation());
					player.setAllowFlight(false);
					if (player.getGameMode() != GameMode.ADVENTURE)
						player.setGameMode(GameMode.ADVENTURE);
					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						if (p == player)
							continue;
						player.showPlayer(p);
					}
				}
			}
		}.runTaskLater(20l);
	}
	
	// Attempts to set the scoreboard of a player to preBoard (sometimes has error from a Bukkit bug, so this catches it)
	public void trySetPreBoard(final Player player) {
		try {
			player.setScoreboard(ScoreboardManager.getInstance().getPreBoard());
		} catch (ConcurrentModificationException e) {
			new GameRunnable() {
				@Override
				public void run() {
					player.setScoreboard(ScoreboardManager.getInstance().getPreBoard());
				}
			}.runTaskLater(20l);
		}
	}
	
	// Goes to the next map, or restarts if there is no last map
	public void rotateMap(Game game) {
		Variable.map.increment();
		try {
			try {
				GameManager.getGameManager().unloadGame(game);
				GameManager.getGameManager().loadGame(Variable.games.value(new ArrayList<String>()).get(Variable.map.value(0)));
			} catch (InvalidGameException e) {
				GameDispenser.getInstance().getLogger().log(Level.WARNING, "Error with map rotation:");
				e.printStackTrace();
			}
			Variable.currentGame.set(GameManager.getGameManager().getGames()[Variable.map.value(0)]);
		} catch (IndexOutOfBoundsException e) {
			Variable.map.set(0);
			try {
				GameManager.getGameManager().unloadGame(game);
				GameManager.getGameManager().loadGame(Variable.games.value(new ArrayList<String>()).get(Variable.map.value(0)));
			} catch (InvalidGameException e1) {
				GameDispenser.getInstance().getLogger().log(Level.WARNING, "Error with map rotation:");
				e1.printStackTrace();
			}
			Variable.currentGame.set(GameManager.getGameManager().getGames()[Variable.map.value(0)]);
		}
	}
	
	// Checks if the player count and game time are equal, and resets
	// the player count 21 ticks later. (a scoreboard can't have two equal values)
	public void checkPlayerTimeEqual() {
		if (Variable.lastTimeCount.value(0) == Variable.lastPlayerCount.value(0)) {
			new GameRunnable() {
				@Override
				public void run() {
					ScoreboardManager.getInstance().updatePlayerCount();
				}
			}.runTaskLater(21l);
		}
	}
	
	// Gives a player tokens for after he/she dies with a bonus of 0
	public void giveTokens(Player player) {
		giveTokens(player, 0);
	}
	
	// Gives a player tokens for after he/she dies
	public void giveTokens(Player player, double bonus) {
		Components components = Variable.currentGame.value(TNTRun.class).components();
		double tokens = components.multiplier() * Variable.lastTimeCount.value(0);
		if (tokens >= components.minTokens())
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tokens add " + player.getName() + " " + ((int) Math.round(tokens)) + bonus);
	}
	
	// Gives a player the bomberman title if he/she won 3 times in a row
	public void awardTitle(Player player) {
		int streak = 1;
		if (Variable.wins.value(Map.class).containsKey(player))
			streak += Variable.wins.value(Player.class, Integer.class).get(player);
		Variable.wins.set(Maps.newHashMap());
		if (streak >= 3) {
            try {
                ArkhamPermissions.getInstance().getDatabaseManager().addPermissionToUserIfNotExists(player.getUniqueId(), "titles.title.bomberman", "global");
                ArkhamPermissions.getInstance().getPermissionManager().reloadPlayer(player);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&'You've UNLOCKED the BomberMan title!"));
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Error with adding rank BomberMan to player " + player.getName() + ":");
                e.printStackTrace();
            }
		} else {
			Variable.wins.value(Player.class, Integer.class).put(player, streak);
		}
	}
	
	public int getTime(Player player) {
		if (Variable.times.value(Map.class).containsKey(player))
			return (int) Variable.times.value(Map.class).get(player);
		else {
			Variable.times.value(Player.class, Integer.class).put(player, 0);
			return 0;
		}
	}
	
	public void resetTime(Player player) {
		if (Variable.times.value(Map.class).containsKey(player))
			Variable.times.value(Player.class, Integer.class).remove(player);
	}
	public void checkTime(Player player) {
		checkTime(player, Variable.currentGame.value(TNTRun.class));
	}
	
	public void checkTime(Player player, TNTRun game) {
		if (player == null || player.isDead())
			return;
		int time = getTime(player);
		time++;
		if (time == 5) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', game.getType().getMessages().getMessage("game.warning")));
			resetTime(player);
			Variable.times.value(Player.class, Integer.class).put(player, time);
		} else if (time == 10) {
			game.die(player);
			resetTime(player);
		} else {
			resetTime(player);
			Variable.times.value(Player.class, Integer.class).put(player, time);
		}
	}
	
	public static Util getInstance() {
		return instance;
	}
}