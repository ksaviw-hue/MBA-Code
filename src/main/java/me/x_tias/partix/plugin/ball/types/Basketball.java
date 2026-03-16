/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.KeybindComponent
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.format.Style
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 *  net.kyori.adventure.title.Title$Times
 *  net.kyori.adventure.title.TitlePart
 *  org.bukkit.Bukkit
 *  org.bukkit.Color
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.SoundCategory
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.util.Vector
 *  org.jetbrains.annotations.NotNull
 */
package me.x_tias.partix.plugin.ball.types;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import me.x_tias.partix.Partix;
import me.x_tias.partix.database.PlayerDb;
import me.x_tias.partix.mini.basketball.BasketballGame;
import me.x_tias.partix.mini.basketball.ScreenManager;
import me.x_tias.partix.mini.game.GoalGame;
import me.x_tias.partix.mini.game.PlayerStats;
import me.x_tias.partix.plugin.ball.BallFactory;
import me.x_tias.partix.plugin.ball.BallType;
import me.x_tias.partix.plugin.cosmetics.CosmeticBallTrail;
import me.x_tias.partix.plugin.cosmetics.Cosmetics;
import me.x_tias.partix.util.Colour;
import me.x_tias.partix.util.Position;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Basketball
extends me.x_tias.partix.plugin.ball.Ball {
    private boolean perfectShot = false;
    private UUID lastOwnerUUID = null;
    private long perfectShotStartTime = 0L;
    private final BasketballGame game;
    private int ownerTicks;
    public int delay = 0;
    private int handYaw = 50;
    private int handModifier = 5;
    private boolean threeEligible = false;
    private int accuracy = 0;
    private int best = 5;
    private int accuracyWait = 0;
    private static final int STEAL_IMMUNITY_DURATION = 20;
    private int stealImmunityTicks = 0;
    private int blockBounceGraceTime = 0;
    private UUID assister;
    private boolean reboundEligible = false;
    private boolean hadPossessionOnGround = false;
    private boolean travelCalled = false;
    private boolean wasInSlabZone = false;
    private final Map<UUID, Integer> contestTime = new HashMap<UUID, Integer>();
    private boolean shotAttemptRegistered = false;
    private UUID currentShooter = null;
    private boolean shotInFlight = false;
    private boolean layupShot = false;

    public Basketball(Location location, BasketballGame game) {
        super(location, game, BallType.BASKETBALL, 0.4, 0.2, 0.2, 0.015, 0.025, 0.35, 0.01, 0.265, false, false, 2.0, "partix:basketball");
        this.game = game;
        this.ownerTicks = 405;
    }

    @Override
    public Component getControls(Player player) {
        String leftClick = "Pass";
        String rightClick = "Shoot";
        String dropItem = this.canDunk(player) ? "Dunk!" : "Layup";
        String swapHand = "Crossover";
        Component lc = Component.text((String)"[", (TextColor)Colour.blackBorder()).append(((KeybindComponent)Component.keybind((String)"key.attack", (TextColor)Colour.border()).append((Component)Component.text((String)"]", (TextColor)Colour.blackBorder()))).append((Component)Component.text((String)(" " + leftClick + ", "), (TextColor)Colour.darkBorder())));
        Component rc = Component.text((String)"[", (TextColor)Colour.blackBorder()).append(((KeybindComponent)Component.keybind((String)"key.use", (TextColor)Colour.border()).append((Component)Component.text((String)"]", (TextColor)Colour.blackBorder()))).append((Component)Component.text((String)(" " + rightClick + ", "), (TextColor)Colour.darkBorder())));
        Component di = Component.text((String)"[", (TextColor)Colour.blackBorder()).append(((KeybindComponent)Component.keybind((String)"key.drop", (TextColor)Colour.border()).append((Component)Component.text((String)"]", (TextColor)Colour.blackBorder()))).append((Component)Component.text((String)(" " + dropItem + ", "), (TextColor)Colour.darkBorder())));
        Component sh = Component.text((String)"[", (TextColor)Colour.blackBorder()).append(((KeybindComponent)Component.keybind((String)"key.swapOffhand", (TextColor)Colour.border()).append((Component)Component.text((String)"]", (TextColor)Colour.blackBorder()))).append((Component)Component.text((String)(" " + swapHand + ", "), (TextColor)Colour.darkBorder())));
        return lc.append(rc).append(di).append(sh);
    }

    public boolean isThreeEligible() {
        return this.threeEligible;
    }

    public UUID getAssister() {
        return this.assister;
    }

    public void setAssister(UUID assister) {
        this.assister = assister;
    }

    public void throwBall(Player player) {
        if (this.getCurrentDamager() != null && this.getCurrentDamager() == player) {
            if (!player.isOnGround()) {
                this.executeThrow(player);
            } else {
                player.sendMessage("\u00a7cYou must be in the air to shoot!");
            }
            if (this.game.inboundingActive && player.equals((Object)this.game.inbounder)) {
                this.game.dropInboundBarrierButKeepClockFrozen();
            }
        }
    }

    public Location getTargetHoop(Player player) {
        if (player.getLocation().clone().add(player.getLocation().getDirection().multiply(1.0)).getX() < this.game.getCenter().getX()) {
            return this.game.getAwayNet().clone().getCenter().toLocation(player.getWorld()).clone();
        }
        return this.game.getHomeNet().clone().getCenter().toLocation(player.getWorld()).clone();
    }

    private void executeThrow(Player player) {
        float pitch = Math.min(145.0f, Math.max(90.0f, 90.0f + Math.abs(player.getLocation().getPitch()))) - 90.0f;
        this.setLocation(player.getEyeLocation().add(player.getLocation().getDirection().multiply(1.425)));
        Location th = player.getLocation().clone();
        int acc = Math.abs(this.accuracy - this.best);
        float yaw = player.getLocation().getYaw();
        if (acc != 0) {
            yaw = acc <= 1 ? (yaw += (float)(new Random().nextBoolean() ? 1 : -1)) : (acc <= 2 ? (float)((double)yaw + (new Random().nextBoolean() ? 3.5 : -3.5)) : (acc <= 4 ? (yaw += (float)(new Random().nextBoolean() ? 6 : -6)) : (float)((double)yaw + (new Random().nextBoolean() ? 7.5 : -7.5))));
        }
        th.setYaw(yaw);
        // Check if there are 3 solid blocks below the player (when shooting, player is in the air)
        Location checkLoc1 = player.getLocation().subtract(0.0, 1.0, 0.0);
        Location checkLoc2 = player.getLocation().subtract(0.0, 2.0, 0.0);
        Location checkLoc3 = player.getLocation().subtract(0.0, 3.0, 0.0);
        
        Block block1 = checkLoc1.getBlock();
        Block block2 = checkLoc2.getBlock();
        Block block3 = checkLoc3.getBlock();
        
        Material material1 = block1.getType();
        Material material2 = block2.getType();
        Material material3 = block3.getType();
        
        // Three-pointer if all 3 blocks below are solid, otherwise two-pointer
        this.threeEligible = (material1.isSolid() && !material1.isAir() && 
                              material2.isSolid() && !material2.isAir() && 
                              material3.isSolid() && !material3.isAir());
        
        // Visual feedback: find the block player is standing on
        Block standingBlockTemp = null;
        Material standingMaterial = null;
        for (double y = 0.5; y < 4.0; y += 0.5) {
            Location checkLoc = player.getLocation().subtract(0.0, y, 0.0);
            Block block = checkLoc.getBlock();
            Material material = block.getType();
            if (material.isSolid() && !material.isAir()) {
                standingBlockTemp = block;
                standingMaterial = material;
                break;
            }
        }
        
        if (standingBlockTemp != null) {
            final Block standingBlock = standingBlockTemp;
            final Material finalMaterial = standingMaterial;
            standingBlock.setType(this.threeEligible ? Material.RED_CONCRETE : Material.YELLOW_CONCRETE);
            Bukkit.getScheduler().runTaskLater((Plugin)Partix.getInstance(), () -> standingBlock.setType(finalMaterial), 22L);
        }
        if (acc == 0) {
            this.perfectShot = true;
            this.lastOwnerUUID = player.getUniqueId();
            this.perfectShotStartTime = System.currentTimeMillis();
        } else {
            this.perfectShot = false;
        }
        this.game.onShotAttempt(player, this.threeEligible);
        this.currentShooter = player.getUniqueId();
        this.shotInFlight = true;
        Location opposingHoop = this.game.getHomePlayers().contains(player)
                ? this.game.getAwayNet().clone().getCenter().toLocation(player.getWorld())
                : this.game.getHomeNet().clone().getCenter().toLocation(player.getWorld());
        this.layupShot = player.getLocation().distance(opposingHoop) <= 6.0;
        Vector vector = th.getDirection().normalize().multiply(0.3875 + (1.0 - (double)pitch / 45.0) / 2.25);
        double ySpeed = 0.366;
        if (this.game.getCourtLength() == 32.0) {
            vector.multiply(1.1);
        }
        if (this.layupShot) {
            vector.multiply(0.6);
        }
        this.setVelocity(player, vector, ySpeed);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 3, true, false));
        this.delay = 10;
        this.setStealDelay(0);
        this.giveaway();
    }

    public void forceThrow() {
        if (this.getCurrentDamager() != null) {
            Player player = this.getCurrentDamager();
            this.setLocation(player.getEyeLocation().add(player.getLocation().getDirection().multiply(0.45)));
            this.setVelocity(player, player.getLocation().getDirection().multiply(0.6));
            this.giveaway();
            this.threeEligible = false;
            this.delay = 10;
            this.giveaway();
        }
    }

    public boolean pass(Player player) {
        if (this.delay < 1 && this.getCurrentDamager() != null && this.getCurrentDamager().equals((Object)player)) {
            Location location = player.getEyeLocation().clone();
            location.subtract(0.0, 0.5, 0.0);
            Vector direction = player.getLocation().getDirection().clone();
            if (player.isSneaking()) {
                direction.setX(-direction.getX());
                direction.setZ(-direction.getZ());
            }
            double passSpeed = 0.915;
            if (this.game.getCourtLength() == 32.0) {
                passSpeed *= 1.1;
            }
            this.setLocation(location.add(direction.clone().multiply(0.45)));
            this.setVelocity(player, direction.clone().multiply(passSpeed), 0.09);
            this.game.startAssistTimer(player.getUniqueId());
            if (this.game.getState().equals((Object)GoalGame.State.REGULATION) || this.game.getState().equals((Object)GoalGame.State.OVERTIME)) {
                this.game.getStatsManager().getPlayerStats(player.getUniqueId()).incrementPassAttempts();
            }
            this.giveaway();
            this.threeEligible = false;
            this.delay = 5;
            if (this.game.inboundingActive && player.equals((Object)this.game.inbounder)) {
                this.game.dropInboundBarrierButKeepClockFrozen();
            }
            return true;
        }
        return false;
    }

    public Location getNearestHoop(Player player) {
        Location home = this.game.getHomeNet().clone().getCenter().toLocation(player.getWorld());
        Location away = this.game.getAwayNet().clone().getCenter().toLocation(player.getWorld());
        return player.getLocation().distanceSquared(home) < player.getLocation().distanceSquared(away) ? home : away;
    }

    public boolean canDunk(Player player) {
        Location nearest = this.getNearestHoop(player);
        nearest.setY(player.getLocation().getY());
        double dis = nearest.distance(player.getLocation());
        return !player.isOnGround() && player.getLocation().getY() > this.game.getCenter().getY() - 2.0 && dis < 2.0;
    }

    public boolean dunk(Player player) {
        if (this.delay > 0) return false;
        if (this.getCurrentDamager() != null && this.getCurrentDamager().equals((Object)player)) {
            if (!this.isShotAttemptRegistered() && (this.game.getState().equals((Object)GoalGame.State.REGULATION) || this.game.getState().equals((Object)GoalGame.State.OVERTIME))) {
                this.game.onShotAttempt(player, false);
                this.setShotAttemptRegistered(true);
            }
            this.currentShooter = player.getUniqueId();
            this.shotInFlight = true;
            Location target = this.getNearestHoop(player);
            int acc = Math.abs(this.accuracy - this.best);
            if (this.canDunk(player)) {
                double a = acc == 0 ? 0.05 : (acc <= 1 ? (new Random().nextBoolean() ? 0.35 : 0.4) : (acc <= 2 ? (new Random().nextDouble() < 0.1 ? 3.0 : (new Random().nextBoolean() ? 0.75 : 0.85)) : (acc <= 3 ? (new Random().nextBoolean() ? 1.2 : 1.2) : (new Random().nextBoolean() ? 3.0 : 3.0))));
                Location slam = target.clone();
                Location location = player.getLocation().clone();
                location.setPitch(0.0f);
                slam.subtract(target.clone().subtract(player.getLocation()).multiply(0.33));
                Vector fly = player.getLocation().getDirection().multiply(-1.08);
                fly.setY(0.55);
                player.setVelocity(fly);
                this.setLocation(target.clone().add(0.0, 0.85, 0.0));
                // Use horizontal direction from hoop to player so look angle can't nullify the miss velocity
                Vector dunkDir = player.getLocation().toVector().subtract(target.toVector());
                dunkDir.setY(0);
                if (dunkDir.lengthSquared() < 0.001) {
                    dunkDir = new Vector(1, 0, 0);
                }
                dunkDir.normalize();
                this.setVelocity(player, dunkDir.multiply(player.getLocation().distance(target) < 6.75 ? a : 0.0105), -0.2);
            } else {
                double a = acc == 0 ? 0.25 : (acc <= 1 ? (new Random().nextBoolean() ? 0.255 : 0.26) : (acc <= 2 ? (new Random().nextBoolean() ? 0.265 : 0.27) : (acc <= 3 ? (new Random().nextBoolean() ? 0.55 : 0.6) : (new Random().nextBoolean() ? 0.833 : 0.866))));
                this.setLocation(player.getEyeLocation().add(player.getLocation().getDirection().multiply(1.425)));
                this.setVelocity(player, player.getLocation().getDirection().normalize().multiply(player.getLocation().distance(target) < 6.75 ? a : 0.0105), 0.335);
            }
            this.threeEligible = false;
            this.giveaway();
            this.delay = 15;
            return true;
        }
        return false;
    }

    public boolean crossover(Player player) {
        int nextHand;
        if (this.getCurrentDamager() != null && Math.abs(player.getVelocity().getY()) < 0.1 && player.isOnGround() && this.getCurrentDamager().equals((Object)player) && ((nextHand = this.handYaw + this.handModifier) >= 49 || nextHand <= -49)) {
            if (this.handModifier == 0) {
                this.handModifier = 5;
                player.setVelocity(Position.stabilize(player, 70.0f, 0.0).getDirection().multiply(0.75));
                this.delay = 10;
            } else if (this.handModifier == 5) {
                this.handModifier = -5;
                this.delay = 10;
                player.setVelocity(Position.stabilize(player, -70.0f, 0.0).getDirection().multiply(0.75));
            } else if (this.handModifier == -5) {
                this.delay = 10;
                player.setVelocity(Position.stabilize(player, 70.0f, 0.0).getDirection().multiply(0.75));
                this.handModifier = 5;
            }
            return true;
        }
        return false;
    }

    public void giveaway() {
        this.removeCurrentDamager();
    }

    private void runStealImmunity() {
        if (this.stealImmunityTicks > 0) {
            --this.stealImmunityTicks;
        }
        if (this.blockBounceGraceTime > 0) {
            --this.blockBounceGraceTime;
        }
    }

    public void takeBall(Player player) {
        if (this.stealImmunityTicks > 0 && this.getCurrentDamager() != null && !this.getCurrentDamager().equals((Object)player)) {
            this.error(player);
            return;
        }
        if (this.delay < 1 && this.getStealDelay() < 1) {
            // Allow pickup if block bounce grace period is active, otherwise check normal restrictions
            if (this.blockBounceGraceTime <= 0 && this.getLocation().getY() > player.getEyeLocation().getY() - 0.1 && this.getVelocity().getY() < 0.0 && this.getCurrentDamager() == null) {
                this.error(player);
                this.setStealDelay(10);
                return;
            }
            // Ensure rebound eligibility is up-to-date before steal logic
            this.checkReboundEligibility();
            this.steal(player);
            if (this.isReboundEligible() || this.shotInFlight) {
                this.setReboundEligible(false);
                this.shotInFlight = false;
                if (this.game.getState().equals((Object)GoalGame.State.REGULATION) || this.game.getState().equals((Object)GoalGame.State.OVERTIME)) {
                    PlayerStats stats = this.game.getStatsManager().getPlayerStats(player.getUniqueId());
                    
                    // Determine if offensive or defensive rebound
                    boolean isOffensiveRebound = false;
                    if (this.currentShooter != null) {
                        Player shooter = Bukkit.getPlayer(this.currentShooter);
                        if (shooter != null) {
                            GoalGame.Team shooterTeam = this.game.getTeamOf(shooter);
                            GoalGame.Team rebounderTeam = this.game.getTeamOf(player);
                            isOffensiveRebound = (shooterTeam != null && shooterTeam.equals(rebounderTeam));
                        }
                    }
                    
                    if (isOffensiveRebound) {
                        stats.incrementOffensiveReboundsWithMessage(player);
                        System.out.println("Debug: " + player.getName() + " grabbed an OFFENSIVE rebound! Total: " + stats.getOffensiveRebounds());
                    } else {
                        stats.incrementDefensiveReboundsWithMessage(player);
                        System.out.println("Debug: " + player.getName() + " grabbed a DEFENSIVE rebound! Total: " + stats.getDefensiveRebounds());
                    }
                }
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 1));
        } else {
            this.error(player);
        }
    }

    public void error(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ARMOR_STAND_BREAK, SoundCategory.MASTER, 100.0f, 1.0f);
    }

    private void steal(Player player) {
        if (!BallFactory.hasBall(player)) {
            this.layupShot = false;
            if (this.getCurrentDamager() == null) {
                // Only check for steals/blocks/turnovers if this is NOT a rebound or a missed shot
                if (!this.isReboundEligible() && !this.shotInFlight) {
                    // Check for block
                    this.checkAndRecordBlock(player);
                    // Check for interception (pass picked up by opposing team = turnover)
                    Player lastHolder = this.getLastDamager();
                    if (lastHolder != null && lastHolder != player && (this.game.getState().equals((Object)GoalGame.State.REGULATION) || this.game.getState().equals((Object)GoalGame.State.OVERTIME))) {
                        boolean lastHolderIsHome = this.game.getHomePlayers().contains(lastHolder);
                        boolean pickerIsHome = this.game.getHomePlayers().contains(player);
                        if (lastHolderIsHome != pickerIsHome) {
                            PlayerStats pickerStats = this.game.getStatsManager().getPlayerStats(player.getUniqueId());
                            pickerStats.incrementStealsWithMessage(player);
                            PlayerStats passerStats = this.game.getStatsManager().getPlayerStats(lastHolder.getUniqueId());
                            passerStats.incrementTurnoversWithMessage(lastHolder);
                        }
                    }
                }
                this.setDamager(player);
                this.stealImmunityTicks = 20;
                this.setStealDelay(10);
                this.delay = 10;
                this.accuracy = 0;
                this.threeEligible = false;
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.MASTER, 100.0f, 1.2f);
                player.getInventory().setHeldItemSlot(0);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 0));
                return;
            }
            if (this.getCurrentDamager() != player) {
                boolean newTakerIsHome;
                boolean oldOwnerIsHome;
                Player oldOwner = this.getCurrentDamager();
                // Only check for steals/blocks/turnovers if this is NOT a rebound or a missed shot
                if (!this.isReboundEligible() && !this.shotInFlight) {
                    // Check for block
                    this.checkAndRecordBlock(player);
                    if ((this.game.getState().equals((Object)GoalGame.State.REGULATION) || this.game.getState().equals((Object)GoalGame.State.OVERTIME)) && (oldOwnerIsHome = this.game.getHomePlayers().contains(oldOwner)) != (newTakerIsHome = this.game.getHomePlayers().contains(player))) {
                        PlayerStats newStats = this.game.getStatsManager().getPlayerStats(player.getUniqueId());
                        newStats.incrementStealsWithMessage(player);
                        PlayerStats oldStats = this.game.getStatsManager().getPlayerStats(oldOwner.getUniqueId());
                        oldStats.incrementTurnoversWithMessage(oldOwner);
                    }
                }
                oldOwner.playSound(oldOwner.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.MASTER, 100.0f, 0.8f);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.MASTER, 100.0f, 1.2f);
                oldOwner.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 0));
                this.setDamager(player);
                this.stealImmunityTicks = 20;
                this.accuracy = 0;
                player.getInventory().setHeldItemSlot(0);
                this.setStealDelay(10);
                this.delay = 10;
                this.threeEligible = false;
            }
        }
    }

    public boolean collides(Player player) {
        if (this.stealImmunityTicks > 0 && this.getCurrentDamager() != null && !this.getCurrentDamager().equals((Object)player)) {
            return false;
        }
        if (BallFactory.hasBall(player)) {
            return true;
        }
        // Ensure reboundEligible state is up-to-date before deciding rebound vs steal path
        // (collision events can fire before the ball's tick runs checkReboundEligibility)
        this.checkReboundEligibility();
        if (this.getLastDamager() != null) {
            if (this.getStealDelay() > 0) {
                return true;
            }
            if (this.delay < 1 || this.getLastDamager() != null) {
                this.setStealDelay(0);
                if (this.getLastDamager() != player) {
                    if (this.isReboundEligible() || this.shotInFlight) {
                        // Rebound/missed shot pickup - no steal, just award rebound
                        this.setDamager(player);
                        this.setReboundEligible(false);
                        this.shotInFlight = false;
                        if (this.game.getState().equals((Object)GoalGame.State.REGULATION) || this.game.getState().equals((Object)GoalGame.State.OVERTIME)) {
                            PlayerStats stats = this.game.getStatsManager().getPlayerStats(player.getUniqueId());
                            boolean isOffensiveRebound = false;
                            if (this.currentShooter != null) {
                                Player shooter = Bukkit.getPlayer(this.currentShooter);
                                if (shooter != null) {
                                    GoalGame.Team shooterTeam = this.game.getTeamOf(shooter);
                                    GoalGame.Team rebounderTeam = this.game.getTeamOf(player);
                                    isOffensiveRebound = (shooterTeam != null && shooterTeam.equals(rebounderTeam));
                                }
                            }
                            if (isOffensiveRebound) {
                                stats.incrementOffensiveReboundsWithMessage(player);
                            } else {
                                stats.incrementDefensiveReboundsWithMessage(player);
                            }
                        }
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                    } else {
                        // Use steal logic for proper stat tracking and speed boost
                        this.steal(player);
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.MASTER, 100.0f, 1.2f);
                    this.stealImmunityTicks = 20;
                    this.setStealDelay(10);
                    this.delay = 10;
                    return true;
                }
                if (this.delay < 1) {
                    this.setDamager(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.MASTER, 100.0f, 1.2f);
                    this.stealImmunityTicks = 20;
                    this.setStealDelay(10);
                    this.delay = 10;
                    return true;
                }
            } else if (this.getLastDamager() != player) {
                this.setStealDelay(0);
                if (this.isReboundEligible() || this.shotInFlight) {
                    // Rebound/missed shot pickup - no steal, just award rebound
                    this.setDamager(player);
                    this.setReboundEligible(false);
                    this.shotInFlight = false;
                    if (this.game.getState().equals((Object)GoalGame.State.REGULATION) || this.game.getState().equals((Object)GoalGame.State.OVERTIME)) {
                        PlayerStats stats = this.game.getStatsManager().getPlayerStats(player.getUniqueId());
                        boolean isOffensiveRebound = false;
                        if (this.currentShooter != null) {
                            Player shooter = Bukkit.getPlayer(this.currentShooter);
                            if (shooter != null) {
                                GoalGame.Team shooterTeam = this.game.getTeamOf(shooter);
                                GoalGame.Team rebounderTeam = this.game.getTeamOf(player);
                                isOffensiveRebound = (shooterTeam != null && shooterTeam.equals(rebounderTeam));
                            }
                        }
                        if (isOffensiveRebound) {
                            stats.incrementOffensiveReboundsWithMessage(player);
                        } else {
                            stats.incrementDefensiveReboundsWithMessage(player);
                        }
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                } else {
                    // Use steal logic for proper stat tracking and speed boost
                    this.steal(player);
                }
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.MASTER, 100.0f, 1.2f);
                this.stealImmunityTicks = 20;
                this.setStealDelay(10);
                this.delay = 10;
                return true;
            }
        }
        this.setDamager(player);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.MASTER, 100.0f, 1.2f);
        this.stealImmunityTicks = 20;
        return true;
    }

    private void detectTravel() {
        // Only detect travels during active gameplay
        if (!this.game.getState().equals((Object)GoalGame.State.REGULATION) && !this.game.getState().equals((Object)GoalGame.State.OVERTIME)) {
            return;
        }
        
        Player damager;
        if (this.delay < 1 && this.stealImmunityTicks < 1 && (damager = this.getCurrentDamager()) != null && !damager.isOnGround() && damager.getVelocity().getY() < -0.19) {
            damager.sendTitlePart(TitlePart.TITLE, Component.text((String)" "));
            damager.sendTitlePart(TitlePart.SUBTITLE, Component.text((String)"Travel!").style(Style.style((TextColor)Colour.deny(), (TextDecoration[])new TextDecoration[]{TextDecoration.BOLD})));
            
            // Give turnover stat to traveling player
            PlayerStats stats = this.game.getStatsManager().getPlayerStats(damager.getUniqueId());
            stats.incrementTurnoversWithMessage(damager);
            
            // Determine which team gets the inbound (opposing team)
            boolean travelerIsHome = this.game.getHomePlayers().contains(damager);
            GoalGame.Team inboundTeam = travelerIsHome ? GoalGame.Team.AWAY : GoalGame.Team.HOME;
            
            // Check number of players on opposing team
            List<Player> opposingTeam = travelerIsHome ? this.game.getAwayPlayers() : this.game.getHomePlayers();
            
            // Remove the ball first
            this.remove();
            
            // If opposing team has 1 or fewer players, teleport them to ball location
            if (opposingTeam.size() <= 1) {
                Location ballLoc = damager.getLocation();
                for (Player opposingPlayer : opposingTeam) {
                    opposingPlayer.teleport(ballLoc);
                }
            } else {
                // Otherwise, trigger normal inbound for opposing team
                this.game.endTimeout(inboundTeam);
            }
        }
    }

    private void runDelay() {
        if (this.delay > 0) {
            --this.delay;
        }
    }

    private void modifyHand() {
        int nextHand = this.handYaw + this.handModifier;
        if (nextHand < 50 && nextHand > -50) {
            this.handYaw = nextHand;
        }
    }

    public void setReboundEligible(boolean eligible) {
        // Track MISS-AG when shot becomes rebound eligible (missed shot)
        if (eligible && !this.reboundEligible && this.currentShooter != null) {
            // Shot just missed - award MISS-AG to top contesting defender
            UUID topDefender = this.getTopContestedOpponent();
            if (topDefender != null) {
                Player defender = Bukkit.getPlayer(topDefender);
                if (defender != null && this.game != null) {
                    // Verify defender is on opposite team from shooter
                    Player shooter = Bukkit.getPlayer(this.currentShooter);
                    if (shooter != null) {
                        GoalGame.Team shooterTeam = this.game.getTeamOf(shooter);
                        GoalGame.Team defenderTeam = this.game.getTeamOf(defender);
                        if (shooterTeam != null && defenderTeam != null && !shooterTeam.equals(defenderTeam)) {
                            PlayerStats defenderStats = this.game.getStatsManager().getPlayerStats(topDefender);
                            if (defenderStats != null) {
                                defenderStats.incrementMissesAgainst();
                                System.out.println("Debug: " + defender.getName() + " forced a miss! MISS-AG: " + defenderStats.getMissesAgainst());
                            }
                        }
                    }
                }
            }
        }
        // When shot becomes rebound-eligible (hit rim/backboard), it's no longer "in flight"
        // This prevents checkAndRecordBlock() from awarding a block on rebound pickups
        if (eligible && !this.reboundEligible) {
            this.shotInFlight = false;
        }
        this.reboundEligible = eligible;
    }

    public boolean isReboundEligible() {
        return this.reboundEligible;
    }

    public void markReboundEligible() {
        this.reboundEligible = true;
    }

    private void detectReboundPickup() {
        if (!this.game.getState().equals((Object)GoalGame.State.REGULATION) && !this.game.getState().equals((Object)GoalGame.State.OVERTIME)) {
            return;
        }
        if (this.isReboundEligible() && this.getCurrentDamager() != null) {
            Player rebounder = this.getCurrentDamager();
            PlayerStats stats = this.game.getStatsManager().getPlayerStats(rebounder.getUniqueId());
            
            // Determine if offensive or defensive rebound
            boolean isOffensiveRebound = false;
            if (this.currentShooter != null) {
                Player shooter = Bukkit.getPlayer(this.currentShooter);
                if (shooter != null) {
                    GoalGame.Team shooterTeam = this.game.getTeamOf(shooter);
                    GoalGame.Team rebounderTeam = this.game.getTeamOf(rebounder);
                    isOffensiveRebound = (shooterTeam != null && shooterTeam.equals(rebounderTeam));
                }
            }
            
            if (isOffensiveRebound) {
                stats.incrementOffensiveReboundsWithMessage(rebounder);
            } else {
                stats.incrementDefensiveReboundsWithMessage(rebounder);
            }
            
            this.game.getStatsManager().updatePlayerStats(rebounder.getUniqueId(), stats);
            rebounder.playSound(rebounder.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
            this.setReboundEligible(false);
            this.game.cancelAssistTimer();
        }
    }

    public boolean isBallNearGoal(Location goalLocation) {
        Location ballLocation = this.getLocation();
        if (ballLocation == null || goalLocation == null || !ballLocation.getWorld().equals((Object)goalLocation.getWorld())) {
            return false;
        }
        double proximityThreshold = 1.0;
        return ballLocation.distance(goalLocation) <= proximityThreshold;
    }

    private void postModify() {
        this.detectTravel();
        this.modifyHand();
    }

    public BasketballGame getGame() {
        return this.game;
    }

    private void displayAccuracy() {
        if (this.getCurrentDamager() != null) {
            TextComponent a = Component.empty();
            boolean canDunk = this.canDunk(this.getCurrentDamager());
            for (int i = 0; i < 11; ++i) {
                TextColor c;
                int distance = Math.abs(this.accuracy - this.best);
                String s = this.accuracy == i ? (canDunk ? "|" : ",") : (canDunk ? ";" : ".");
                int cDistance = Math.abs(i - this.best);
                
                // Gradient background colors based on distance from center
                TextColor cd;
                if (cDistance == 0) {
                    cd = Colour.allow(); // Green center
                } else if (cDistance == 1) {
                    cd = Colour.partix(); // Yellow
                } else if (cDistance == 2) {
                    cd = TextColor.color(0xFF6600); // Red-orange (less yellow)
                } else if (cDistance == 3) {
                    cd = TextColor.color(0xFF4400); // Deeper red-orange
                } else {
                    cd = Colour.deny(); // Full red
                }
                
                // Indicator colors when cursor is at this position
                if (distance == 0) {
                    c = this.accuracy == i ? TextColor.color((int)9830254) : cd;
                    a = a.append(Component.text((String)s).color(c));
                    continue;
                }
                if (distance <= 1) {
                    c = this.accuracy == i ? TextColor.color((int)16774810) : cd;
                    a = a.append(Component.text((String)s).color(c));
                    continue;
                }
                c = this.accuracy == i ? TextColor.color((int)16758444) : cd;
                a = a.append(Component.text((String)s).color(c));
            }
            this.getCurrentDamager().sendTitlePart(TitlePart.TITLE, Component.text((String)"   "));
            this.getCurrentDamager().sendTitlePart(TitlePart.SUBTITLE, a);
            this.getCurrentDamager().sendTitlePart(TitlePart.TIMES, Title.Times.times((Duration)Duration.ofMillis(0L), (Duration)Duration.ofMillis(100L), (Duration)Duration.ofMillis(350L)));
        }
    }

    private void nextAccuracy() {
        List<Player> home = this.game.getHomePlayers();
        List<Player> away = this.game.getAwayPlayers();
        List<Player> defenders = home.contains(this.getCurrentDamager()) ? away : home;
        List<Player> onBall = this.getLocation().clone().add(this.getCurrentDamager().getLocation().getDirection()).getNearbyPlayers(2.5).stream().filter(defenders::contains).toList();
        if (this.game.getState().equals((Object)GoalGame.State.REGULATION) || this.game.getState().equals((Object)GoalGame.State.OVERTIME)) {
            for (Player defender : onBall) {
                PlayerStats ps = this.game.getStatsManager().getPlayerStats(this.getCurrentDamager().getUniqueId());
                if (ps == null) continue;
                ps.addContestTime(defender.getUniqueId(), 1);
            }
        }
        onBall.forEach(player -> {
            player.sendTitlePart(TitlePart.TITLE, Component.text((String)"   "));
            player.sendTitlePart(TitlePart.SUBTITLE, Component.text((String)".,.,.").color(Colour.deny()));
            player.sendTitlePart(TitlePart.TIMES, Title.Times.times((Duration)Duration.ofMillis(0L), (Duration)Duration.ofMillis(100L), (Duration)Duration.ofMillis(350L)));
        });
        int opp = onBall.size();
        this.accuracyWait += opp > 0 ? 4 : 1;
        if (this.accuracyWait > 1) {
            this.accuracyWait = 0;
            ++this.accuracy;
            if (this.accuracy > 10) {
                this.accuracy = 0;
                this.best = 5;
            }
        }
    }

    @Override
    public void modify() {
        ScreenManager.INSTANCE.tickActiveScreens(this.game);
        if (this.getCurrentDamager() != null) {
            this.nextAccuracy();
            this.displayAccuracy();
            Player poss = this.getCurrentDamager();
            Location l = Position.stabilize(poss, this.handYaw, 0.75);
            l.setY(poss.getEyeLocation().getY() - 0.75);
            double bounceSpeed = Math.max((0.234 + (Math.abs(poss.getVelocity().getX()) + Math.abs(poss.getVelocity().getZ())) * 2.0) * -1.0, -0.75);
            if (this.getLocation().getY() > poss.getEyeLocation().getY() - 0.75) {
                if (this.getLocation().getY() > poss.getEyeLocation().getY() + 0.75) {
                    this.setLocation(l);
                }
                this.setVertical(bounceSpeed);
            }
            if (this.getSpeed() < 0.075) {
                this.setLocation(l);
                this.setVertical(bounceSpeed);
            }
            this.spawnOldBallParticles();
            this.setHorizontal(l);
            this.postModify();
            this.perfectShot = false;
        } else if (this.perfectShot && this.lastOwnerUUID != null) {
            long elapsed = System.currentTimeMillis() - this.perfectShotStartTime;
            if (elapsed < 1900L) {
                PlayerDb.get(this.lastOwnerUUID, PlayerDb.Stat.BALL_TRAIL).thenAccept(ballTrailKey -> {
                    CosmeticBallTrail ballTrail = Cosmetics.ballTrails.get(ballTrailKey);
                    if (ballTrail != null && !ballTrail.getKey().equals("balltrail.default")) {
                        ballTrail.applyEffect(this.getLocation());
                    }
                });
            } else {
                this.perfectShot = false;
            }
        }
        if (this.ownerTicks < 405) {
            this.ownerTicks = 405;
        }
        this.runStealImmunity();
        this.checkReboundEligibility();
        this.detectReboundPickup();
        this.detectMissedShotBySlab();
        this.checkSlabZone();
        this.runDelay();
    }

    public void clearPerfectShot() {
        this.perfectShot = false;
        this.lastOwnerUUID = null;
    }

    private void checkReboundEligibility() {
        Location ballLocation = this.getLocation();
        if (ballLocation == null || ballLocation.getWorld() == null) {
            this.setReboundEligible(false);
            return;
        }
        boolean nearSlab = false;
        for (int x = -3; x <= 3; ++x) {
            for (int y = -2; y <= 2; ++y) {
                for (int z = -3; z <= 3; ++z) {
                    Block block = ballLocation.clone().add((double)x, (double)y, (double)z).getBlock();
                    if (block.getType() != Material.QUARTZ_BLOCK) continue;
                    nearSlab = true;
                    break;
                }
                if (nearSlab) break;
            }
            if (nearSlab) break;
        }
        if (nearSlab) {
            this.setReboundEligible(true);
            if (this.game.getShotClockTicks() < 240) {
                this.game.resetShotClockTo12();
            }
        }
    }

    private void spawnOldBallParticles() {
        Location ballLocation = this.getLocation();
        if (ballLocation == null || ballLocation.getWorld() == null) {
            return;
        }
    }

    @NotNull
    private World getWorld() {
        Location location = this.getLocation();
        if (location != null && location.getWorld() != null) {
            return location.getWorld();
        }
        throw new IllegalStateException("Basketball location or world is not set.");
    }

    private void detectMissedShotBySlab() {
        Player shooter = this.getLastDamager();
        if (shooter == null) {
            return;
        }
        if (!this.game.isShotActive(shooter.getUniqueId())) {
            return;
        }
        Location ballLocation = this.getLocation();
        if (ballLocation == null || ballLocation.getWorld() == null) {
            return;
        }
        boolean nearSlab = false;
        for (int x = -3; x <= 3; ++x) {
            for (double y = -2.5; y <= 3.0; y += 1.0) {
                for (int z = -3; z <= 3; ++z) {
                    Block block = ballLocation.clone().add((double)x, y, (double)z).getBlock();
                    if (block.getType() != Material.QUARTZ_BLOCK) continue;
                    nearSlab = true;
                    break;
                }
                if (nearSlab) break;
            }
            if (nearSlab) break;
        }
        if (nearSlab) {
            if (this.game.getShotClockTicks() < 240) {
                this.game.resetShotClockTo12();
            }
            return;
        }
    }

    private void checkSlabZone() {
        Player shooter;
        Location ballLocation = this.getLocation();
        if (ballLocation == null || ballLocation.getWorld() == null) {
            return;
        }
        boolean inSlabZone = false;
        for (int x = -3; x <= 3; ++x) {
            for (double y = -2.1; y <= 2.1; y += 1.0) {
                for (int z = -3; z <= 3; ++z) {
                    Block block = ballLocation.clone().add((double)x, y, (double)z).getBlock();
                    if (block.getType() != Material.QUARTZ_BLOCK) continue;
                    inSlabZone = true;
                    break;
                }
                if (inSlabZone) break;
            }
            if (inSlabZone) break;
        }
        if (inSlabZone) {
            this.setReboundEligible(true);
        }
        if ((shooter = this.getLastDamager()) == null) {
            this.wasInSlabZone = inSlabZone;
            return;
        }
        UUID shooterId = shooter.getUniqueId();
        boolean activeShot = this.game.isShotActive(shooterId);
        this.wasInSlabZone = inSlabZone;
    }

    public void handleBallInterception(Player opponent) {
        boolean newTakerIsHome;
        boolean oldOwnerIsHome;
        this.game.cancelAssistTimer();
        Player lastDamager = this.getLastDamager();
        if (lastDamager != null && (oldOwnerIsHome = this.game.getHomePlayers().contains(lastDamager)) != (newTakerIsHome = this.game.getHomePlayers().contains(opponent))) {
            PlayerStats stats = this.game.getStatsManager().getPlayerStats(opponent.getUniqueId());
            stats.incrementStealsWithMessage(opponent);
        }
    }

    public void addContestTime(UUID opponent, int time) {
        this.contestTime.merge(opponent, time, Integer::sum);
    }

    public UUID getTopContestedOpponent() {
        return this.contestTime.entrySet().stream().max((e1, e2) -> Integer.compare((Integer)e1.getValue(), (Integer)e2.getValue())).map(Map.Entry::getKey).orElse(null);
    }

    public void resetContestTime() {
        this.contestTime.clear();
    }

    private boolean isPerfectShot() {
        return this.accuracy == 0;
    }

    public boolean isShotAttemptRegistered() {
        return this.shotAttemptRegistered;
    }

    public void setShotAttemptRegistered(boolean shotAttemptRegistered) {
        this.shotAttemptRegistered = shotAttemptRegistered;
    }

    public void initializeAllTeammateHotkeys(me.x_tias.partix.mini.basketball.BasketballGame game) {
    }
    
    public boolean isShouldPreventScore() {
        return false;
    }
    
    @Override
    protected void hitBlock(Location hitLocation, Block block, BlockFace blockFace) {
        super.hitBlock(hitLocation, block, blockFace);
        boolean wasLayup = this.layupShot;
        this.shotInFlight = false;
        this.currentShooter = null;
        this.layupShot = false;
        this.blockBounceGraceTime = wasLayup ? 6 : 10;

        // Reset shot clock to 12 when ball hits backboard (blocks near either net)
        if (this.game != null && (this.game.getState().equals(GoalGame.State.REGULATION) || this.game.getState().equals(GoalGame.State.OVERTIME))) {
            Location blockLoc = block.getLocation();
            org.bukkit.util.BoundingBox homeNet = this.game.getHomeNet();
            org.bukkit.util.BoundingBox awayNet = this.game.getAwayNet();
            boolean nearHome = homeNet != null && blockLoc.toVector().distance(homeNet.getCenter()) < 4.0;
            boolean nearAway = awayNet != null && blockLoc.toVector().distance(awayNet.getCenter()) < 4.0;
            if (nearHome || nearAway) {
                this.game.resetShotClockTo12();
            }
        }
    }
    
    public boolean isLobPass() {
        return false;
    }
    
    public boolean isPlayerInSittingAnimation(Player player) {
        return false;
    }
    
    private void checkAndRecordBlock(Player blocker) {
        if (!this.shotInFlight || this.currentShooter == null) {
            return;
        }
        
        Player shooter = Bukkit.getPlayer(this.currentShooter);
        if (shooter == null) {
            this.shotInFlight = false;
            this.currentShooter = null;
            return;
        }
        
        // Check if blocker is on opposing team from shooter
        if (this.game.getState().equals((Object)GoalGame.State.REGULATION) || this.game.getState().equals((Object)GoalGame.State.OVERTIME)) {
            boolean shooterIsHome = this.game.getHomePlayers().contains(shooter);
            boolean blockerIsHome = this.game.getHomePlayers().contains(blocker);
            
            if (shooterIsHome != blockerIsHome) {
                PlayerStats blockerStats = this.game.getStatsManager().getPlayerStats(blocker.getUniqueId());
                blockerStats.incrementBlocksWithMessage(blocker);
            }
        }
        
        // Clear shot tracking
        this.shotInFlight = false;
        this.currentShooter = null;
    }

    public class Ball {
        private boolean reboundEligible = false;

        public void setReboundEligible(boolean eligible) {
            this.reboundEligible = eligible;
        }

        public boolean isReboundEligible() {
            return this.reboundEligible;
        }

        public void onScored() {
            this.reboundEligible = false;
        }

        public void markReboundEligible() {
            this.reboundEligible = true;
        }
    }
}
