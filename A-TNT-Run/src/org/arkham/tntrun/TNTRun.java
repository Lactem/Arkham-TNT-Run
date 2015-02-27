package org.arkham.tntrun;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.galaxygaming.dispenser.GameDispenser;
import net.galaxygaming.dispenser.game.Game;
import net.galaxygaming.dispenser.game.GameBase;
import net.galaxygaming.dispenser.game.GameManager;
import net.galaxygaming.dispenser.game.GameState;
import net.galaxygaming.dispenser.game.component.Component;
import net.galaxygaming.dispenser.task.GameRunnable;
import net.galaxygaming.dispenser.team.Spectator;
import net.galaxygaming.selection.RegenableSelection;
import net.galaxygaming.util.FormatUtil;

import org.arkham.tntrun.board.ScoreboardManager;
import org.arkham.tntrun.task.Fireworks;
import org.arkham.tntrun.util.Components;
import org.arkham.tntrun.util.Util;
import org.arkham.tntrun.util.Variable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Lists;

public class TNTRun extends GameBase {
	private @Component Location spawn;					// Spawn location
	private @Component RegenableSelection arena;			// Arena to be regerenated at the end of every round
	private @Component float explosionPower = 1f;		// The power of the TNT explosions
	private @Component int bombChance = 10;				// The percentage a falling block has to turn into TNT
	private @Component double tokenMultiplier = 0.03;	// What to multiply the time by to calculate the amount of tokens to award a player
	private @Component double minTokens = 1.35;			// The minimum amount of tokens required to be earned in a single round. Otherwise no tokens will be awarded to the player.
	private @Component double winBonus = 2;				// How many extra tokens to give to the winner of the game
	private ScoreboardManager scoreboardManager = ScoreboardManager.getInstance();
	private Util util = Util.getInstance();
	private static Components components;

	static {
		new GameRunnable() {
			@Override
			public void run() {
				Variable.games.value(List.class).clear();
				for (Game game : GameManager.getGameManager().getGames()) {
					Variable.games.value(new ArrayList<String>()).add(game.getName());
				}
				if (Variable.games.value(List.class).size() > 0)
					Variable.currentGame.set(GameManager.getGameManager().getGame(Variable.games.value(new ArrayList<String>()).get(Variable.map.value(0))));
			}
		}.runTaskLater(15l);
	}
	
	@Override
	public void onLoad() {
		Variable.reset();
		new GameRunnable() { // This is necessary because components are loaded after onLoad() is called in GameBase.
			@Override
			public void run() {
				components = new Components(spawn, arena, explosionPower, bombChance, tokenMultiplier, minTokens, winBonus);
			}
		}.runTaskLater(1l);
		Variable.startingIn.set(countdownDuration);
		Variable.graceCounter.set(graceDuration);
		if (Variable.currentGame.value() == null)
			Variable.currentGame.set(this);
		setPlayers(Lists.newArrayList(new Player[0]));
		state = GameState.LOBBY;
		
		if (!getConfig().contains("force players"))
			getConfig().set("force players", true);
		boolean force = getConfig().getBoolean("force players");
		if (force) {
			getConfig().set("minimum players", 8);
			getConfig().set("maximum players", 12);
		}
		
		minimumPlayers = getConfig().getInt("minimum players");
		maximumPlayers = getConfig().getInt("maximum players");
		scoreboardManager.makeAll();
		scoreboardManager.updateAll();
	}

	@Override
	public void onStart() {
		Variable.alive.set(getPlayers().length);
		Variable.total.set(getPlayers().length);
		scoreboardManager.updateBoard();
		
		for (Player player : getPlayers()) {
			player.setAllowFlight(false);
			player.teleport(components.spawn());
			player.setScoreboard(scoreboardManager.getBoard());
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true));
			if (player.getGameMode() != GameMode.SURVIVAL)
				player.setGameMode(GameMode.SURVIVAL);
		}
	}

	@Override
	public void onSecond() {
		if (state == GameState.GRACE) {  // Grace period
			getPlayers()[0].playSound(getPlayers()[0].getLocation(), Sound.ANVIL_LAND, 0.7F, 0.25F);
			broadcast(ChatColor.translateAlternateColorCodes('&', " &4&l>> &cGrace time ends in &7" + Variable.graceCounter.value() + " &cseconds!"));
			Variable.graceCounter.decrement();
			util.checkPlayerTimeEqual();
		} else if (state == GameState.STARTING) {  // Countdown until grace period
			Variable.startingIn.decrement();
			scoreboardManager.updatePreStarting();
			if (Variable.startingIn.value(0) % 30 == 0 || Variable.startingIn.value(0) <= 5)
				getPlayers()[0].playSound(getPlayers()[0].getLocation(), Sound.ANVIL_LAND, 0.7F, 0.25F);
			if (getPlayers().length < minimumPlayers) {
				state = GameState.LOBBY;
				scoreboardManager.updatePreBoard();
			}
		} else if (state == GameState.ACTIVE) {  // Game is in progress
			util.checkPlayerTimeEqual();
			scoreboardManager.updateTime();

			for (Player player : getPlayers()) {
				if (Variable.spectator.value(Spectator.class).isOnTeam(player))
					continue;
				player.setFoodLevel(20);
				util.checkTime(player, this);
			}
		}
	}

	@Override
	public void onCountdown() {
		setCounter(Variable.startingIn.value(0) - 1);
	}
	
	@Override
	public void onEnd() {
		new GameRunnable() { // Regenerate broken blocks in the arena
			@Override
			public void run() {
				components.arena().regen();
			}
		}.runTask();
		
		new Fireworks(Variable.winner.value(Player.class), 10).runTaskTimer(GameDispenser.getInstance(), 0l, 20l);
		Variable.alive.set(-1);
		giveRewards();
		util.respawnAllPlayers();
		util.rotateMap(this);
		scoreboardManager.updateMap();
		scoreboardManager.updateBoard();
	}

	@Override
	public void onPlayerJoin(final Player player) {
        if (state.ordinal() < GameState.STARTING.ordinal())
            state = GameState.LOBBY;
        
		player.setCanPickupItems(false);
		scoreboardManager.updateMap();
		
		if (state == GameState.ACTIVE || Variable.alive.value(0) == -1) {  // If game is in progress
			util.setSpectator(player);
			player.setScoreboard(scoreboardManager.getBoard());
			player.teleport(components.spawn());
		} else {
			addSinglePlayer(player, true);
	        if (getPlayers().length >= minimumPlayers) {
	            startCountdown();
	        }
			player.teleport(player.getWorld().getSpawnLocation());
			util.trySetPreBoard(player);
		}
		scoreboardManager.updatePreBoard();
		scoreboardManager.updatePlayerCount();
        updateSigns();
	}
	
	@Override
	public void onPlayerLeave(Player player) {
		if (state == GameState.ACTIVE || state == GameState.GRACE) {
			if (Variable.spectator.value(Spectator.class).isOnTeam(player))
				Variable.spectator.value(Spectator.class).remove(player);
			else if (Lists.newArrayList(getPlayers()).contains(player))
				Variable.alive.decrement();
			util.checkGameOver(this, player);
		} else {
			scoreboardManager.updatePreBoard();
		}
		scoreboardManager.updatePlayerCount();
	}
	
	// Method to respawn a player without actually killing him/her.
	public void die(final Player player) {
		try {
			util.respawn(player, components.spawn());
		} catch (Exception e) {
			Bukkit.getLogger().log(Level.WARNING, "Error with respawning player " + player.getName() + ":");
			e.printStackTrace();
		}
		
		if (!Variable.spectator.value(Spectator.class).isOnTeam(player) && Lists.newArrayList(getPlayers()).contains(player)) {
			Variable.alive.decrement();
			util.setSpectator(player);
			broadcast("&4&l" + player.getName() + " &r&elost at &b&l" + Variable.lastTimeCount.value(0) + " &r&eseconds&r.&e "
					+ FormatUtil.format(getType().getMessages().getMessage("game.playerCount"), Variable.alive.value(0), Variable.total.value(0)));
			util.giveTokens(player);
		}
		util.checkGameOver(this, player);
		scoreboardManager.updatePlayerCount();
	}
	
	// Gives title if applicable and tokens to the winner
	private void giveRewards() {
		Player winner = Variable.winner.value(Player.class);
		if (winner != null) {
			broadcast("&6&l" + winner.getName() + " &r&ehas won the game!");
			util.awardTitle(winner);
			util.giveTokens(winner, components.bonus());
		}
	}
	
	public int getMinPlayers() {
		return minimumPlayers;
	}
	
	public int getMaxPlayers() {
		return maximumPlayers;
	}
	
	public Components components() {
		return components;
	}
}