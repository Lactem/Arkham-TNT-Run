package org.arkham.tntrun.board;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;

import com.google.common.collect.Lists;

// For making unchanging titles on the pre-game scoreboard
public class PreTitle {
	private final String name;
	private final int score;
	
	private static PreTitle[] titles;
	
	public static final PreTitle
			OBJECTIVE		= new PreTitle("&c&lArkham &f&lTNTRun", 999),
			SERVER			= new PreTitle("&lServer", 8),
			SERVER_NUMBER	= new PreTitle("  " + "TNT Run #" + (Bukkit.getPort() - 20000), 7),
			MAP_NAME			= new PreTitle("&lMap", 2);
	
	public PreTitle(String name, int score) {
		this.name = ChatColor.translateAlternateColorCodes('&', name);
		this.score = score;
		
		if (titles == null)
			titles = new PreTitle[] {this};
		
		else {
			List<PreTitle> temp = Lists.newArrayList();
			temp.addAll(Arrays.asList(titles.clone()));
			temp.add(this);
			titles = temp.toArray(new PreTitle[titles.length + 1]);
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
	
	public static PreTitle[] titles() {
		return titles;
	}
}