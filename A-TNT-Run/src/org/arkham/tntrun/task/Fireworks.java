package org.arkham.tntrun.task;

import java.util.ArrayList;
import java.util.Random;

import net.galaxygaming.dispenser.game.GameManager;

import org.arkham.tntrun.TNTRun;
import org.arkham.tntrun.util.Variable;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class Fireworks extends BukkitRunnable {
    private static ArrayList<Color> colors = new ArrayList<Color>();
    private static ArrayList<FireworkEffect.Type> types = new ArrayList<FireworkEffect.Type>();
	private Player player;
	private int duration;
	private Location lastLocation;

	/**
	 * 
	 * @param pl - Player for fireworks to stalk.
	 * @param duration - Duration of effect.
	 */
	public Fireworks(Player player, int duration) {
		this.player = player;
		this.duration = duration;
        if(colors == null || colors.size() == 0) addColors();
        if(types == null || types.size() == 0) addTypes();
	}

	@Override
	public void run() {
		duration--;
		// Assumes this runnable is called every 20 ticks
		if (duration <= 0) {
			TNTRun game = Variable.currentGame.value(TNTRun.class);
			game.onLoad();
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				player.teleport(player.getWorld().getSpawnLocation());
				if (GameManager.getGameManager().getGameForPlayer(player) != null)
					GameManager.getGameManager().removePlayerFromGame(player);
				game.onPlayerJoin(player);
			}
			cancel();
			return;
		}
		
		lastLocation = (player == null || !player.isOnline()) ? Bukkit.getOnlinePlayers().length == 0 ? null : Bukkit.getOnlinePlayers()[0].getLocation() : player.getLocation();
		if (lastLocation != null)
			launchRandomFirework(lastLocation);
	}
 
    private void addColors(){
        // Add all the colors...
        colors = new ArrayList<Color>();
        colors.add(Color.WHITE);
        colors.add(Color.PURPLE);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.AQUA);
        colors.add(Color.BLUE);
        colors.add(Color.FUCHSIA);
        colors.add(Color.GRAY);
        colors.add(Color.LIME);
        colors.add(Color.MAROON);
        colors.add(Color.YELLOW);
        colors.add(Color.SILVER);
        colors.add(Color.TEAL);
        colors.add(Color.ORANGE);
        colors.add(Color.OLIVE);
        colors.add(Color.NAVY);
        colors.add(Color.BLACK);
    }
 
    private void addTypes() {
        types.add(FireworkEffect.Type.BURST);
        types.add(FireworkEffect.Type.BALL);
        types.add(FireworkEffect.Type.BALL_LARGE);
        types.add(FireworkEffect.Type.CREEPER);
        types.add(FireworkEffect.Type.STAR);
    }
 
    // Getting a random firework
    private FireworkEffect.Type getRandomType() {
        int size = types.size();
        Random ran = new Random();
        FireworkEffect.Type theType = types.get(ran.nextInt(size));
 
        return theType;
    }
 
    private Color getRandomColor() {
        int size = colors.size();
        Random ran = new Random();
        Color color = colors.get(ran.nextInt(size));
 
        return color;
    }
 
    private void launchRandomFirework(Location loc) {
    		lastLocation = loc;
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta fm = fw.getFireworkMeta();
        fm.setPower(1);
        // Adding all the effects to the firework meta
        fm.addEffects(FireworkEffect.builder().with(getRandomType()).withColor(getRandomColor()).trail(true).build());
        // Set the firework meta to the firework!
        fw.setFireworkMeta(fm);
    }
}