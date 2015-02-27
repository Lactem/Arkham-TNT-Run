package org.arkham.tntrun.board;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;

import com.google.common.collect.Lists;

// For making unchanging titles on the scoreboard
public class Title {
	private final String name;
	private final int score;
	
	private static Title[] titles;
	
	public static final Title
			OBJECTIVE		= new Title("&c&lArkham &f&lTNTRun", 999),
			RUN				= new Title("&7Run, run, run!", 13),
			SERVER		    = new Title("&lServer", 5),
			SERVER_NUMBER	= new Title("  " + "TNT Run #" + (Bukkit.getPort() - 20000), 4),
			GAME_TIME		= new Title("&c&lGame Time", 8),
			GAME_OVER		= new Title("&c&lGame Over", 13),
			PLAYERS			= new Title("&lPlayers", 11),
			MAP_NAME			= new Title("&c&lMap", 2);
	
	public Title(String name, int score) {
		this.name = ChatColor.translateAlternateColorCodes('&', name);
		this.score = score;
		if (titles == null)
			titles = new Title[] {this};
		
		else {
			List<Title> temp = Lists.newArrayList();
			temp.addAll(Arrays.asList(titles.clone()));
			temp.add(this);
			titles = temp.toArray(new Title[titles.length + 1]);
		}
	}
	
	public String name() {
		return name;
	}
	
	public String getName() {
		return name;
	}
	
	public int score() {
		return score;
	}
	
	public int ordinal() {
		return score;
	}
	
	public void addTo(Objective obj) {
		obj.getScore(name).setScore(score);
	}
	
	public static Title[] titles() {
		return titles;
	}
}