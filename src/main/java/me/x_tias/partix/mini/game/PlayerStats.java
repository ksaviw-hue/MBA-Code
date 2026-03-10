/*
 * Decompiled with CFR 0.152.
 */
package me.x_tias.partix.mini.game;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStats {
    private final Map<UUID, Integer> contestTime = new HashMap<>();
    @Setter
    @Getter
    private int blocks;
    @Setter
    @Getter
    private int points;
    @Setter
    @Getter
    private int threes;
    @Setter
    @Getter
    private int assists;
    @Setter
    @Getter
    private int rebounds;
    @Getter
    private int offensiveRebounds;
    @Getter
    private int defensiveRebounds;
    @Setter
    @Getter
    private int steals;
    @Getter
    private int turnovers;
    @Getter
    private int missesAgainst;
    @Getter
    private long possessionTime;
    private int fgMade;
    private int fgAttempted;
    private int fg3Made;
    private int fg3Attempted;
    private int fg2Made;
    private int fg2Attempted;
    @Getter
    private int passAttempts;
    @Getter
    private long minutesPlayed; // In milliseconds
    @Getter
    private long activePlayTime; // Time in REGULATION/OVERTIME on court (milliseconds)
    @Getter
    @Setter
    private long lastActivePlayUpdate; // Track when activePlayTime was last updated
    
    // Stamina system
    @Getter
    @Setter
    private double currentStamina;
    @Getter
    @Setter
    private double maxStamina;
    @Getter
    @Setter
    private long lastStaminaUpdate; // Timestamp for gradual stamina loss/gain

    public PlayerStats() {
        this.reset();
    }

    public void incrementBlocks() {
        ++this.blocks;
    }
    
    public void incrementBlocksWithMessage(org.bukkit.entity.Player player) {
        ++this.blocks;
        player.sendMessage(net.kyori.adventure.text.Component.text("Block!").color(net.kyori.adventure.text.format.NamedTextColor.GREEN)
            .append(net.kyori.adventure.text.Component.text(" (Blocks: " + this.blocks + ")").color(net.kyori.adventure.text.format.NamedTextColor.GRAY)));
    }

    public int getFGMade() {
        return this.fgMade;
    }

    public void setFGMade(int fgMade) {
        this.fgMade = fgMade;
    }

    public int getFGAttempted() {
        return this.fgAttempted;
    }

    public void setFGAttempted(int fgAttempted) {
        this.fgAttempted = fgAttempted;
    }

    public int get3FGMade() {
        return this.fg3Made;
    }

    public void set3FGMade(int fg3Made) {
        this.fg3Made = fg3Made;
    }

    public int get3FGAttempted() {
        return this.fg3Attempted;
    }

    public void set3FGAttempted(int fg3Attempted) {
        this.fg3Attempted = fg3Attempted;
    }

    public int get2FGMade() {
        return this.fg2Made;
    }

    public int get2FGAttempted() {
        return this.fg2Attempted;
    }

    public double getFGPercentage() {
        return this.fgAttempted == 0 ? 0.0 : (double) this.fgMade / (double) this.fgAttempted * 100.0;
    }

    public double get3FGPercentage() {
        return this.fg3Attempted == 0 ? 0.0 : (double) this.fg3Made / (double) this.fg3Attempted * 100.0;
    }

    public void addPoints(int pts) {
        this.points += pts;
    }

    public void incrementThrees() {
        ++this.threes;
    }

    public void incrementAssists() {
        ++this.assists;
    }
    
    public void incrementAssistsWithMessage(org.bukkit.entity.Player player) {
        ++this.assists;
        player.sendMessage(net.kyori.adventure.text.Component.text("Assist!").color(net.kyori.adventure.text.format.NamedTextColor.GREEN)
            .append(net.kyori.adventure.text.Component.text(" (Assists: " + this.assists + ")").color(net.kyori.adventure.text.format.NamedTextColor.GRAY)));
    }

    public void incrementRebounds() {
        ++this.rebounds;
    }
    
    public void incrementOffensiveRebounds() {
        ++this.offensiveRebounds;
        ++this.rebounds;
    }
    
    public void incrementDefensiveRebounds() {
        ++this.defensiveRebounds;
        ++this.rebounds;
    }
    
    public void incrementReboundsWithMessage(org.bukkit.entity.Player player) {
        ++this.rebounds;
        player.sendMessage(net.kyori.adventure.text.Component.text("Rebound!").color(net.kyori.adventure.text.format.NamedTextColor.GREEN)
            .append(net.kyori.adventure.text.Component.text(" (Rebounds: " + this.rebounds + ")").color(net.kyori.adventure.text.format.NamedTextColor.GRAY)));
    }
    
    public void incrementOffensiveReboundsWithMessage(org.bukkit.entity.Player player) {
        ++this.offensiveRebounds;
        ++this.rebounds;
        player.sendMessage(net.kyori.adventure.text.Component.text("Offensive Rebound!").color(net.kyori.adventure.text.format.NamedTextColor.GOLD)
            .append(net.kyori.adventure.text.Component.text(" (OREB: " + this.offensiveRebounds + ")").color(net.kyori.adventure.text.format.NamedTextColor.GRAY)));
    }
    
    public void incrementDefensiveReboundsWithMessage(org.bukkit.entity.Player player) {
        ++this.defensiveRebounds;
        ++this.rebounds;
        player.sendMessage(net.kyori.adventure.text.Component.text("Defensive Rebound!").color(net.kyori.adventure.text.format.NamedTextColor.AQUA)
            .append(net.kyori.adventure.text.Component.text(" (DREB: " + this.defensiveRebounds + ")").color(net.kyori.adventure.text.format.NamedTextColor.GRAY)));
    }
    
    public void incrementMissesAgainst() {
        ++this.missesAgainst;
    }

    public void incrementSteals() {
        ++this.steals;
    }
    
    public void incrementStealsWithMessage(org.bukkit.entity.Player player) {
        ++this.steals;
        player.sendMessage(net.kyori.adventure.text.Component.text("Steal!").color(net.kyori.adventure.text.format.NamedTextColor.GREEN)
            .append(net.kyori.adventure.text.Component.text(" (Steals: " + this.steals + ")").color(net.kyori.adventure.text.format.NamedTextColor.GRAY)));
    }

    public void incrementTurnovers() {
        ++this.turnovers;
    }
    
    public void incrementTurnoversWithMessage(org.bukkit.entity.Player player) {
        ++this.turnovers;
        player.sendMessage(net.kyori.adventure.text.Component.text("Turnover!").color(net.kyori.adventure.text.format.NamedTextColor.RED)
            .append(net.kyori.adventure.text.Component.text(" (Turnovers: " + this.turnovers + ")").color(net.kyori.adventure.text.format.NamedTextColor.GRAY)));
    }

    public void addPossessionTime(long millis) {
        this.possessionTime += millis;
    }

    public void addMinutesPlayed(long millis) {
        this.minutesPlayed += millis;
    }

    public void incrementFGMade() {
        ++this.fgMade;
    }

    public void incrementFGAttempted() {
        ++this.fgAttempted;
    }

    public void increment3FGMade() {
        ++this.fg3Made;
    }

    public void increment3FGAttempted() {
        ++this.fg3Attempted;
    }

    public void increment2FGMade() {
        ++this.fg2Made;
    }

    public void increment2FGAttempted() {
        ++this.fg2Attempted;
    }

    public void incrementPassAttempts() {
        ++this.passAttempts;
    }
    
    public void addActivePlayTime(long millis) {
        this.activePlayTime += millis;
    }

    public void addContestTime(UUID opponent, int time) {
        this.contestTime.merge(opponent, time, Integer::sum);
    }

    public UUID getTopContestedOpponent() {
        return this.contestTime.entrySet().stream().max((e1, e2) -> Integer.compare(e1.getValue(), e2.getValue())).map(Map.Entry::getKey).orElse(null);
    }

    public void resetContestTime() {
        this.contestTime.clear();
    }

    public void reset() {
        this.points = 0;
        this.threes = 0;
        this.assists = 0;
        this.rebounds = 0;
        this.offensiveRebounds = 0;
        this.defensiveRebounds = 0;
        this.steals = 0;
        this.turnovers = 0;
        this.missesAgainst = 0;
        this.possessionTime = 0L;
        this.fgMade = 0;
        this.fgAttempted = 0;
        this.fg3Made = 0;
        this.fg3Attempted = 0;
        this.fg2Made = 0;
        this.fg2Attempted = 0;
        this.passAttempts = 0;
        this.blocks = 0;
        this.minutesPlayed = 0L;
        this.activePlayTime = 0L;
        this.lastActivePlayUpdate = System.currentTimeMillis();
        this.resetContestTime();
        // Initialize stamina
        this.currentStamina = 20.0;
        this.maxStamina = 20.0;
        this.lastStaminaUpdate = System.currentTimeMillis();
    }
    
    public String getStaminaStatus() {
        if (this.currentStamina >= 18.0) {
            return "§aExcellent";
        } else if (this.currentStamina >= 16.0) {
            return "§2Good";
        } else if (this.currentStamina >= 12.0) {
            return "§eFatigued";
        } else if (this.currentStamina >= 8.0) {
            return "§6Tired";
        } else if (this.currentStamina >= 4.0) {
            return "§cExhausted";
        } else {
            return "§4Drained";
        }
    }
    
    public float getSpeedModifier() {
        // Returns a multiplier for speed based on stamina level
        // 20-18: 1.0 (100% speed)
        // 18-16: 0.95 (95% speed)
        // 16-12: 0.90 (90% speed)
        // 12-8: 0.85 (85% speed)
        // 8-4: 0.80 (80% speed)
        // <4: 0.75 (75% speed)
        if (this.currentStamina >= 18.0) {
            return 1.0f;
        } else if (this.currentStamina >= 16.0) {
            return 0.95f;
        } else if (this.currentStamina >= 12.0) {
            return 0.90f;
        } else if (this.currentStamina >= 8.0) {
            return 0.85f;
        } else if (this.currentStamina >= 4.0) {
            return 0.80f;
        } else {
            return 0.75f;
        }
    }
}

