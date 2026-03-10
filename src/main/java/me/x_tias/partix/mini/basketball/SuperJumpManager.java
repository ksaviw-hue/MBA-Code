package me.x_tias.partix.mini.basketball;

import me.x_tias.partix.Partix;
import me.x_tias.partix.plugin.athlete.Athlete;
import me.x_tias.partix.plugin.athlete.AthleteManager;
import me.x_tias.partix.plugin.ball.Ball;
import me.x_tias.partix.plugin.ball.BallFactory;
import me.x_tias.partix.plugin.ball.event.PressSwapKeyEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages super jump mechanic for players in basketball games
 * - Press F to perform a super jump (~1 block high)
 * - Only works when player doesn't have the ball
 * - Only works in actual basketball courts
 * - 2 second freeze after landing (slowness)
 * - Jump ability removed for 3 seconds after landing
 * - 7 second cooldown between super jumps
 */
public class SuperJumpManager implements Listener {
    
    // Cooldown tracking
    private static final Map<UUID, Long> superJumpCooldowns = new HashMap<>();
    
    // Track players currently in super jump (to detect landing)
    private static final Map<UUID, Long> activeSuperJumps = new HashMap<>();
    
    // Track players who cannot jump (during landing recovery)
    private static final Map<UUID, Long> jumpBlockedUntil = new HashMap<>();
    
    // Track players in landing recovery (to prevent multiple landing triggers)
    private static final Map<UUID, Boolean> landingRecovery = new HashMap<>();
    
    // Constants
    private static final long SUPER_JUMP_COOLDOWN_MS = 7000; // 7 seconds
    private static final int FREEZE_DURATION_TICKS = 40; // 2 seconds
    private static final int JUMP_REMOVAL_DURATION_TICKS = 40; // 2 seconds (same as freeze)
    private static final int FREEZE_AMPLIFIER = 5; // Very slow (almost frozen)
    private static final double SUPER_JUMP_VELOCITY = 0.6; // ~1.5 blocks high
    private static final double FORWARD_MOMENTUM_MULTIPLIER = 0.4; // Forward momentum boost
    
    // Jump blocker task
    private final BukkitRunnable jumpBlockerTask;
    
    public SuperJumpManager() {
        // Start repeating task to manage jump blocking for players
        jumpBlockerTask = new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                
                // Check if any players in activeSuperJumps (mid-air) have grabbed the ball
                activeSuperJumps.keySet().stream()
                    .forEach(playerId -> {
                        Player player = Partix.getInstance().getServer().getPlayer(playerId);
                        if (player != null && player.isOnline()) {
                            // Check if player has grabbed the ball mid-air
                            boolean hasBall = false;
                            for (Ball ball : BallFactory.getNearby(player.getLocation(), 4.0)) {
                                if (ball.getCurrentDamager() != null && ball.getCurrentDamager().equals(player)) {
                                    hasBall = true;
                                    break;
                                }
                            }
                            
                            // If they grabbed the ball mid-air, cancel the super jump effects
                            if (hasBall) {
                                activeSuperJumps.remove(playerId);
                                landingRecovery.remove(playerId);
                                // Player can move normally - no landing effects will be applied
                            }
                        }
                    });
                
                // Create a copy of keys to avoid ConcurrentModificationException
                jumpBlockedUntil.keySet().stream()
                    .filter(playerId -> {
                        long blockTime = jumpBlockedUntil.get(playerId);
                        return currentTime < blockTime;
                    })
                    .forEach(playerId -> {
                        Player player = Partix.getInstance().getServer().getPlayer(playerId);
                        if (player != null && player.isOnline()) {
                            // Check if player has the ball - if so, allow jumping (for jumpshots)
                            boolean hasBall = false;
                            for (Ball ball : BallFactory.getNearby(player.getLocation(), 4.0)) {
                                if (ball.getCurrentDamager() != null && ball.getCurrentDamager().equals(player)) {
                                    hasBall = true;
                                    break;
                                }
                            }
                            
                            if (!hasBall) {
                                // Player doesn't have ball - block jumping by setting Y velocity to 0
                                Vector velocity = player.getVelocity();
                                if (velocity.getY() > 0) {
                                    velocity.setY(0.0);
                                    player.setVelocity(velocity);
                                }
                            }
                        }
                    });
                
                // Clean up expired entries
                jumpBlockedUntil.entrySet().removeIf(entry -> currentTime >= entry.getValue());
            }
        };
        
        // Run every tick for immediate jump prevention
        jumpBlockerTask.runTaskTimer(Partix.getInstance(), 0L, 1L);
    }
    
    @EventHandler
    public void onSwapKeyPress(PressSwapKeyEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Get athlete and check if in basketball game
        Athlete athlete = AthleteManager.get(playerId);
        if (athlete == null || !(athlete.getPlace() instanceof BasketballGame)) {
            return; // Not in a basketball court
        }
        
        // Check if player has the ball (nearby balls within 4 blocks)
        for (Ball ball : BallFactory.getNearby(player.getLocation(), 4.0)) {
            if (ball.getCurrentDamager() != null && ball.getCurrentDamager().equals(player)) {
                // Player has the ball, don't allow super jump
                return;
            }
        }
        
        // Check if player is on ground (can only super jump from ground)
        if (!player.isOnGround()) {
            return;
        }
        
        // Check cooldown
        long currentTime = System.currentTimeMillis();
        if (superJumpCooldowns.containsKey(playerId)) {
            long lastJumpTime = superJumpCooldowns.get(playerId);
            long timeSinceLastJump = currentTime - lastJumpTime;
            
            if (timeSinceLastJump < SUPER_JUMP_COOLDOWN_MS) {
                return;
            }
        }
        
        // Execute super jump with forward momentum!
        Vector velocity = player.getVelocity();
        velocity.setY(SUPER_JUMP_VELOCITY);
        
        // Add forward momentum based on where player is facing
        Vector direction = player.getLocation().getDirection();
        direction.setY(0); // Only horizontal direction
        direction.normalize();
        direction.multiply(FORWARD_MOMENTUM_MULTIPLIER);
        
        velocity.add(direction);
        player.setVelocity(velocity);
        
        // Track this super jump (for landing detection)
        activeSuperJumps.put(playerId, currentTime);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null) {
            return;
        }
        
        // Only check landing for players who recently performed a super jump
        if (!activeSuperJumps.containsKey(playerId)) {
            return;
        }
        
        // Skip if already in landing recovery (prevent multiple triggers)
        if (landingRecovery.getOrDefault(playerId, false)) {
            return;
        }
        
        // Detect landing: player is on ground and was falling (negative Y velocity)
        if (player.isOnGround() && from.getY() > to.getY()) {
            // Check if they were actually in the air (minimum time check)
            long jumpTime = activeSuperJumps.get(playerId);
            long currentTime = System.currentTimeMillis();
            long airTime = currentTime - jumpTime;
            
            // Only trigger landing if they were in air for at least 200ms (to avoid false positives)
            if (airTime < 200) {
                return;
            }
            
            // Player has landed!
            handleSuperJumpLanding(player);
        }
    }
    
    private void handleSuperJumpLanding(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Mark as in landing recovery
        landingRecovery.put(playerId, true);
        activeSuperJumps.remove(playerId);
        
        // Apply freeze effect (2 seconds of almost frozen)
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                FREEZE_DURATION_TICKS,
                FREEZE_AMPLIFIER,
                false,
                false
        ));
        
        // Track jump block duration (velocity-based blocking handled by task)
        long currentTime = System.currentTimeMillis();
        jumpBlockedUntil.put(playerId, currentTime + (JUMP_REMOVAL_DURATION_TICKS * 50));
        
        // Set cooldown (7 seconds from now)
        superJumpCooldowns.put(playerId, currentTime);
        
        // Schedule task to clear landing recovery flag after cooldown
        new BukkitRunnable() {
            @Override
            public void run() {
                landingRecovery.remove(playerId);
            }
        }.runTaskLater(Partix.getInstance(), SUPER_JUMP_COOLDOWN_MS / 50); // Convert ms to ticks
    }
    
    /**
     * Clean up player data when they leave a game or disconnect
     */
    public static void cleanupPlayer(UUID playerId) {
        superJumpCooldowns.remove(playerId);
        activeSuperJumps.remove(playerId);
        landingRecovery.remove(playerId);
        jumpBlockedUntil.remove(playerId);
    }
}
