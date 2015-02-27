package org.arkham.tntrun.util;

import net.galaxygaming.selection.RegenableSelection;

import org.bukkit.Location;

// This class is to channel everything so that the main class
// doesn't need so many getters.
public class Components {
	private Location spawn;
	private RegenableSelection arena;
	private float explosionPower = 1f;
	private int bombChance = 10;
	private double tokenMultiplier = 0.03;
	private double minTokens = 1.35;	
	private double winBonus = 2;
	
	public Components(Location spawn, RegenableSelection arena, float explosionPower,
			int bombChance, double tokenMultiplier, double minTokens, double winBonus) {
		this.spawn = spawn;
		this.arena = arena;
		this.explosionPower = explosionPower;
		this.bombChance = bombChance;
		this.tokenMultiplier = tokenMultiplier;
		this.minTokens = minTokens;
		this.winBonus = winBonus;
	}
	
	public int bombChance() {
		return bombChance;
	}
	
	public float explosionPower() {
		return explosionPower;
	}
	
	public Location spawn() {
		return spawn;
	}
	
	public RegenableSelection arena() {
		return arena;
	}
	
	public double bonus() {
		return winBonus;
	}
	
	public double winBonus() {
		return winBonus;
	}
	
	public double tokenMultiplier() {
		return tokenMultiplier;
	}
	
	public double multiplier() {
		return tokenMultiplier;
	}
	
	public double minTokens() {
		return minTokens;
	}
}