package org.arkham.tntrun.board;

import net.galaxygaming.dispenser.game.GameState;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;

import org.arkham.tntrun.TNTRun;
import org.arkham.tntrun.util.Variable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

// Keeps track of scoreboard stuff.
public class ScoreboardManager {
	private static ScoreboardManager instance = new ScoreboardManager();
	private Scoreboard preBoard, board;
	private Objective preObjective, objective;
	
	public void makeAll() {
		makePreBoard();
		makeBoard();
	}
	
	public void updateAll() {
		updatePreBoard();
		updateBoard();
		updateAllSpaces();
	}
	
	public void makePreBoard() {
		preBoard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		preObjective = preBoard.registerNewObjective("Pre-Game", "dummy");
		Variable.needed.set(preBoard.registerNewTeam("needed"));
		Variable.needed.value(Team.class).addPlayer(Variable.FAKE_PLAYER.value(OfflinePlayer.class));
		Variable.needed.value(Team.class).setSuffix(ChatColor.translateAlternateColorCodes('&', "&c&l Needed"));
	}
	
	public void makeBoard() {
		board = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		objective = board.registerNewObjective("ArkhamTNTRun", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	@SuppressWarnings("deprecation")
	public void updatePreBoard() {
		preObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		preObjective.setDisplayName(PreTitle.OBJECTIVE.name());
		if (Variable.playersNeeded.value() != null)
			preBoard.resetScores(Variable.playersNeeded.value(String.class));
		TNTRun game = (TNTRun) Variable.currentGame.value();
		Variable.playersNeeded.set(ChatColor.translateAlternateColorCodes('&', "  " + "" + game.getPlayers().length + "/" + game.getMinPlayers()));
		preObjective.setDisplayName(PreTitle.OBJECTIVE.name());
		PreTitle.SERVER.addTo(preObjective);
		PreTitle.SERVER_NUMBER.addTo(preObjective);
		PreTitle.MAP_NAME.addTo(preObjective);
		preObjective.getScore(Variable.FAKE_PLAYER.value(OfflinePlayer.class)).setScore(Score.PRE_PLAYERS_NEEDED_TAG);
		preObjective.getScore(Variable.playersNeeded.value(String.class)).setScore(Score.PRE_PLAYERS_NEEDED_VALUE);
		if (Variable.mapName.value() != null)
			preObjective.getScore("  " + Variable.mapName.value()).setScore(Score.PRE_MAP_VALUE);
	}
	
	public void updateBoard() {
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(Title.OBJECTIVE.name());
		if (Variable.alive.value(0) == -1) {
			board.resetScores(Title.RUN.name());
			Title.GAME_OVER.addTo(objective);
		} else {
			board.resetScores(Title.GAME_OVER.name());
			Title.RUN.addTo(objective);
		}
		Title.SERVER.addTo(objective);
		Title.SERVER_NUMBER.addTo(objective);
		Title.MAP_NAME.addTo(objective);
		if (Variable.mapName.value() != null)
			objective.getScore("  " + Variable.mapName.value()).setScore(Score.MAP_VALUE);
	}
	
	public void updatePreStarting() {
		Variable.starting.set(ChatColor.translateAlternateColorCodes('&', "&c&lArkham &f&lTNTRun - " + Variable.startingIn.value() + "s"));
		preObjective.setDisplayName(Variable.starting.value(String.class));
	}
	
	public void updatePlayerCount() {
		// Player tag
		Title.PLAYERS.addTo(objective);

		// Player counter
		TNTRun game = (TNTRun) Variable.currentGame.value();
		if (game == null) return;
		board.resetScores("  " + Variable.lastPlayerCount.value(0));
		Variable.lastPlayerCount.set(Variable.alive.value(0));
		if (game.getState() == GameState.ACTIVE && Variable.alive.value(0) <= 1)
			game.end();
		objective.getScore("  " + Variable.lastPlayerCount.value(0)).setScore(Score.PLAYER_COUNTER);
	}
	
	// Only to be called every second while game is in progress.
	public void updateTime() {
		// Time tag
		Title.GAME_TIME.addTo(objective);

		// Time counter
		board.resetScores("  " + Variable.lastTimeCount.value(0));
		Variable.lastTimeCount.increment();
		objective.getScore("  " + Variable.lastTimeCount.value(0)).setScore(Score.TIME_COUNTER);
	}
	
	public void updateMap() {
		Variable.mapName.set((ChatColor.translateAlternateColorCodes('&', Variable.currentGame.value(TNTRun.class).getName()) + "              ").substring(0, 14));
		updateAll();
	}
	
	public void updateAllSpaces() {
		updatePreSpaces();
		updateSpaces();
	}
	
	public void updatePreSpaces() {
		int[] spaces = Variable.SPACES_PRE.value(int[].class);
		for (int i = 0; i < spaces.length; i++) {
			preObjective.getScore(StringUtils.repeat(' ', i + 1)).setScore(spaces[i]);
		}
	}
	
	public void updateSpaces() {
		int[] spaces = Variable.SPACES.value(int[].class);
		for (int i = 0; i < spaces.length; i++) {
			objective.getScore(StringUtils.repeat(' ', i + 1)).setScore(spaces[i]);
		}
	}
	
	public Scoreboard getBoard() {
		return board;
	}
	
	public Scoreboard getPreBoard() {
		return preBoard;
	}
	
	public static ScoreboardManager getInstance() {
		return instance;
	}
}