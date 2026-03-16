/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.event.ClickEvent
 *  net.kyori.adventure.text.event.HoverEventSource
 *  net.kyori.adventure.text.format.Style
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 *  org.bukkit.Bukkit
 *  org.bukkit.Color
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Particle
 *  org.bukkit.Particle$DustOptions
 *  org.bukkit.Sound
 *  org.bukkit.SoundCategory
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.boss.BarColor
 *  org.bukkit.boss.BarFlag
 *  org.bukkit.boss.BarStyle
 *  org.bukkit.boss.BossBar
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.scheduler.BukkitTask
 *  org.bukkit.util.Vector
 */
package me.x_tias.partix.mini.basketball;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import me.x_tias.partix.Partix;
import me.x_tias.partix.database.BasketballDb;
import me.x_tias.partix.database.PlayerDb;
import me.x_tias.partix.database.SeasonDb;
import me.x_tias.partix.mini.anteup.AnteUpManager;
import me.x_tias.partix.mini.game.GoalGame;
import me.x_tias.partix.mini.game.PlayerStats;
import me.x_tias.partix.mini.game.PlayerStatsManager;
import me.x_tias.partix.plugin.athlete.Athlete;
import me.x_tias.partix.plugin.athlete.AthleteManager;
import me.x_tias.partix.plugin.ball.Ball;
import me.x_tias.partix.plugin.ball.BallFactory;
import me.x_tias.partix.plugin.ball.BallType;
import me.x_tias.partix.plugin.ball.types.Basketball;
import me.x_tias.partix.plugin.cosmetics.CosmeticSound;
import me.x_tias.partix.plugin.gui.GUI;
import me.x_tias.partix.plugin.gui.ItemButton;
import me.x_tias.partix.plugin.settings.CompType;
import me.x_tias.partix.plugin.settings.GameType;
import me.x_tias.partix.plugin.settings.Settings;
import me.x_tias.partix.plugin.sidebar.Sidebar;
import me.x_tias.partix.plugin.team.BaseTeam;
import me.x_tias.partix.util.Colour;
import me.x_tias.partix.util.Items;
import me.x_tias.partix.util.Message;
import me.x_tias.partix.util.Text;
import me.x_tias.partix.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.util.BoundingBox;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class BasketballGame
extends GoalGame {
    private final PlayerStatsManager statsManager = new PlayerStatsManager();
    private final Map<UUID, Boolean> shotInProgress = new HashMap<UUID, Boolean>();
    private final Location location;
    private final List<UUID> joinedHome = new ArrayList<UUID>();
    private final List<UUID> joinedAway = new ArrayList<UUID>();
    private final HashMap<UUID, Integer> points = new HashMap();
    private final HashMap<UUID, Integer> threes = new HashMap();
    private final Map<UUID, Boolean> activeShots = new HashMap<UUID, Boolean>();
    private int shotClockTicks = 480;
    private BukkitTask timeoutTask = null;
    private int timeoutSecs = 0;
    private boolean shotClockFrozen = false;
    private boolean shotAttemptDetected = false;
    private boolean shotClockStopped = false;
    private boolean inboundTouchedByInbounder = false;
    public boolean inboundingActive = false;
    private boolean inbounderHasReleased = false;
    private GoalGame.Team inboundingTeam;
    private Location inboundSpot;
    public Player inbounder;
    private BukkitTask inboundBarrierTask;
    private BukkitTask inboundTimer;
    private Ball inboundBall;
    private GoalGame.Team shotAttemptTeam = null;
    private GoalGame.Team lastPossessionTeam = null;
    private boolean shotClockEnabled = true;
    private boolean buzzerPlayed = false;
    private UUID currentPossessor = null;
    private long possessionStartTime = 0L;
    private int homeTimeouts = 4;
    private int awayTimeouts = 4;
    private UUID homeCoach = null;
    private UUID awayCoach = null;
    private BossBar timeoutBar;
    private final double courtLength;
    private final Map<String, Object> customProperties = new HashMap<String, Object>();
    private boolean reboundEligible = false;
    private UUID assistEligiblePasser;
    private long assistTimerEndTime;
    private boolean assistTimerActive = false;
    private final List<BallRack> ballRacks = new ArrayList<>();
    private final Map<UUID, Integer> reboundMachineShotsTaken = new HashMap<>();
    private final Map<UUID, Integer> reboundMachineShotsMade = new HashMap<>();
    private final Map<UUID, Integer> reboundMachineShotsMissed = new HashMap<>();
    private BukkitTask pregameTask = null;
    private ShootingMachine homeShootingMachine = null;
    private ShootingMachine awayShootingMachine = null;

    public boolean isHalfCourt1v1 = false;
    public boolean isRecGame = false;
    public boolean isPhysicalQueueGame = false;
    public boolean isSingleHoopMode = false;
    public boolean gameClockFrozenForInbound = false;

    public BasketballGame(Settings settings, Location location, double xDistance, double yDistance, double xLength, double zWidth, double yHeight) {
        this.setup(settings.copy(), location, xDistance, yDistance, xLength, zWidth, yHeight);
        this.courtLength = xDistance;
        this.location = location.clone();
        spawnBallRacks();
    }

    public Location getLocation() {
        return this.location;
    }

    @Override
    public PlayerStatsManager getStatsManager() {
        return this.statsManager;
    }

    public double getCourtLength() {
        return this.courtLength;
    }

    @Override
    public void resetStats() {
        this.statsManager.resetStats();
        for (Player p : this.getHomePlayers()) {
            p.sendMessage(Component.text((String)"Your stats have been reset.").color(Colour.allow()));
        }
        for (Player p : this.getAwayPlayers()) {
            p.sendMessage(Component.text((String)"Your stats have been reset.").color(Colour.allow()));
        }
    }

    public void resetAllStats() {
        System.out.println("Debug: Resetting all player stats.");
        this.statsManager.resetStats();
    }

    @Override
    public void resetShotClock() {
        this.shotClockTicks = 480;
        this.shotClockStopped = false;
        this.shotAttemptDetected = false;
        this.shotAttemptTeam = null;
        this.lastPossessionTeam = null;
    }

    public void callTimeout(final GoalGame.Team callingTeam) {
        int remaining;
        if (this.getState() != GoalGame.State.REGULATION) {
            return;
        }
        if (this.settings.compType == CompType.RANKED) {
            return;
        }
        if (this.courtLength == 26.0) {
            return;
        }
        int n = remaining = callingTeam == GoalGame.Team.HOME ? this.homeTimeouts : this.awayTimeouts;
        if (remaining <= 0) {
            return;
        }
        if (callingTeam == GoalGame.Team.HOME) {
            --this.homeTimeouts;
        } else {
            --this.awayTimeouts;
        }
        this.setState(GoalGame.State.STOPPAGE);
        this.removeBalls();
        this.sendMessage(Component.text((String)(callingTeam.name() + " called a timeout!")).color(Colour.partix()));
        this.updateDisplay();
        this.timeoutBar = Bukkit.createBossBar((String)"Timeout: 60s", (BarColor)BarColor.WHITE, (BarStyle)BarStyle.SOLID, (BarFlag[])new BarFlag[0]);
        this.getPlayers().forEach(arg_0 -> ((BossBar)this.timeoutBar).addPlayer(arg_0));
        this.timeoutSecs = 60;
        if (this.timeoutTask != null) {
            this.timeoutTask.cancel();
            this.timeoutTask = null;
        }
        this.timeoutTask = new BukkitRunnable(){

            public void run() {
                --BasketballGame.this.timeoutSecs;
                BasketballGame.this.timeoutBar.setProgress((double)BasketballGame.this.timeoutSecs / 60.0);
                BasketballGame.this.timeoutBar.setTitle("Timeout: " + BasketballGame.this.timeoutSecs + "s");
                if (BasketballGame.this.timeoutSecs == 10) {
                    List<Player> teamPlayers = callingTeam == GoalGame.Team.HOME ? BasketballGame.this.getHomePlayers() : BasketballGame.this.getAwayPlayers();
                    teamPlayers.forEach(p -> p.sendMessage(Component.text((String)"10 seconds remaining: get ready to inbound!").color(Colour.partix())));
                }
                if (BasketballGame.this.timeoutSecs <= 0) {
                    BasketballGame.this.timeoutBar.removeAll();
                    BasketballGame.this.timeoutTask = null;
                    this.cancel();
                    BasketballGame.this.endTimeout(callingTeam);
                }
            }
        }.runTaskTimer((Plugin)Partix.getInstance(), 20L, 20L);
        this.getPlayers().stream().filter(p -> p.getInventory().contains(Material.POLISHED_BLACKSTONE_BUTTON)).forEach(p -> p.getInventory().remove(Material.POLISHED_BLACKSTONE_BUTTON));
    }

    public void skipTimeoutToTen() {
        if (this.timeoutTask == null) {
            this.getPlayers().forEach(p -> p.sendMessage(Component.text((String)"No timeout in progress to skip").color(Colour.deny())));
            return;
        }
        this.timeoutSecs = 10;
        this.timeoutBar.setTitle("Timeout: 10s");
        this.timeoutBar.setProgress(0.16666666666666666);
    }

    private void enforceHalfCourtBarrier() {
        double centerX = this.getCenter().getX();
        List<Player> opponents = this.inboundingTeam == GoalGame.Team.HOME ? this.getAwayPlayers() : this.getHomePlayers();
        for (Player p : opponents) {
            double x = p.getLocation().getX();
            boolean bl = this.inboundingTeam == GoalGame.Team.HOME ? x < centerX : x > centerX;
            boolean crossed = bl;
            if (!crossed) continue;
            double pushX = this.inboundingTeam == GoalGame.Team.HOME ? 0.5 : -0.5;
            p.setVelocity(new Vector(pushX, 0.0, 0.0));
        }
    }

    public void endTimeout(final GoalGame.Team callingTeam) {
        this.inboundingActive = true;
        this.inboundingTeam = callingTeam;
        this.inboundTouchedByInbounder = false;
        this.inbounderHasReleased = false;
        final Location center = this.getCenter();
        final double centerX = center.getX();
        final double centerZ = center.getZ();
        // Inbound from the sideline at mid-court, on the inbounding team's side of the court
        BoundingBox arenaBox = this.getArenaBox();
        Location sidelineSpot = center.clone();
        if (callingTeam == GoalGame.Team.HOME) {
            sidelineSpot.setX(arenaBox.getMinX() - 0.5); // Home team sideline
            sidelineSpot.setZ(centerZ + 5.0); // On home's half (toward +Z home basket)
        } else {
            sidelineSpot.setX(arenaBox.getMinX() - 0.5); // Same sideline
            sidelineSpot.setZ(centerZ - 5.0); // On away's half (toward -Z away basket)
        }
        sidelineSpot.setY(center.getY() + 1.0);
        this.inboundSpot = sidelineSpot.clone();
        System.out.println("[Inbound Debug] team=" + callingTeam + " center=(" + centerX + ", " + centerZ + ") inboundSpot=(" + this.inboundSpot.getX() + ", " + this.inboundSpot.getZ() + ")");
        List<Player> candidates = callingTeam == GoalGame.Team.HOME ? this.getHomePlayers() : this.getAwayPlayers();
        this.inbounder = candidates.stream().min(Comparator.comparingDouble(p -> p.getLocation().distance(this.inboundSpot))).orElse(null);
        if (this.inbounder == null) {
            this.inboundingActive = false;
            return;
        }
        Location spawnLoc = this.inboundSpot.clone();
        // Face into the court (toward +X from the minX sideline)
        spawnLoc.setYaw(270.0f);
        System.out.println("[Inbound Debug] Teleporting " + this.inbounder.getName() + " to (" + spawnLoc.getX() + ", " + spawnLoc.getY() + ", " + spawnLoc.getZ() + ") yaw=" + spawnLoc.getYaw());
        this.inbounder.teleport(spawnLoc);
        for (int slot = 0; slot < this.inbounder.getInventory().getSize(); ++slot) {
            ItemStack it = this.inbounder.getInventory().getItem(slot);
            if (it == null || it.getType() != Material.POLISHED_BLACKSTONE_BUTTON) continue;
            this.inbounder.getInventory().setItem(slot, null);
            break;
        }
        this.inbounder.updateInventory();
        this.inboundBall = this.setBall(BallFactory.create(this.inboundSpot.clone().add(0.0, 1.0, 0.0), this.getBallType(), this));
        this.inboundBall.setVelocity(0.0, 0.0, 0.0);
        this.setState(GoalGame.State.STOPPAGE);
        this.shotClockStopped = true;
        this.inboundBarrierTask = new BukkitRunnable(){

            public void run() {
                if (!BasketballGame.this.inboundingActive || BasketballGame.this.getState() != GoalGame.State.STOPPAGE || BasketballGame.this.inbounder == null || !BasketballGame.this.inbounder.isOnline()) {
                    this.cancel();
                    BasketballGame.this.inboundBarrierTask = null;
                    return;
                }
                World w = center.getWorld();
                // Draw half-court barrier line across the width of the court (along X-axis at centerZ)
                for (double x = centerX - 30.0; x <= centerX + 30.0; x += 1.0) {
                    w.spawnParticle(Particle.DUST, x, center.getY() + 0.1, centerZ, 1, new Particle.DustOptions(Color.RED, 1.0f));
                }
                // Push opponents back to their own half of the court (Z-axis)
                List<Player> opponents = callingTeam == GoalGame.Team.HOME ? BasketballGame.this.getAwayPlayers() : BasketballGame.this.getHomePlayers();
                for (Player opp : opponents) {
                    Vector push;
                    boolean crossed;
                    double z = opp.getLocation().getZ();
                    if (callingTeam == GoalGame.Team.HOME) {
                        // Home inbounds on +Z side, opponents must stay on -Z side
                        crossed = z > centerZ;
                        push = new Vector(0.0, 0.0, -0.5);
                    } else {
                        // Away inbounds on -Z side, opponents must stay on +Z side
                        crossed = z < centerZ;
                        push = new Vector(0.0, 0.0, 0.5);
                    }
                    if (!crossed) continue;
                    opp.setVelocity(push);
                }
            }
        }.runTaskTimer((Plugin)Partix.getInstance(), 1L, 1L);
        this.inboundTimer = new BukkitRunnable(){
            int secs = 7;

            public void run() {
                Player holder;
                if (!BasketballGame.this.inboundingActive) {
                    this.cancel();
                    BasketballGame.this.inboundTimer = null;
                    return;
                }
                Player player = holder = BasketballGame.this.getBall() != null ? BasketballGame.this.getBall().getCurrentDamager() : null;
                if (!BasketballGame.this.inboundTouchedByInbounder) {
                    if (holder != null && holder.equals((Object)BasketballGame.this.inbounder)) {
                        BasketballGame.this.inboundTouchedByInbounder = true;
                    }
                    return;
                }
                if (!BasketballGame.this.inbounderHasReleased) {
                    if (holder != null && holder.equals((Object)BasketballGame.this.inbounder)) {
                        if (--this.secs > 0 && this.secs <= 5) {
                            for (Player p : BasketballGame.this.getPlayers()) {
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, SoundCategory.MASTER, 1.0f, 1.0f);
                            }
                        }
                        if (this.secs <= 0) {
                            BasketballGame.this.cancelInboundSequence();
                            BasketballGame.this.inboundViolation(callingTeam);
                            this.cancel();
                            BasketballGame.this.inboundTimer = null;
                        }
                        return;
                    }
                    if (holder == null) {
                        BasketballGame.this.inbounderHasReleased = true;
                    }
                }
                if (BasketballGame.this.inbounderHasReleased) {
                    if (holder != null && holder.equals((Object)BasketballGame.this.inbounder)) {
                        BasketballGame.this.cancelInboundSequence();
                        BasketballGame.this.inboundViolation(callingTeam);
                        this.cancel();
                        BasketballGame.this.inboundTimer = null;
                        return;
                    }
                    if (holder != null && !holder.equals((Object)BasketballGame.this.inbounder)) {
                        BasketballGame.this.resumePlay();
                        this.cancel();
                        BasketballGame.this.inboundTimer = null;
                        return;
                    }
                    return;
                }
            }
        }.runTaskTimer((Plugin)Partix.getInstance(), 20L, 20L);
    }

    public void dropInboundBarrierButKeepClockFrozen() {
        if (this.inboundBarrierTask != null) {
            this.inboundBarrierTask.cancel();
            this.inboundBarrierTask = null;
        }
    }

    private void enforceInboundBounds() {
        double dz;
        if (this.inbounder == null) {
            return;
        }
        Vector flatDir = this.inboundSpot.toVector().subtract(this.inbounder.getLocation().toVector()).setY(0).normalize();
        double dx = this.inbounder.getLocation().getX() - this.inboundSpot.getX();
        double dist = Math.hypot(dx, dz = this.inbounder.getLocation().getZ() - this.inboundSpot.getZ());
        if (dist > 0.5) {
            this.inbounder.setVelocity(flatDir.multiply(0.25));
        }
    }

    private void enforceNoProximity() {
        if (this.inbounder == null) {
            return;
        }
        World w = this.inboundSpot.getWorld();
        for (int deg = 0; deg < 360; deg += 10) {
            double rad = Math.toRadians(deg);
            double x = this.inboundSpot.getX() + Math.cos(rad) * 3.0;
            double z = this.inboundSpot.getZ() + Math.sin(rad) * 3.0;
            w.spawnParticle(Particle.DUST, x, this.inboundSpot.getY() + 0.1, z, 1, new Particle.DustOptions(Color.RED, 1.0f));
        }
        for (Player p : this.getPlayers()) {
            Location loc;
            if (p.equals((Object)this.inbounder) || !((loc = p.getLocation()).distance(this.inboundSpot) < 3.0)) continue;
            Vector dir = loc.toVector().subtract(this.inboundSpot.toVector()).setY(0).normalize();
            p.setVelocity(dir.multiply(0.5));
        }
    }

    public void inboundViolation(GoalGame.Team callingTeam) {
        final GoalGame.Team next = callingTeam == GoalGame.Team.HOME ? GoalGame.Team.AWAY : GoalGame.Team.HOME;
        String title = "\u00a7c\u00a7lInbound Violation!";
        String subtitle = "\u00a7fNext: " + next.name();
        for (Player p : this.getPlayers()) {
            p.sendTitle(title, subtitle, 10, 40, 10);
        }
        this.removeBalls();
        this.resetShotClock();
        this.setState(GoalGame.State.STOPPAGE);
        this.cancelInboundSequence();
        new BukkitRunnable(){

            public void run() {
                BasketballGame.this.endTimeout(next);
            }
        }.runTaskLater((Plugin)Partix.getInstance(), 100L);
    }

    private void resumePlay() {
        this.inboundingActive = false;
        if (this.inboundBarrierTask != null) {
            this.inboundBarrierTask.cancel();
            this.inboundBarrierTask = null;
        }
        if (this.inboundTimer != null) {
            this.inboundTimer.cancel();
            this.inboundTimer = null;
        }
        this.setState(GoalGame.State.REGULATION);
        this.shotClockStopped = false;
    }

    private void updatePossessionTime() {
        Player currentHolder;
        if (!this.getState().equals((Object)GoalGame.State.REGULATION) && !this.getState().equals((Object)GoalGame.State.OVERTIME)) {
            this.currentPossessor = null;
            return;
        }
        long now = System.currentTimeMillis();
        Player player = currentHolder = this.getBall() != null ? this.getBall().getCurrentDamager() : null;
        if (currentHolder != null) {
            UUID holderId = currentHolder.getUniqueId();
            if (!holderId.equals(this.currentPossessor)) {
                if (this.currentPossessor != null) {
                    long duration = now - this.possessionStartTime;
                    PlayerStats stats = this.statsManager.getPlayerStats(this.currentPossessor);
                    stats.addPossessionTime(duration);
                }
                this.currentPossessor = holderId;
                this.possessionStartTime = now;
            }
        } else if (this.currentPossessor != null) {
            long duration = now - this.possessionStartTime;
            PlayerStats stats = this.statsManager.getPlayerStats(this.currentPossessor);
            stats.addPossessionTime(duration);
            this.currentPossessor = null;
        }
    }

    private void updateStamina() {
        // Only update stamina during active play (REGULATION or OVERTIME)
        if (!this.getState().equals((Object)GoalGame.State.REGULATION) && !this.getState().equals((Object)GoalGame.State.OVERTIME)) {
            return;
        }
        
        long now = System.currentTimeMillis();
        // Stamina drain: 1 stamina per 60 seconds on court = 0.05 stamina per second (50 ticks)
        // Stamina gain: 2 stamina per 60 seconds on bench = 0.1 stamina per second (50 ticks)
        
        for (Player player : this.getHomePlayers()) {
            this.updatePlayerStamina(player, now);
        }
        for (Player player : this.getAwayPlayers()) {
            this.updatePlayerStamina(player, now);
        }
    }
    
    private void updateActivePlayTime() {
        // Only track active play time during REGULATION or OVERTIME
        if (!this.getState().equals((Object)GoalGame.State.REGULATION) && !this.getState().equals((Object)GoalGame.State.OVERTIME)) {
            return;
        }
        
        long now = System.currentTimeMillis();
        
        // Update for home players
        for (Player player : this.getHomePlayers()) {
            if (!this.isInBench(player)) {
                // Player is on the court, track time
                PlayerStats stats = this.statsManager.getPlayerStats(player.getUniqueId());
                if (stats != null) {
                    long timeSinceLastUpdate = now - stats.getLastActivePlayUpdate();
                    if (timeSinceLastUpdate > 0) {
                        stats.addActivePlayTime(timeSinceLastUpdate);
                        stats.setLastActivePlayUpdate(now);
                    }
                }
            } else {
                // Player is on bench, just update the timestamp
                PlayerStats stats = this.statsManager.getPlayerStats(player.getUniqueId());
                if (stats != null) {
                    stats.setLastActivePlayUpdate(now);
                }
            }
        }
        
        // Update for away players
        for (Player player : this.getAwayPlayers()) {
            if (!this.isInBench(player)) {
                // Player is on the court, track time
                PlayerStats stats = this.statsManager.getPlayerStats(player.getUniqueId());
                if (stats != null) {
                    long timeSinceLastUpdate = now - stats.getLastActivePlayUpdate();
                    if (timeSinceLastUpdate > 0) {
                        stats.addActivePlayTime(timeSinceLastUpdate);
                        stats.setLastActivePlayUpdate(now);
                    }
                }
            } else {
                // Player is on bench, just update the timestamp
                PlayerStats stats = this.statsManager.getPlayerStats(player.getUniqueId());
                if (stats != null) {
                    stats.setLastActivePlayUpdate(now);
                }
            }
        }
    }
    
    private void updatePlayerStamina(Player player, long currentTime) {
        PlayerStats stats = this.statsManager.getPlayerStats(player.getUniqueId());
        if (stats == null) return;
        
        long timeSinceLastUpdate = currentTime - stats.getLastStaminaUpdate();
        if (timeSinceLastUpdate < 1000) return; // Update every second
        
        double secondsElapsed = timeSinceLastUpdate / 1000.0;
        stats.setLastStaminaUpdate(currentTime);
        
        boolean onBench = this.isInBench(player);
        double staminaChange;
        
        if (onBench) {
            // Gain 2 stamina per minute = 2/60 per second
            staminaChange = (2.0 / 60.0) * secondsElapsed;
            double newStamina = Math.min(stats.getCurrentStamina() + staminaChange, stats.getMaxStamina());
            stats.setCurrentStamina(newStamina);
        } else {
            // Lose 1 stamina per minute = 1/60 per second
            staminaChange = (1.0 / 60.0) * secondsElapsed;
            double newStamina = Math.max(stats.getCurrentStamina() - staminaChange, 0.0);
            stats.setCurrentStamina(newStamina);
        }
        
        // Update player's hunger bar to reflect stamina (20 stamina = 20 food level)
        int foodLevel = (int) Math.ceil(stats.getCurrentStamina());
        player.setFoodLevel(foodLevel);
        
        // Apply speed effects based on stamina
        this.applyStaminaSpeedEffect(player, stats);
    }
    
    private void applyStaminaSpeedEffect(Player player, PlayerStats stats) {
        float speedMod = stats.getSpeedModifier();
        // Remove any existing speed effects from stamina
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.SPEED);
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS);
        
        if (speedMod < 1.0f) {
            // Apply slowness based on stamina level
            // Speed reduction: 1.0 = no effect, 0.95 = slight slow, 0.75 = very slow
            int amplifier = (int) Math.round((1.0f - speedMod) * 10);
            if (amplifier > 0) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOWNESS,
                    40, // 2 seconds duration (refreshed constantly)
                    amplifier - 1, // Amplifier 0 = Slowness I
                    true, // ambient
                    false // show particles
                ));
            }
        }
    }

    public void onShotAttempt(Player shooter, boolean isThree) {
        if (shooter == null) {
            return;
        }
        // Track rebound machine shot attempts (pregame only)
        if (this.settings.reboundMachineEnabled && this.getState().equals(GoalGame.State.PREGAME)) {
            UUID shooterId = shooter.getUniqueId();
            this.reboundMachineShotsTaken.put(shooterId, this.reboundMachineShotsTaken.getOrDefault(shooterId, 0) + 1);
        }
        GoalGame.State st = this.getState();
        if (!st.equals((Object)GoalGame.State.REGULATION) && !st.equals((Object)GoalGame.State.OVERTIME)) {
            return;
        }
        PlayerStats stats = this.statsManager.getPlayerStats(shooter.getUniqueId());
        stats.incrementFGAttempted();
        if (isThree) {
            stats.increment3FGAttempted();
        }
        this.shotInProgress.put(shooter.getUniqueId(), isThree);
    }

    public GoalGame.Team getTeamOf(Player p) {
        return this.getHomePlayers().contains(p) ? GoalGame.Team.HOME : GoalGame.Team.AWAY;
    }

    @Override
    public void enableShotClock() {
        this.shotClockEnabled = true;
    }

    @Override
    public void disableShotClock() {
        this.shotClockEnabled = false;
    }

    private void shotClockViolation() {
        this.shotClockStopped = true;
        this.sendMessage(Component.text((String)"SHOT CLOCK VIOLATION!").color(Colour.deny()));
        this.removeBalls();
        GoalGame.Team violationTeam = this.lastPossessionTeam;
        // Award sideline inbound to the non-violating team
        GoalGame.Team inboundTeam = violationTeam == GoalGame.Team.HOME ? GoalGame.Team.AWAY : GoalGame.Team.HOME;
        this.lastPossessionTeam = null;
        this.shotClockTicks = 480;
        this.shotAttemptDetected = false;
        this.shotAttemptTeam = null;
        this.setState(GoalGame.State.STOPPAGE);
        this.endTimeout(inboundTeam);
    }

    private void stopPlayDueToShotClockViolation() {
        this.setState(GoalGame.State.STOPPAGE);
        this.removeBalls();
        this.startCountdown(GoalGame.State.FACEOFF, 10);
    }

    private void updateActionBarShotClock() {
        String shotClockDisplay;
        if (!this.shotClockEnabled || this.getState().equals((Object)GoalGame.State.STOPPAGE)) {
            for (Player p : this.getPlayers()) {
                p.sendActionBar((Component)Component.empty());
            }
            return;
        }
        double secondsRemaining = (double)this.shotClockTicks / 20.0;
        String string = shotClockDisplay = secondsRemaining < 5.0 ? String.format("%.1f", secondsRemaining) : String.valueOf((int)Math.ceil(secondsRemaining));
        TextColor clockColor = secondsRemaining > 16.0 ? TextColor.color((int)65280) : (secondsRemaining > 8.0 ? TextColor.color((int)0xFFFF00) : TextColor.color((int)0xFF0000));
        TextComponent timeComponent = Component.text((String)shotClockDisplay, (TextColor)clockColor);
        if (secondsRemaining <= 3.0) {
            timeComponent = timeComponent.decorate(TextDecoration.BOLD);
        }
        Component display = ((TextComponent)Component.text((String)"Shot Clock: ", (TextColor)TextColor.color((int)0xFFFFFF)).decorate(TextDecoration.BOLD)).append((Component)timeComponent);
        for (Player p : this.getPlayers()) {
            p.sendActionBar(display);
        }
    }

    public void resetShotClockTo12() {
        this.shotClockTicks = 240;
        this.buzzerPlayed = false;
    }

    public int getShotClockTicks() {
        return this.shotClockTicks;
    }

    @Override
    public void onTick() {
        super.onTick();
        // Update stamina system if enabled
        if (this.settings.staminaEnabled && !this.settings.compType.equals((Object)CompType.RANKED)) {
            this.updateStamina();
        }
        if (this.inboundingActive) {
            Player holder;
            Player player = holder = this.getBall() != null ? this.getBall().getCurrentDamager() : null;
            if (!this.inboundTouchedByInbounder) {
                if (holder != null && holder.equals((Object)this.inbounder)) {
                    this.inboundTouchedByInbounder = true;
                }
                this.enforceInboundBounds();
                this.enforceNoProximity();
                return;
            }
            if (this.inboundTouchedByInbounder && holder == null) {
                return;
            }
            if (holder != null && !holder.equals((Object)this.inbounder)) {
                this.resumePlay();
                if (this.inboundTimer != null) {
                    this.inboundTimer.cancel();
                    this.inboundTimer = null;
                }
                return;
            }
            this.enforceInboundBounds();
            this.enforceNoProximity();
            return;
        }
        this.updateShotClock();
        this.updateActionBarShotClock();
        this.updatePossessionTime();
        this.updateActivePlayTime();
    }

    private void updateShotClock() {
        GoalGame.Team currentTeam;
        Player possessor;
        if (!this.shotClockEnabled || this.shotClockStopped || this.getState().equals((Object)GoalGame.State.STOPPAGE)) {
            return;
        }
        int gameTimeLeft = this.getTimeTicks();
        if (this.shotClockTicks > gameTimeLeft) {
            return;
        }
        Player player = possessor = this.getBall() != null ? this.getBall().getCurrentDamager() : null;
        if (possessor == null && this.lastPossessionTeam == null) {
            return;
        }
        GoalGame.Team team = currentTeam = possessor != null ? this.getTeamOf(possessor) : this.lastPossessionTeam;
        if (this.lastPossessionTeam != null && currentTeam != this.lastPossessionTeam) {
            this.shotClockTicks = 480;
            this.shotAttemptDetected = false;
            this.shotAttemptTeam = null;
            this.buzzerPlayed = false;
        } else if (this.shotAttemptDetected && this.shotAttemptTeam == currentTeam && this.shotClockTicks < 240) {
            this.shotClockTicks = 240;
            this.shotAttemptDetected = false;
            this.shotAttemptTeam = null;
            this.buzzerPlayed = false;
        }
        this.lastPossessionTeam = currentTeam;
        if (this.shotClockTicks > 0) {
            --this.shotClockTicks;
            this.buzzerPlayed = false;
            double secondsRemaining = (double)this.shotClockTicks / 20.0;
            if (secondsRemaining <= 5.0 && this.shotClockTicks % 20 == 0) {
                for (Player p : this.getPlayers()) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, SoundCategory.MASTER, 1.0f, 1.0f);
                }
            }
        } else {
            this.shotClockTicks = 0;
            if (!this.buzzerPlayed) {
                for (Player p : this.getPlayers()) {
                    p.playSound(p.getLocation(), "shotclockbuzzer", SoundCategory.MASTER, 1.0f, 1.0f);
                }
                this.buzzerPlayed = true;
            }
            if (this.getBall() != null) {
                double threshold;
                double ballY = this.getBall().getLocation().getY();
                if (ballY <= (threshold = this.getCenter().getY() + 2.0)) {
                    this.shotClockViolation();
                }
            } else {
                this.shotClockViolation();
            }
        }
    }

    @Override
    public void setPregame() {
        World world = this.getCenter().getWorld();
        Block h = this.getHomeNet().clone().getCenter().toLocation(world).getBlock();
        h.getLocation().clone().getBlock().setType(Material.AIR);
        h.getLocation().clone().subtract(0.0, 1.0, 0.0).getBlock().setType(Material.AIR);
        Block a = this.getAwayNet().clone().getCenter().toLocation(world).getBlock();
        a.getLocation().clone().getBlock().setType(Material.AIR);
        a.getLocation().clone().subtract(0.0, 1.0, 0.0).getBlock().setType(Material.AIR);
        this.resetShotClock();

        // Spawn ball racks on the wings of the three-point arc at both ends
        spawnBallRacks();
        startPregameTask();
    }

    private void spawnBallRacks() {
        removeBallRacks();

        Location center = this.getCenter();
        double wingX = 10.0;   // X offset for wing position (sideline direction)
        double homeZ = this.getHomeNet().getCenterZ() - 8.0; // Z near home basket
        double awayZ = this.getAwayNet().getCenterZ() + 8.0; // Z near away basket

        // Home end - left wing rack (diagonal, facing court center)
        ballRacks.add(new BallRack(
                new Location(center.getWorld(), center.getX() - wingX, center.getY(), homeZ),
                -45f, this
        ));
        // Home end - right wing rack
        ballRacks.add(new BallRack(
                new Location(center.getWorld(), center.getX() + wingX, center.getY(), homeZ),
                45f, this
        ));

        // Away end - left wing rack
        ballRacks.add(new BallRack(
                new Location(center.getWorld(), center.getX() - wingX, center.getY(), awayZ),
                -135f, this
        ));
        // Away end - right wing rack
        ballRacks.add(new BallRack(
                new Location(center.getWorld(), center.getX() + wingX, center.getY(), awayZ),
                135f, this
        ));
    }

    private void removeBallRacks() {
        for (BallRack rack : ballRacks) {
            rack.remove();
        }
        ballRacks.clear();
    }

    public void startPregameTask() {
        if (pregameTask != null) {
            pregameTask.cancel();
        }
        pregameTask = Bukkit.getScheduler().runTaskTimer(Partix.getInstance(), () -> {
            if (getState().equals(GoalGame.State.PREGAME) && settings.reboundMachineEnabled) {
                reboundDetection();
                // Rotate shooting machines toward closest player
                if (homeShootingMachine != null) {
                    homeShootingMachine.rotateTowardClosest(this.getPlayers());
                }
                if (awayShootingMachine != null) {
                    awayShootingMachine.rotateTowardClosest(this.getPlayers());
                }
            }
        }, 0L, 1L);
    }

    private void reboundDetection() {
        List<Ball> nearbyBalls = BallFactory.getNearby(this.getCenter(), 100.0);

        for (Ball ball : nearbyBalls) {
            if (!(ball instanceof Basketball basketball)) continue;

            Location ballLoc = basketball.getLocation();
            Vector ballVec = ballLoc.toVector();

            boolean inHomeNet = this.getHomeNet().clone().expand(0.3).contains(ballVec);
            boolean inAwayNet = this.getAwayNet().clone().expand(0.3).contains(ballVec);

            // MADE SHOT
            if ((inHomeNet || inAwayNet) && basketball.getVelocity().getY() < 0.15) {
                Player shooter = basketball.getLastDamager();
                if (shooter != null && shooter.isOnline()) {
                    UUID shooterId = shooter.getUniqueId();
                    int currentAttempts = reboundMachineShotsTaken.getOrDefault(shooterId, 0);
                    int currentMakes = reboundMachineShotsMade.getOrDefault(shooterId, 0);
                    int currentMisses = reboundMachineShotsMissed.getOrDefault(shooterId, 0);

                    if (currentMakes + currentMisses < currentAttempts) {
                        reboundMachineShotsMade.put(shooterId, currentMakes + 1);
                        int shotsTaken = currentAttempts;
                        int shotsMade = currentMakes + 1;
                        int shotsMissed = currentMisses;
                        double percentage = ((double) shotsMade / shotsTaken) * 100;
                        shooter.sendMessage(Component.text("\u2713 MAKE | Shots: " + shotsTaken + " | Makes: " + shotsMade + " | Misses: " + shotsMissed + " | FG%: " + String.format("%.1f", percentage) + "%").color(Colour.allow()));

                        Location hoopLocation = inHomeNet ? this.getHomeNet().getCenter().toLocation(ballLoc.getWorld()) : this.getAwayNet().getCenter().toLocation(ballLoc.getWorld());

                        // Use shooting machine launcher position if available
                        ShootingMachine machine = inHomeNet ? homeShootingMachine : awayShootingMachine;
                        Location spawnLoc = (machine != null && machine.isAlive()) ? machine.getLauncherLocation() : hoopLocation.clone().subtract(0, 2.0, 0);

                        basketball.remove();

                        Bukkit.getScheduler().runTaskLater(Partix.getInstance(), () -> {
                            if (shooter.isOnline()) {
                                Ball newBall = BallFactory.create(spawnLoc, BallType.BASKETBALL, this);
                                Location playerLoc = shooter.getLocation();
                                Vector direction = playerLoc.toVector().subtract(spawnLoc.toVector()).normalize();
                                newBall.setVelocity(direction.getX() * 0.45, 0.2, direction.getZ() * 0.45);
                                shooter.playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1.2f);
                            }
                        }, 10L);
                    }
                }
            }
            // MISSED SHOT
            else if (basketball.getVelocity().getY() < -0.3 && ballLoc.getY() < this.getCenter().getY() + 1.0) {
                Player shooter = basketball.getLastDamager();
                if (shooter != null && shooter.isOnline()) {
                    UUID shooterId = shooter.getUniqueId();
                    int currentAttempts = reboundMachineShotsTaken.getOrDefault(shooterId, 0);
                    int currentMakes = reboundMachineShotsMade.getOrDefault(shooterId, 0);
                    int currentMisses = reboundMachineShotsMissed.getOrDefault(shooterId, 0);

                    if (currentMakes + currentMisses < currentAttempts) {
                        reboundMachineShotsMissed.put(shooterId, currentMisses + 1);
                        int shotsTaken = currentAttempts;
                        int shotsMade = currentMakes;
                        int shotsMissed = currentMisses + 1;
                        double percentage = ((double) shotsMade / shotsTaken) * 100;
                        shooter.sendMessage(Component.text("\u2717 MISS | Shots: " + shotsTaken + " | Makes: " + shotsMade + " | Misses: " + shotsMissed + " | FG%: " + String.format("%.1f", percentage) + "%").color(Colour.deny()));

                        basketball.remove();

                        // Pick closest hoop for return
                        Location underRim;
                        ShootingMachine machine;
                        double homeDistSq = ballLoc.distanceSquared(this.getHomeNet().getCenter().toLocation(ballLoc.getWorld()));
                        double awayDistSq = ballLoc.distanceSquared(this.getAwayNet().getCenter().toLocation(ballLoc.getWorld()));
                        if (homeDistSq < awayDistSq) {
                            machine = homeShootingMachine;
                            underRim = this.getHomeNet().getCenter().toLocation(ballLoc.getWorld()).subtract(0, 2.0, 0);
                        } else {
                            machine = awayShootingMachine;
                            underRim = this.getAwayNet().getCenter().toLocation(ballLoc.getWorld()).subtract(0, 2.0, 0);
                        }
                        Location spawnLoc = (machine != null && machine.isAlive()) ? machine.getLauncherLocation() : underRim;

                        Bukkit.getScheduler().runTaskLater(Partix.getInstance(), () -> {
                            if (shooter.isOnline()) {
                                Ball newBall = BallFactory.create(spawnLoc, BallType.BASKETBALL, this);
                                Location playerLoc = shooter.getLocation();
                                Vector direction = playerLoc.toVector().subtract(spawnLoc.toVector()).normalize();
                                newBall.setVelocity(direction.getX() * 0.45, 0.2, direction.getZ() * 0.45);
                                shooter.playSound(shooter.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.3f, 0.8f);
                            }
                        }, 10L);
                    }
                }
            }
        }
    }

    public void spawnShootingMachines() {
        removeShootingMachines();
        World world = this.getCenter().getWorld();
        double floorY = this.getCenter().getY();
        double courtCenterX = this.getCenter().getX();
        double courtCenterZ = this.getCenter().getZ();
        Location homeHoopCenter = this.getHomeNet().getCenter().toLocation(world);
        homeShootingMachine = new ShootingMachine(homeHoopCenter, floorY, courtCenterX, courtCenterZ);
        if (!this.isSingleHoopMode) {
            Location awayHoopCenter = this.getAwayNet().getCenter().toLocation(world);
            awayShootingMachine = new ShootingMachine(awayHoopCenter, floorY, courtCenterX, courtCenterZ);
        }
    }

    public void removeShootingMachines() {
        if (homeShootingMachine != null) {
            homeShootingMachine.remove();
            homeShootingMachine = null;
        }
        if (awayShootingMachine != null) {
            awayShootingMachine.remove();
            awayShootingMachine = null;
        }
    }

    @Override
    public void setFaceoff() {
        World world = this.getCenter().getWorld();
        this.removeBalls();
        this.removeBallRacks();
        this.removeShootingMachines();
        // Cancel pregame task when leaving pregame
        if (pregameTask != null) {
            pregameTask.cancel();
            pregameTask = null;
        }
        Location h = this.getHomeNet().clone().getCenter().toLocation(world);
        h.getBlock().setType(Material.AIR);
        h.subtract(0.0, 1.0, 0.0).getBlock().setType(Material.BARRIER);
        Location a = this.getAwayNet().clone().getCenter().toLocation(world);
        a.getBlock().setType(Material.AIR);
        a.subtract(0.0, 1.0, 0.0).getBlock().setType(Material.BARRIER);
    }

    @Override
    public void dropBall() {
        Location spawn = this.getCenter().add(0.0, 1.5 + Math.random() / 1.5, 0.0);
        Ball ball = this.setBall(BallFactory.create(spawn, BallType.BASKETBALL, this));
        ball.setVelocity(0.0, 0.1 + Math.random() / 3.0, new Random().nextBoolean() ? Math.max(0.05 + (0.05 + Math.random()) / 25.0, 0.05) / 3.0 : Math.min(-0.05 + (-0.5 - Math.random()) / 25.0, -0.05) / 3.0);
    }

    @Override
    public boolean periodIsComplete(int ticksRemaining) {
        if (this.getBall() != null && this.getBall().getCurrentDamager() == null && this.getBall().getLocation().getY() > this.getCenter().getY() + 2.0) {
            this.addTime(1);
            return false;
        }
        this.resetShotClock();
        return true;
    }

    @Override
    public void gameOver(GoalGame.Team winner) {
        this.sendTitle(((TextComponent)((TextComponent)Component.text((String)"The ").color(Colour.partix())).append(winner.equals((Object)GoalGame.Team.HOME) ? this.home.name : this.away.name)).append(Component.text((String)" Win!").color(Colour.bold())));
        if (this.settings.compType.equals((Object)CompType.RANKED) && (this.getHomePlayers().size() > 1 || this.getAwayPlayers().size() > 1)) {
            if (winner.equals((Object)GoalGame.Team.HOME)) {
                this.getHomePlayers().forEach(uuid -> {
                    BasketballDb.add(uuid.getUniqueId(), BasketballDb.Stat.WINS, 1);
                    SeasonDb.get(uuid.getUniqueId(), SeasonDb.Stat.WINS).thenAccept(wins -> 
                        SeasonDb.set(uuid.getUniqueId(), SeasonDb.Stat.WINS, wins + 1));
                    SeasonDb.get(uuid.getUniqueId(), SeasonDb.Stat.POINTS).thenAccept(points -> 
                        SeasonDb.set(uuid.getUniqueId(), SeasonDb.Stat.POINTS, points + 2));
                    AthleteManager.get(uuid.getUniqueId()).giveCoins(10, true);
                });
                this.getAwayPlayers().forEach(uuid -> {
                    BasketballDb.add(uuid.getUniqueId(), BasketballDb.Stat.LOSSES, 1);
                    SeasonDb.get(uuid.getUniqueId(), SeasonDb.Stat.LOSSES).thenAccept(losses -> 
                        SeasonDb.set(uuid.getUniqueId(), SeasonDb.Stat.LOSSES, losses + 1));
                    SeasonDb.get(uuid.getUniqueId(), SeasonDb.Stat.POINTS).thenAccept(points -> {
                        int newPoints = Math.max(0, points - 1);
                        SeasonDb.set(uuid.getUniqueId(), SeasonDb.Stat.POINTS, newPoints);
                    });
                    AthleteManager.get(uuid.getUniqueId()).giveCoins(5, true);
                });
            } else {
                this.getAwayPlayers().forEach(uuid -> {
                    BasketballDb.add(uuid.getUniqueId(), BasketballDb.Stat.WINS, 1);
                    SeasonDb.get(uuid.getUniqueId(), SeasonDb.Stat.WINS).thenAccept(wins -> 
                        SeasonDb.set(uuid.getUniqueId(), SeasonDb.Stat.WINS, wins + 1));
                    SeasonDb.get(uuid.getUniqueId(), SeasonDb.Stat.POINTS).thenAccept(points -> 
                        SeasonDb.set(uuid.getUniqueId(), SeasonDb.Stat.POINTS, points + 2));
                    AthleteManager.get(uuid.getUniqueId()).giveCoins(10, true);
                });
                this.getHomePlayers().forEach(uuid -> {
                    BasketballDb.add(uuid.getUniqueId(), BasketballDb.Stat.LOSSES, 1);
                    SeasonDb.get(uuid.getUniqueId(), SeasonDb.Stat.LOSSES).thenAccept(losses -> 
                        SeasonDb.set(uuid.getUniqueId(), SeasonDb.Stat.LOSSES, losses + 1));
                    SeasonDb.get(uuid.getUniqueId(), SeasonDb.Stat.POINTS).thenAccept(points -> {
                        int newPoints = Math.max(0, points - 1);
                        SeasonDb.set(uuid.getUniqueId(), SeasonDb.Stat.POINTS, newPoints);
                    });
                    AthleteManager.get(uuid.getUniqueId()).giveCoins(5, true);
                });
            }
            this.points.forEach((uuid, integer) -> BasketballDb.add(uuid, BasketballDb.Stat.POINTS, integer));
            this.threes.forEach((uuid, integer) -> BasketballDb.add(uuid, BasketballDb.Stat.THREES, integer));
        }
        if (Boolean.TRUE.equals(this.getCustomProperty("anteUp"))) {
            AnteUpManager.handleAnteUpPayout(this, winner);
        }
        this.setState(GoalGame.State.FINAL);
        this.sendMessage(Message.gameOver(this.homeScore, this.awayScore));
        this.displayTeamStatsWithCopyButton();
        this.displayNonZeroStats();
        this.generateStatsFile();
        this.startCountdown(GoalGame.State.FINAL, this.settings.waitType.med);
        if (Boolean.TRUE.equals(this.getCustomProperty("anteUp"))) {
            new BukkitRunnable(){

                public void run() {
                    for (Player p : BasketballGame.this.getPlayers()) {
                        AnteUpManager.resetPlayerState(p);
                    }
                    BasketballGame.this.reset();
                }
            }.runTaskLater((Plugin)Partix.getInstance(), 400L);
        }
        this.resetStats();
        this.cancelInboundSequence();
    }

    @Override
    public void setCustomProperty(String key, Object value) {
        this.customProperties.put(key, value);
    }

    @Override
    public Object getCustomProperty(String key) {
        return this.customProperties.get(key);
    }

    @Override
    public Object getCustomPropertyOrDefault(String key, Object defaultValue) {
        return this.customProperties.getOrDefault(key, defaultValue);
    }

    @Override
    public void cancelInboundSequence() {
        this.inboundingActive = false;
        this.inbounder = null;
        if (this.inboundBarrierTask != null && !this.inboundBarrierTask.isCancelled()) {
            this.inboundBarrierTask.cancel();
            this.inboundBarrierTask = null;
        }
        if (this.inboundTimer != null && !this.inboundTimer.isCancelled()) {
            this.inboundTimer.cancel();
            this.inboundTimer = null;
        }
        this.inboundBall = null;
    }

    private void endGameSaveStats() {
        System.out.println("Debug: endGameSaveStats() has been triggered.");
    }

    public void displayStats() {
        TextComponent statsHeader = Component.text((String)"Game Stats", (TextColor)Colour.partix(), (TextDecoration[])new TextDecoration[]{TextDecoration.BOLD});
        this.sendMessage(((TextComponent)Component.text((String)" ").append((Component)statsHeader)).append((Component)Component.text((String)" ")));
        for (UUID playerId : this.statsManager.getAllStats().keySet()) {
            Player player;
            PlayerStats stats = this.statsManager.getPlayerStats(playerId);
            if (stats.getPoints() <= 0 && stats.getThrees() <= 0 && stats.getAssists() <= 0 && stats.getRebounds() <= 0 && stats.getSteals() <= 0 || (player = Bukkit.getPlayer((UUID)playerId)) == null) continue;
            Component statsMessage = ((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.text((String)(player.getName() + ": "), (TextColor)Colour.title()).append(Component.text((String)"Points: ", (TextColor)Colour.border()).append((Component)Component.text((int)stats.getPoints(), (TextColor)Colour.allow())))).append(Component.text((String)", 3s: ", (TextColor)Colour.border()).append((Component)Component.text((int)stats.getThrees(), (TextColor)Colour.allow())))).append(Component.text((String)", Assists: ", (TextColor)Colour.border()).append((Component)Component.text((int)stats.getAssists(), (TextColor)Colour.allow())))).append(Component.text((String)", Rebounds: ", (TextColor)Colour.border()).append((Component)Component.text((int)stats.getRebounds(), (TextColor)Colour.allow())))).append(Component.text((String)", Steals: ", (TextColor)Colour.border()).append((Component)Component.text((int)stats.getSteals(), (TextColor)Colour.allow())));
            this.sendMessage(statsMessage);
        }
    }

    private UUID calculateMVP() {
        double rnd;
        double win;
        double pts;
        double pct;
        HashMap<UUID, Double> mvp = new HashMap<UUID, Double>();
        List<UUID> first = this.homeScore > this.awayScore ? new ArrayList<Player>(this.getHomePlayers()).stream().map(Entity::getUniqueId).toList() : new ArrayList<Player>(this.getAwayPlayers()).stream().map(Entity::getUniqueId).toList();
        List<UUID> second = this.homeScore > this.awayScore ? new ArrayList<Player>(this.getAwayPlayers()).stream().map(Entity::getUniqueId).toList() : new ArrayList<Player>(this.getHomePlayers()).stream().map(Entity::getUniqueId).toList();
        double firstScore = Math.max(this.homeScore, this.awayScore);
        double secondScore = Math.min(this.homeScore, this.awayScore);
        for (UUID uuid : first) {
            pct = (double)Math.max(this.points.getOrDefault(uuid, 0), 1) / firstScore * 50.0;
            pts = this.points.getOrDefault(uuid, 0) * 2 + this.threes.getOrDefault(uuid, 0);
            win = Math.abs(firstScore - secondScore);
            rnd = Math.random() / 10.0;
            mvp.putIfAbsent(uuid, pct + pts + win + rnd);
        }
        for (UUID uuid : second) {
            pct = (double)Math.max(this.points.getOrDefault(uuid, 0), 1) / secondScore * 50.0;
            pts = this.points.getOrDefault(uuid, 0) * 2 + this.threes.getOrDefault(uuid, 0);
            win = 0.0;
            rnd = Math.random() / 10.0;
            mvp.putIfAbsent(uuid, pct + pts + win + rnd);
        }
        return Util.getHighest(mvp);
    }

    @Override
    public void updateDisplay() {
        Object time;
        if (this.getState().equals((Object)GoalGame.State.REGULATION) || this.getState().equals((Object)GoalGame.State.OVERTIME) || this.getState().equals((Object)GoalGame.State.FACEOFF)) {
            time = this.getState().equals((Object)GoalGame.State.OVERTIME) ? (this.settings.suddenDeath ? "\u00a7fTime: \u00a7e0:00" : "\u00a7fTime: \u00a7e" + this.getGameTime()) : (this.settings.winType.timed ? "\u00a7fTime: \u00a7e" + this.getGameTime() : "\u00a7fFirst to: \u00a7e" + this.settings.winType.amount);
            if (this.getState().equals((Object)GoalGame.State.FACEOFF)) {
                time = (String)time + " \u00a77(" + this.getCountSeconds() + "s)";
            }
        } else {
            time = this.getState().equals((Object)GoalGame.State.PREGAME) ? "\u00a7bPregame" + (String)(this.getCountSeconds() > 0 ? ": \u00a7f" + this.getCountSeconds() + "s" : "") : (this.getState().equals((Object)GoalGame.State.FINAL) ? "\u00a7cGame Over" + (String)(this.getCountSeconds() > 0 ? ": \u00a7f" + this.getCountSeconds() + "s" : "") : "\u00a7fStoppage");
        }
        int MAX_TO = 4;
        StringBuilder homeTO = new StringBuilder("\u00a7fTimeouts \u00a77[");
        StringBuilder awayTO = new StringBuilder("\u00a7fTimeouts \u00a77[");
        for (int i = 0; i < 4; ++i) {
            homeTO.append(i < this.homeTimeouts ? "\u00a7a\u00a7l\u2b24" : "\u00a78\u25ef");
            awayTO.append(i < this.awayTimeouts ? "\u00a7a\u00a7l\u2b24" : "\u00a78\u25ef");
        }
        homeTO.append("\u00a77] \u00a70");
        awayTO.append("\u00a77] \u00a71");
        Sidebar.set(this.getPlayers(), (Component)Component.text((String)"\u26f9 MBA Basketball", (TextColor)TextColor.color(0x55FFFF), (TextDecoration[])new TextDecoration[]{TextDecoration.BOLD}), "", "", "\u00a7b\u00a7lMatch Info", "  " + (String)time, "  \u00a7f" + (this.settings.winType.timed ? this.getShortPeriodString() : "---"), "", "\u00a7b\u00a7lScoreboard", "  \u00a7bHome: \u00a7e" + this.homeScore + " \u00a77(" + Text.serialize(this.home.abrv) + ")", homeTO.toString(), "  \u00a7dAway: \u00a7e" + this.awayScore + " \u00a77(" + Text.serialize(this.away.abrv) + ")", awayTO.toString());
    }

    @Override
    public void goal(GoalGame.Team team) {
        Ball ball = this.getBall();
        if (ball instanceof Basketball) {
            Basketball ball2 = (Basketball)ball;
            if (ball2.getVelocity().getY() < 0.01) {
                int score;
                BaseTeam t = team.equals((Object)GoalGame.Team.HOME) ? this.home : this.away;
                boolean isThree = ball2.isThreeEligible();
                Player scorer = ball2.getLastDamager();
                if (scorer != null) {
                    UUID scorerId = scorer.getUniqueId();
                    this.checkAssistEligibility(scorer);
                    PlayerStats stats = this.statsManager.getPlayerStats(scorerId);
                    stats.incrementFGMade();
                    if (isThree) {
                        stats.increment3FGMade();
                        stats.incrementThrees();
                    }
                    if (isThree) {
                        CosmeticSound greenSound;
                        Athlete athlete = AthleteManager.get(scorerId);
                        CosmeticSound cosmeticSound = greenSound = athlete != null ? athlete.getGreenSound() : CosmeticSound.NO_SOUND;
                        if (greenSound != CosmeticSound.NO_SOUND && greenSound.getSoundIdentifier() != null && !greenSound.getSoundIdentifier().isEmpty()) {
                            Bukkit.getLogger().info("[DEBUG] Playing Green Sound for player: " + scorer.getName() + ", Sound: " + greenSound.getSoundIdentifier());
                            scorer.getWorld().playSound(scorer.getLocation(), greenSound.getSoundIdentifier(), SoundCategory.PLAYERS, 3.5f, 1.0f);
                        } else {
                            Bukkit.getLogger().warning("No valid Green Sound for player: " + scorer.getName());
                        }
                    }
                    stats.addPoints(isThree ? 3 : 2);
                    this.sendMessage(((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.text((String)(scorer.getName() + "'s current stats: ")).color(Colour.title())).append((Component)Component.text((String)"Points: ", (TextColor)Colour.border()))).append((Component)Component.text((int)stats.getPoints(), (TextColor)Colour.allow()))).append((Component)Component.text((String)", 3s: ", (TextColor)Colour.border()))).append((Component)Component.text((int)stats.getThrees(), (TextColor)Colour.allow()))).append((Component)Component.text((String)", Assists: ", (TextColor)Colour.border()))).append((Component)Component.text((int)stats.getAssists(), (TextColor)Colour.allow()))).append((Component)Component.text((String)", Rebounds: ", (TextColor)Colour.border()))).append((Component)Component.text((int)stats.getRebounds(), (TextColor)Colour.allow()))).append((Component)Component.text((String)", Steals: ", (TextColor)Colour.border()))).append((Component)Component.text((int)stats.getSteals(), (TextColor)Colour.allow())));
                }
                ball2.setReboundEligible(false);
                if (this.settings.gameType.equals((Object)GameType.AUTOMATIC)) {
                    List<Player> players;
                    List<Player> list = players = team.equals((Object)GoalGame.Team.HOME) ? this.getHomePlayers() : this.getAwayPlayers();
                    if (scorer != null && players.contains(scorer)) {
                        this.points.put(scorer.getUniqueId(), this.points.getOrDefault(scorer.getUniqueId(), 0) + (isThree ? 3 : 2));
                        if (isThree) {
                            this.threes.put(scorer.getUniqueId(), this.threes.getOrDefault(scorer.getUniqueId(), 0) + 1);
                            if (scorer.hasPermission("rank.vip") || scorer.hasPermission("rank.pro")) {
                                PlayerDb.add(scorer.getUniqueId(), PlayerDb.Stat.COINS, 1);
                            }
                        }
                    }
                }
                int n = score = team.equals((Object)GoalGame.Team.HOME) ? this.homeScore : this.awayScore;
                if (this.getState().equals((Object)GoalGame.State.REGULATION) || !this.settings.suddenDeath || !this.settings.winType.timed && score + (isThree ? 3 : 2) >= this.settings.winType.amount) {
                    this.sendTitle(t.name.append(Component.text((String)(isThree ? " \u2023 3 Points!" : " \u2023 2 Points")).color(Colour.partix())));
                    if (scorer != null) {
                        AthleteManager.get(scorer.getUniqueId()).getExplosion().mediumExplosion(ball2.getLocation());
                    }
                    Vector v;
                    if (team.equals((Object)GoalGame.Team.HOME)) {
                        this.homeScore += isThree ? 3 : 2;
                        v = new Vector(1.25, -1.5, 0.0);
                        this.getHomePlayers().stream().filter(player -> player.getLocation().getX() < this.getCenter().getX()).forEach(player -> {
                            player.teleport(player.getLocation().clone().set(this.getCenter().clone().getX(), player.getLocation().getY(), player.getLocation().getZ()));
                            player.setVelocity(v);
                        });
                    } else {
                        this.awayScore += isThree ? 3 : 2;
                        v = new Vector(-1.25, -1.5, 0.0);
                        this.getAwayPlayers().stream().filter(player -> player.getLocation().getX() > this.getCenter().getX()).forEach(player -> {
                            player.teleport(player.getLocation().clone().set(this.getCenter().clone().getX(), player.getLocation().getY(), player.getLocation().getZ()));
                            player.setVelocity(v);
                        });
                    }
                    // Remove the ball and schedule sideline inbound for the non-scoring team
                    ball2.clearPerfectShot();
                    this.removeBalls();
                    this.setState(GoalGame.State.STOPPAGE);
                    this.shotClockStopped = true;
                    final GoalGame.Team inboundTeam = team.equals((Object)GoalGame.Team.HOME) ? GoalGame.Team.AWAY : GoalGame.Team.HOME;
                    new BukkitRunnable(){
                        public void run() {
                            BasketballGame.this.endTimeout(inboundTeam);
                        }
                    }.runTaskLater((Plugin)Partix.getInstance(), 60L);
                } else {
                    if (team.equals((Object)GoalGame.Team.HOME)) {
                        this.homeScore += isThree ? 3 : 2;
                    } else {
                        this.awayScore += isThree ? 3 : 2;
                    }
                    this.gameOver(team);
                    this.sendTitle(((TextComponent)((TextComponent)Component.text((String)"The ").color(Colour.partix())).append(t.name)).append(Component.text((String)" Win!").color(Colour.partix())));
                    this.removeBalls();
                }
            }
            ball2.clearPerfectShot();
            this.lastPossessionTeam = null;
            this.resetShotClock();
        }
    }

    @Override
    public void joinTeam(Player player, GoalGame.Team team) {
        Athlete athlete = AthleteManager.get(player.getUniqueId());
        if (team.equals((Object)GoalGame.Team.HOME)) {
            athlete.setSpectator(false);
            this.addHomePlayer(player);
            player.getActivePotionEffects().clear();
            if (this.settings.gameEffect.effect != null) {
                player.addPotionEffect(this.settings.gameEffect.effect);
            }
            player.getInventory().setChestplate(this.home.chest);
            player.getInventory().setLeggings(this.home.pants);
            player.getInventory().setBoots(this.home.boots);
            player.getInventory().setItem(6, Items.get(Component.text((String)"Your Bench").color(Colour.partix()), Material.OAK_STAIRS));
            player.getInventory().setItem(7, Items.get(Component.text((String)"Game Settings").color(Colour.partix()), Material.CHEST));
            player.getInventory().setItem(8, Items.get(Component.text((String)"Change Team/Leave Game").color(Colour.partix()), Material.GRAY_DYE));
            if (this.getHomePlayers().size() < this.settings.playersPerTeam) {
                player.teleport(this.getHomeSpawn());
            } else {
                this.enterBench(player);
            }
            player.sendMessage(Message.joinTeam("home"));
            this.joinedHome.add(player.getUniqueId());
            this.removeAwayPlayer(player);
        } else if (team.equals((Object)GoalGame.Team.AWAY)) {
            athlete.setSpectator(false);
            this.addAwayPlayer(player);
            player.getActivePotionEffects().clear();
            if (this.settings.gameEffect.effect != null) {
                player.addPotionEffect(this.settings.gameEffect.effect);
            }
            player.getInventory().setChestplate(Items.armor(Material.LEATHER_CHESTPLATE, 0xFFFFFF, "Jersey", "Your teams away jersey"));
            player.getInventory().setLeggings(this.away.away);
            player.getInventory().setBoots(this.away.boots);
            player.getInventory().setItem(6, Items.get(Component.text((String)"Your Bench").color(Colour.partix()), Material.OAK_STAIRS));
            player.getInventory().setItem(7, Items.get(Component.text((String)"Game Settings").color(Colour.partix()), Material.CHEST));
            player.getInventory().setItem(8, Items.get(Component.text((String)"Change Team").color(Colour.partix()), Material.GRAY_DYE));
            if (this.getAwayPlayers().size() < this.settings.playersPerTeam) {
                player.teleport(this.getAwaySpawn());
            } else {
                this.enterBench(player);
            }
            player.sendMessage(Message.joinTeam("away"));
            this.joinedAway.add(player.getUniqueId());
            this.removeHomePlayer(player);
        } else {
            player.getActivePotionEffects().clear();
            athlete.setSpectator(true);
            player.getInventory().clear();
            player.getInventory().setItem(7, Items.get(Component.text((String)"Game Settings").color(Colour.partix()), Material.CHEST));
            player.getInventory().setItem(8, Items.get(Component.text((String)"Change Team").color(Colour.partix()), Material.GRAY_DYE));
            player.teleport(this.getCenter().add(0.0, 10.0, 0.0));
            player.sendMessage(Message.joinTeam("spectators"));
        }
        this.updateDisplay();
    }

    @Override
    public BallType getBallType() {
        return BallType.BASKETBALL;
    }

    @Override
    public boolean canEditGame(Player player) {
        if (player.hasPermission("rank.admin")) {
            return true;
        }
        if (this.owner != null) {
            return this.owner == player.getUniqueId();
        }
        return false;
    }

    @Override
    public void onJoin(Athlete ... athletes) {
        if (this.settings.compType.equals((Object)CompType.CASUAL)) {
            for (Athlete athlete : athletes) {
                this.joinTeam(athlete.getPlayer(), GoalGame.Team.SPECTATOR);
            }
        }
    }

    @Override
    public void onQuit(Athlete ... athletes) {
        for (Athlete athlete : athletes) {
            Player player = athlete.getPlayer();
            if (player == null) continue;
            
            // Clean up super jump data
            SuperJumpManager.cleanupPlayer(player.getUniqueId());
            
            boolean isProAm = Boolean.TRUE.equals(this.getCustomPropertyOrDefault("proam", false));
            if (this.settings.compType == CompType.RANKED && !this.getState().equals((Object)GoalGame.State.FINAL) && !isProAm) {
                SeasonDb.remove(player.getUniqueId(), SeasonDb.Stat.POINTS, 3);
                player.sendMessage("\u00a7cYou disconnected from a ranked game. You lost 3 Season Points!");
            }
            this.removeAthlete(athlete);
        }
        if (this.getPlayers().isEmpty()) {
            if (!this.getState().equals((Object)GoalGame.State.FINAL)) {
                Bukkit.getLogger().info("No players remain. Ending with no changes..");
            }
            this.reset();
            return;
        }
        if (!(this.settings.playersPerTeam != 2 && this.settings.playersPerTeam != 3 || this.settings.compType != CompType.RANKED || this.getState().equals((Object)GoalGame.State.FINAL))) {
            int homeCount = this.getHomePlayers().size();
            int awayCount = this.getAwayPlayers().size();
            if (homeCount == 0 && awayCount > 0) {
                Bukkit.broadcast((Component)Component.text((String)"\u00a7aThe away team is automatically declared the winner (other side left!)"));
                this.gameOver(GoalGame.Team.AWAY);
            } else if (awayCount == 0 && homeCount > 0) {
                Bukkit.broadcast((Component)Component.text((String)"\u00a7aThe home team is automatically declared the winner (other side left!)"));
                this.gameOver(GoalGame.Team.HOME);
            }
        }
    }

    public void removeAthlete(Athlete athlete) {
        Player player = athlete.getPlayer();
        this.getHomePlayers().remove(player);
        this.getAwayPlayers().remove(player);
    }

    @Override
    public void stoppageDetection() {
    }

    public void start() {
        this.reset();
        this.startCountdown(GoalGame.State.FACEOFF, 10);
    }

    public void generateStatsFile() {
        System.out.println("Debug: generateStatsFile() triggered.");
        String fileName = "BasketballGameStats.csv";
        String filePath = Paths.get("plugins", "GameStats", fileName).toString();
        try {
            File statsDirectory = new File("plugins/GameStats");
            if (!statsDirectory.exists()) {
                statsDirectory.mkdirs();
                System.out.println("Debug: Created stats directory.");
            }
            try (FileWriter writer = new FileWriter(filePath);){
                writer.append("Player Name,Points,3-Pointers,Assists,Rebounds,Steals\n");
                for (UUID playerId : this.statsManager.getAllStats().keySet()) {
                    Player player = Bukkit.getPlayer((UUID)playerId);
                    if (player == null) continue;
                    PlayerStats stats = this.statsManager.getPlayerStats(playerId);
                    writer.append(player.getName()).append(",").append(String.valueOf(stats.getPoints())).append(",").append(String.valueOf(stats.getThrees())).append(",").append(String.valueOf(stats.getAssists())).append(",").append(String.valueOf(stats.getRebounds())).append(",").append(String.valueOf(stats.getSteals())).append("\n");
                }
                System.out.println("Debug: Stats saved to " + filePath);
            }
            Bukkit.getLogger().info("Game stats saved to: " + filePath);
        }
        catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save game stats: " + e.getMessage());
        }
    }

    public boolean pass(Player player) {
        if (this.getBall() != null && this.getBall().getCurrentDamager() == player) {
            this.startAssistTimer(player.getUniqueId());
            this.sendMessage(Component.text((String)(player.getName() + " passed the ball!")).color(Colour.allow()));
            return true;
        }
        return false;
    }

    public void onOpponentTouch() {
        this.cancelAssistTimer();
    }

    public void startAssistTimer(UUID passerId) {
        this.assistEligiblePasser = passerId;
        this.assistTimerEndTime = System.currentTimeMillis() + 12000L;
        this.assistTimerActive = true;
    }

    private void checkAssistEligibility(Player scorer) {
        if (this.assistTimerActive && this.assistEligiblePasser != null && System.currentTimeMillis() <= this.assistTimerEndTime) {
            Player passer = Bukkit.getPlayer((UUID)this.assistEligiblePasser);
            if (passer != null && !this.assistEligiblePasser.equals(scorer.getUniqueId()) && this.getHomePlayers().contains(scorer) == this.getHomePlayers().contains(passer)) {
                PlayerStats passerStats = this.statsManager.getPlayerStats(this.assistEligiblePasser);
                passerStats.incrementAssistsWithMessage(passer);
                System.out.println("\u2705 Assist recorded for " + passer.getName() + " (Scorer: " + scorer.getName() + ")");
            } else {
                System.out.println("\u274c Assist denied: " + (passer != null ? passer.getName() : "Unknown") + " passed to themselves.");
            }
        }
        this.assistTimerActive = false;
        this.assistEligiblePasser = null;
    }

    public void cancelAssistTimer() {
        this.assistTimerActive = false;
        this.assistEligiblePasser = null;
    }

    public void handleBallInterception(Player opponent) {
        this.cancelAssistTimer();
        this.sendMessage(Component.text((String)(opponent.getName() + " intercepted the ball!")).color(Colour.deny()));
    }

    private void displayNonZeroStats() {
        this.sendMessage((Component)Component.text((String)"Game Stats", (TextColor)Colour.partix(), (TextDecoration[])new TextDecoration[]{TextDecoration.BOLD}));
        ArrayList<UUID> allPlayerIds = new ArrayList<UUID>();
        allPlayerIds.addAll(this.getHomePlayers().stream().map(Entity::getUniqueId).toList());
        allPlayerIds.addAll(this.getAwayPlayers().stream().map(Entity::getUniqueId).toList());
        boolean hasStats = false;
        for (UUID playerId : allPlayerIds) {
            PlayerStats stats = this.statsManager.getPlayerStats(playerId);
            if (stats == null || stats.getPoints() <= 0 && stats.getThrees() <= 0 && stats.getAssists() <= 0 && stats.getRebounds() <= 0 && stats.getSteals() <= 0) continue;
            Player player = Bukkit.getPlayer((UUID)playerId);
            String playerName = player != null ? player.getName() : "Unknown";
            Component statsMessage = ((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.text((String)(playerName + ": "), (TextColor)Colour.title()).append(Component.text((String)"Points: ", (TextColor)Colour.border()).append((Component)Component.text((int)stats.getPoints(), (TextColor)Colour.allow())))).append(Component.text((String)", 3s: ", (TextColor)Colour.border()).append((Component)Component.text((int)stats.getThrees(), (TextColor)Colour.allow())))).append(Component.text((String)", Assists: ", (TextColor)Colour.border()).append((Component)Component.text((int)stats.getAssists(), (TextColor)Colour.allow())))).append(Component.text((String)", Rebounds: ", (TextColor)Colour.border()).append((Component)Component.text((int)stats.getRebounds(), (TextColor)Colour.allow())))).append(Component.text((String)", Steals: ", (TextColor)Colour.border()).append((Component)Component.text((int)stats.getSteals(), (TextColor)Colour.allow())));
            this.sendMessage(statsMessage);
            hasStats = true;
        }
        if (!hasStats) {
            this.sendMessage(Component.text((String)"No player recorded stats in this game.").color(Colour.deny()));
        }
    }

    public boolean isShotActive(UUID shooterId) {
        return this.shotInProgress.containsKey(shooterId);
    }

    public void displayTeamStatsWithCopyButton() {
        StringBuilder rawStatsBuilder = new StringBuilder();
        rawStatsBuilder.append("=== MBA Game Stats ===\n\n");
        int homePoints = 0;
        int homeAssists = 0;
        int homeRebounds = 0;
        int homeSteals = 0;
        int homeTurnovers = 0;
        int homeFGMade = 0;
        int homeFGAtt = 0;
        int home3FGMade = 0;
        int home3FGAtt = 0;
        long homePossTime = 0L;
        int homePassAttempts = 0;
        rawStatsBuilder.append("=== ").append(Text.serialize(this.home.name)).append(" ===\n");
        this.sendMessage(((TextComponent)((TextComponent)Component.text((String)"=== ").append(this.home.name)).append((Component)Component.text((String)" ==="))).color(Colour.partix()));
        for (Player p : this.getHomePlayers()) {
            PlayerStats s;
            if (p == null || (s = this.statsManager.getPlayerStats(p.getUniqueId())) == null) continue;
            int points = s.getPoints();
            int assists = s.getAssists();
            int rebounds = s.getRebounds();
            int offensiveRebounds = s.getOffensiveRebounds();
            int defensiveRebounds = s.getDefensiveRebounds();
            int steals = s.getSteals();
            int blocks = s.getBlocks();
            int turnovers = s.getTurnovers();
            int missesAgainst = s.getMissesAgainst();
            long possTimeMillis = s.getPossessionTime();
            int possTimeSec = (int)(possTimeMillis / 1000L);
            long activePlayTimeMillis = s.getActivePlayTime();
            int activePlayTimeSec = (int)(activePlayTimeMillis / 1000L);
            int activePlayMinutes = activePlayTimeSec / 60;
            int activePlaySeconds = activePlayTimeSec % 60;
            String minutesFormatted = String.format("%d:%02d", activePlayMinutes, activePlaySeconds);
            int passAttempts = s.getPassAttempts();
            int totalFGMadeLocal = s.getFGMade();
            int totalFGAttLocal = s.getFGAttempted();
            int threeFGMadeLocal = s.get3FGMade();
            int threeFGAttLocal = s.get3FGAttempted();
            double totalFGPct = totalFGAttLocal == 0 ? 0.0 : (double)totalFGMadeLocal / (double)totalFGAttLocal * 100.0;
            double threeFGPct = threeFGAttLocal == 0 ? 0.0 : (double)threeFGMadeLocal / (double)threeFGAttLocal * 100.0;
            String rawLine = String.format("%s | MIN %s | PTS %d | FG %d/%d (%.0f%%) | 3FG %d/%d (%.0f%%) | AST/PASS %d/%d | OREB %d | DREB %d | REB %d | STL %d | BLK %d | TOV %d | MISS-AG %d | POSS %ds", p.getName(), minutesFormatted, points, totalFGMadeLocal, totalFGAttLocal, totalFGPct, threeFGMadeLocal, threeFGAttLocal, threeFGPct, assists, passAttempts, offensiveRebounds, defensiveRebounds, rebounds, steals, blocks, turnovers, missesAgainst, possTimeSec);
            rawStatsBuilder.append(rawLine).append("\n\n");
            this.sendMessage(Component.text(rawLine).color(TextColor.color((int)43775)));
            homePoints += points;
            homeAssists += assists;
            homeRebounds += rebounds;
            homeSteals += steals;
            homeTurnovers += turnovers;
            homeFGMade += totalFGMadeLocal;
            homeFGAtt += totalFGAttLocal;
            home3FGMade += threeFGMadeLocal;
            home3FGAtt += threeFGAttLocal;
            homePossTime += possTimeMillis;
            homePassAttempts += passAttempts;
        }
        int homePossSec = (int)(homePossTime / 1000L);
        double homeTotalFGPct = homeFGAtt == 0 ? 0.0 : (double)homeFGMade / (double)homeFGAtt * 100.0;
        double homeThreeFGPct = home3FGAtt == 0 ? 0.0 : (double)home3FGMade / (double)home3FGAtt * 100.0;
        String homeTotalLine = String.format("TEAM TOTALS | PTS %d | FG %d/%d (%.0f%%) | 3FG %d/%d (%.0f%%) | AST/PASS %d/%d | REB %d | STL %d | TOV %d | POSS %ds", homePoints, homeFGMade, homeFGAtt, homeTotalFGPct, home3FGMade, home3FGAtt, homeThreeFGPct, homeAssists, homePassAttempts, homeRebounds, homeSteals, homeTurnovers, homePossSec);
        rawStatsBuilder.append("\n").append(homeTotalLine).append("\n\n");
        this.sendMessage(Component.text((String)homeTotalLine).color(TextColor.color((int)43775)));
        int awayPoints = 0;
        int awayAssists = 0;
        int awayRebounds = 0;
        int awaySteals = 0;
        int awayTurnovers = 0;
        int awayFGMade = 0;
        int awayFGAtt = 0;
        int away3FGMade = 0;
        int away3FGAtt = 0;
        long awayPossTime = 0L;
        int awayPassAttempts = 0;
        rawStatsBuilder.append("=== ").append(Text.serialize(this.away.name)).append(" ===\n");
        this.sendMessage(((TextComponent)((TextComponent)Component.text((String)"=== ").append(this.away.name)).append((Component)Component.text((String)" ==="))).color(Colour.partix()));
        for (Player p : this.getAwayPlayers()) {
            PlayerStats s;
            if (p == null || (s = this.statsManager.getPlayerStats(p.getUniqueId())) == null) continue;
            int points = s.getPoints();
            int assists = s.getAssists();
            int rebounds = s.getRebounds();
            int offensiveRebounds = s.getOffensiveRebounds();
            int defensiveRebounds = s.getDefensiveRebounds();
            int steals = s.getSteals();
            int blocks = s.getBlocks();
            int turnovers = s.getTurnovers();
            int missesAgainst = s.getMissesAgainst();
            long possTimeMillis = s.getPossessionTime();
            int possTimeSec = (int)(possTimeMillis / 1000L);
            long activePlayTimeMillis = s.getActivePlayTime();
            int activePlayTimeSec = (int)(activePlayTimeMillis / 1000L);
            int activePlayMinutes = activePlayTimeSec / 60;
            int activePlaySeconds = activePlayTimeSec % 60;
            String minutesFormatted = String.format("%d:%02d", activePlayMinutes, activePlaySeconds);
            int passAttempts = s.getPassAttempts();
            int totalFGMadeLocal = s.getFGMade();
            int totalFGAttLocal = s.getFGAttempted();
            int threeFGMadeLocal = s.get3FGMade();
            int threeFGAttLocal = s.get3FGAttempted();
            double totalFGPct = totalFGAttLocal == 0 ? 0.0 : (double)totalFGMadeLocal / (double)totalFGAttLocal * 100.0;
            double threeFGPct = threeFGAttLocal == 0 ? 0.0 : (double)threeFGMadeLocal / (double)threeFGAttLocal * 100.0;
            String rawLine = String.format("%s | MIN %s | PTS %d | FG %d/%d (%.0f%%) | 3FG %d/%d (%.0f%%) | AST/PASS %d/%d | OREB %d | DREB %d | REB %d | STL %d | BLK %d | TOV %d | MISS-AG %d | POSS %ds", p.getName(), minutesFormatted, points, totalFGMadeLocal, totalFGAttLocal, totalFGPct, threeFGMadeLocal, threeFGAttLocal, threeFGPct, assists, passAttempts, offensiveRebounds, defensiveRebounds, rebounds, steals, blocks, turnovers, missesAgainst, possTimeSec);
            rawStatsBuilder.append(rawLine).append("\n\n");
            this.sendMessage(Component.text(rawLine).color(TextColor.color((int)0xFFAA00)));
            awayPoints += points;
            awayAssists += assists;
            awayRebounds += rebounds;
            awaySteals += steals;
            awayTurnovers += turnovers;
            awayFGMade += totalFGMadeLocal;
            awayFGAtt += totalFGAttLocal;
            away3FGMade += threeFGMadeLocal;
            away3FGAtt += threeFGAttLocal;
            awayPossTime += possTimeMillis;
            awayPassAttempts += passAttempts;
        }
        int awayPossSec = (int)(awayPossTime / 1000L);
        double awayTotalFGPct = awayFGAtt == 0 ? 0.0 : (double)awayFGMade / (double)awayFGAtt * 100.0;
        double awayThreeFGPct = away3FGAtt == 0 ? 0.0 : (double)away3FGMade / (double)away3FGAtt * 100.0;
        String awayTotalLine = String.format("TEAM TOTALS | PTS %d | FG %d/%d (%.0f%%) | 3FG %d/%d (%.0f%%) | AST/PASS %d/%d | REB %d | STL %d | TOV %d | POSS %ds", awayPoints, awayFGMade, awayFGAtt, awayTotalFGPct, away3FGMade, away3FGAtt, awayThreeFGPct, awayAssists, awayPassAttempts, awayRebounds, awaySteals, awayTurnovers, awayPossSec);
        rawStatsBuilder.append("\n").append(awayTotalLine).append("\n\n");
        this.sendMessage(Component.text((String)awayTotalLine).color(TextColor.color((int)0xFFAA00)));
        Component copyButton = ((TextComponent)((TextComponent)Component.text((String)"[Click to Copy Stats]").color(Colour.allow())).clickEvent(ClickEvent.copyToClipboard((String)rawStatsBuilder.toString()))).hoverEvent((HoverEventSource)Component.text((String)"Click to copy all final stats"));
        for (Player player : this.getPlayers()) {
            player.sendMessage(copyButton);
        }
    }

    public int getHomeScore() {
        return this.homeScore;
    }

    public int getAwayScore() {
        return this.awayScore;
    }

    private int parseColor(String colorName) {
        String c;
        return switch (c = colorName.toUpperCase()) {
            case "RED" -> 0xFF0000;
            case "BLUE" -> 255;
            case "GREEN" -> 65280;
            case "YELLOW" -> 0xFFFF00;
            case "BLACK" -> 0;
            default -> 0xFFFFFF;
        };
    }

    public void setTeamName(GoalGame.Team team, String name) {
        if (team == GoalGame.Team.HOME) {
            this.home.name = Component.text((String)name);
        } else if (team == GoalGame.Team.AWAY) {
            this.away.name = Component.text((String)name);
        }
    }

    public void setTeamJerseys(GoalGame.Team team, String chestplateColor, String leggingsColor, String bootsColor) {
        block3: {
            block2: {
                if (team != GoalGame.Team.HOME) break block2;
                this.home.chest = Items.armor(Material.LEATHER_CHESTPLATE, this.parseColor(chestplateColor), "Jersey", "Your team jersey");
                this.home.pants = Items.armor(Material.LEATHER_LEGGINGS, this.parseColor(leggingsColor), "Pants", "Your team pants");
                this.home.boots = Items.armor(Material.LEATHER_BOOTS, this.parseColor(bootsColor), "Boots", "Your team boots");
                for (Player p : this.getHomePlayers()) {
                    p.getInventory().setChestplate(this.home.chest);
                    p.getInventory().setLeggings(this.home.pants);
                    p.getInventory().setBoots(this.home.boots);
                }
                break block3;
            }
            if (team != GoalGame.Team.AWAY) break block3;
            this.away.chest = Items.armor(Material.LEATHER_CHESTPLATE, 0xFFFFFF, "Jersey", "Your team away jersey");
            this.away.pants = Items.armor(Material.LEATHER_LEGGINGS, this.parseColor(leggingsColor), "Pants", "Your team pants");
            this.away.boots = Items.armor(Material.LEATHER_BOOTS, this.parseColor(bootsColor), "Boots", "Your team boots");
            for (Player p : this.getAwayPlayers()) {
                p.getInventory().setChestplate(this.away.chest);
                p.getInventory().setLeggings(this.away.pants);
                p.getInventory().setBoots(this.away.boots);
            }
        }
    }

    public void updateHomeTeamDisplay(String teamName, String chestColor, String leggingsColor, String bootsColor) {
    }

    public void updateAwayTeamDisplay(String teamName, String chestColor, String leggingsColor, String bootsColor) {
    }

    public boolean isInMBAStadium() {
        return false;
    }
    
    public void resetReboundMachineStats() {
        this.reboundMachineShotsTaken.clear();
        this.reboundMachineShotsMade.clear();
        this.reboundMachineShotsMissed.clear();
        this.sendMessage(Component.text("Rebound Machine stats reset for all players.").color(Colour.partix()));
    }
    
    public boolean isCoach(UUID playerId) {
        return false;
    }
    
    public GoalGame.Team getCoachTeam(UUID playerId) {
        return null;
    }
    
    public int getHomeTimeouts() {
        return this.homeTimeouts;
    }
    
    public int getAwayTimeouts() {
        return this.awayTimeouts;
    }
    
    public UUID getCoach(GoalGame.Team team) {
        return team == GoalGame.Team.HOME ? this.homeCoach : this.awayCoach;
    }
    
    public void setCoach(GoalGame.Team team, UUID coachId) {
        if (team == GoalGame.Team.HOME) {
            this.homeCoach = coachId;
        } else {
            this.awayCoach = coachId;
        }
    }
    
    public void removeCoach(UUID coachId) {
        if (this.homeCoach != null && this.homeCoach.equals(coachId)) {
            this.homeCoach = null;
        }
        if (this.awayCoach != null && this.awayCoach.equals(coachId)) {
            this.awayCoach = null;
        }
    }
    
    public void start1v1Game() {
        this.start();
    }

    public void openTeamManagementGUI(Player p) {
        GUI gui = new GUI("Team Management", 3, false, new ItemButton[0]);
        gui.addButton(new ItemButton(10, Items.get(Component.text((String)"Home \u2013 Timeout").color(Colour.deny()), Material.RED_DYE), b -> {
            this.homeTimeouts = Math.max(0, this.homeTimeouts - 1);
            b.sendMessage(Component.text((String)("Home timeouts: " + this.homeTimeouts)).color(Colour.deny()));
            this.openTeamManagementGUI((Player)b);
        }));
        gui.addButton(new ItemButton(11, Items.get(Component.text((String)"Home \uff0b Timeout").color(Colour.allow()), Material.LIME_DYE), b -> {
            this.homeTimeouts = Math.min(4, this.homeTimeouts + 1);
            b.sendMessage(Component.text((String)("Home timeouts: " + this.homeTimeouts)).color(Colour.allow()));
            this.openTeamManagementGUI((Player)b);
        }));
        gui.addButton(new ItemButton(12, Items.get(Component.text((String)"Away \u2013 Timeout").color(Colour.deny()), Material.RED_DYE), b -> {
            this.awayTimeouts = Math.max(0, this.awayTimeouts - 1);
            b.sendMessage(Component.text((String)("Away timeouts: " + this.awayTimeouts)).color(Colour.deny()));
            this.openTeamManagementGUI((Player)b);
        }));
        gui.addButton(new ItemButton(13, Items.get(Component.text((String)"Away \uff0b Timeout").color(Colour.allow()), Material.LIME_DYE), b -> {
            this.awayTimeouts = Math.min(4, this.awayTimeouts + 1);
            b.sendMessage(Component.text((String)("Away timeouts: " + this.awayTimeouts)).color(Colour.allow()));
            this.openTeamManagementGUI((Player)b);
        }));
        gui.addButton(new ItemButton(21, Items.get(Component.text((String)"Skip to 10s").color(Colour.partix()), Material.CLOCK), b -> {
            if (this.timeoutTask != null && this.getState().equals((Object)GoalGame.State.STOPPAGE)) {
                this.timeoutSecs = 10;
                this.timeoutBar.setTitle("Timeout: 10s");
                this.timeoutBar.setProgress(0.16666666666666666);
                this.skipTimeoutToTen();
                b.sendMessage(Component.text((String)"\u26a1 Skipped timeout to 10 seconds").color(Colour.allow()));
            } else {
                b.sendMessage(Component.text((String)"No timeout in progress to skip").color(Colour.deny()));
            }
            this.openTeamManagementGUI((Player)b);
        }));
        gui.addButton(new ItemButton(14, Items.get(Component.text((String)"Inbound \u25b6 Home").color(Colour.partix()), Material.ARROW), b -> {
            this.endTimeout(GoalGame.Team.HOME);
            this.openTeamManagementGUI((Player)b);
        }));
        gui.addButton(new ItemButton(15, Items.get(Component.text((String)"Inbound \u25b6 Away").color(Colour.partix()), Material.ARROW), b -> {
            this.endTimeout(GoalGame.Team.AWAY);
            this.openTeamManagementGUI((Player)b);
        }));
        gui.addButton(new ItemButton(16, Items.get(Component.text((String)"Cancel Inbound").color(Colour.deny()), Material.BARRIER), b -> {
            this.inboundingActive = false;
            b.sendMessage(Component.text((String)"Inbound sequence cancelled").color(Colour.deny()));
            this.openTeamManagementGUI((Player)b);
        }));
        gui.addButton(new ItemButton(22, Items.get(Component.text((String)"Reset Timeouts").color(Colour.partix()), Material.PAPER), b -> {
            this.awayTimeouts = 4;
            this.homeTimeouts = 4;
            b.sendMessage(Component.text((String)"All timeouts reset to 4").color(Colour.allow()));
            this.openTeamManagementGUI((Player)b);
        }));
        gui.openInventory(p);
    }
}
