package org.arkham.tntrun.listener;

import java.util.Random;

import net.galaxygaming.dispenser.game.GameState;
import net.galaxygaming.dispenser.task.GameRunnable;
import net.galaxygaming.dispenser.team.Spectator;

import org.arkham.tntrun.TNTRun;
import org.arkham.tntrun.util.Util;
import org.arkham.tntrun.util.Variable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.util.Vector;

public class Events implements Listener {
	private static Random random = new Random();
	
    @EventHandler
	public void onPlayerMove(PlayerMoveEvent event, TNTRun game) {
		if (game.getState() != GameState.ACTIVE)
			return;
		final Block block = event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (block.getType() == Material.AIR)
			return;
		if (!game.components().arena().getSelection().isIn(block.getLocation())) // No griefing outside of reset arena
			return;
		if (Variable.spectator.value(Spectator.class).isOnTeam(event.getPlayer()))
			return;

		if (event.getFrom().distanceSquared(event.getTo()) >= 0.03)
			Util.getInstance().resetTime(event.getPlayer());
		
		if (block.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.AIR) {
			breakBlock(block);
			return;
		}
		
		for (int i = 1; i < 11; i++) {
			if (block.getLocation().subtract(0, i, 0).getBlock().getType() == Material.AIR) {
				breakBlock(block.getLocation().subtract(0, i + 1, 0).getBlock());
				break;
			} else {
				block.getLocation().subtract(0, i, 0).getBlock().setType(Material.AIR);
			}
		}
	}
    
    // Death should not happen in TNT Run, but just in case...
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
		Variable.currentGame.value(TNTRun.class).die(event.getEntity());
		event.setDeathMessage("");
		event.getDrops().clear();
    }
    
    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
    		event.setCancelled(true);
    }
    
    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
    		event.getItem().remove();
    		event.setCancelled(true);
    }
    
    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
    		if (!event.getPlayer().isOp())
    			event.setCancelled(true);
    }
    
    @EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
    		event.setJoinMessage("");
		new GameRunnable() {
			@Override
			public void run() {
				if (Variable.currentGame.value() != null)
					Variable.currentGame.value(TNTRun.class).onPlayerJoin(event.getPlayer());
			}
		}.runTaskLater(2l);
	}
    
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
    		if (!(event.getEntity() instanceof Player))
    			return;
    		TNTRun game = Variable.currentGame.value(TNTRun.class);
    		if (game == null)
    			return;
    		
    		if (event.getCause() == DamageCause.VOID)
    			game.die((Player)event.getEntity());
    		event.setCancelled(true);
    }
    
    @EventHandler
    public void blockChange(final EntityChangeBlockEvent event) {
		final TNTRun game = Variable.currentGame.value(TNTRun.class);
		if (game == null || event.getEntity().getType() != EntityType.FALLING_BLOCK || !game.components().arena().getSelection().isIn(event.getEntity().getLocation()))
			return;
		new GameRunnable() {
			@Override
			public void run() {
				final Location loc = event.getEntity().getLocation();
				if (game.components().bombChance() < random.nextInt(100) + 1) {
					loc.getBlock().setType(Material.AIR);
					return;
				}
				loc.getBlock().setType(Material.TNT);
				new GameRunnable() {
					@Override
					public void run() {
						if (loc.getBlock().getType() == Material.TNT) {
							loc.getBlock().setType(Material.AIR);
							loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 2.2f, false, true);
						}
					}
				}.runTaskLater(40l);
			}
		}.runTaskLater(1l);
	}
    
    @EventHandler
    public void blockBreak(BlockBreakEvent event, TNTRun game) {
    		if (event.getBlock().getType() == Material.TNT && game.components().arena().getSelection().isIn(event.getBlock().getLocation()))
    			event.getBlock().setType(Material.AIR);
    		event.setCancelled(true);
    }
    
    @EventHandler
    public void onPrime(ExplosionPrimeEvent event) {
    		event.setCancelled(true);
    }
    
    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
    		for (Block block : event.blockList()) {
    			block.setType(Material.AIR);
    		}
    		event.blockList().clear();
    }
    
    // Makes the blocks underneath people fall away
    private void breakBlock(final Block block) {
		new GameRunnable() {
			@Override
			public void run() {
				@SuppressWarnings("deprecation")
				FallingBlock falling = (FallingBlock) block.getWorld().spawnFallingBlock(block.getLocation(), block.getType(), block.getData());
				falling.setDropItem(false);
				falling.setVelocity(new Vector(0, -0.3, 0));
				block.setType(Material.AIR);
			}
		}.runTaskLater(7);
    }
}