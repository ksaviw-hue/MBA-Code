package me.x_tias.partix.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import me.x_tias.partix.mini.basketball.BasketballGame;
import me.x_tias.partix.mini.game.PlayerStats;
import me.x_tias.partix.plugin.athlete.Athlete;
import me.x_tias.partix.plugin.athlete.AthleteManager;
import me.x_tias.partix.util.Colour;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("stamina|energy")
public class StaminaCommand extends BaseCommand {
    
    @Default
    @CommandCompletion("@players")
    public void onStamina(Player sender, @Optional String targetName) {
        Player target;
        
        // If no target specified, show sender's stamina
        if (targetName == null || targetName.isEmpty()) {
            target = sender;
        } else {
            // Try to find the target player
            target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage(Component.text("❌ Player not found: ", NamedTextColor.RED)
                        .append(Component.text(targetName, NamedTextColor.YELLOW)));
                return;
            }
        }
        
        // Find the player's current game
        Athlete athlete = AthleteManager.get(target.getUniqueId());
        if (athlete == null || !(athlete.getPlace() instanceof BasketballGame)) {
            sender.sendMessage(Component.text("❌ ", NamedTextColor.RED)
                    .append(Component.text(target.getName() + " is not in a basketball game.", NamedTextColor.YELLOW)));
            return;
        }
        
        BasketballGame basketballGame = (BasketballGame) athlete.getPlace();
        
        // Check if stamina is enabled
        if (!basketballGame.settings.staminaEnabled) {
            sender.sendMessage(Component.text("❌ ", NamedTextColor.RED)
                    .append(Component.text("Stamina system is not enabled for this game.", NamedTextColor.YELLOW)));
            return;
        }
        
        // Get player's stamina stats
        PlayerStats stats = basketballGame.getStatsManager().getPlayerStats(target.getUniqueId());
        if (stats == null) {
            sender.sendMessage(Component.text("❌ ", NamedTextColor.RED)
                    .append(Component.text("Could not find stats for " + target.getName(), NamedTextColor.YELLOW)));
            return;
        }
        
        // Format and display stamina information
        String staminaStatus = stats.getStaminaStatus();
        double currentStamina = stats.getCurrentStamina();
        double maxStamina = stats.getMaxStamina();
        boolean isOnBench = basketballGame.isInBench(target);
        
        // Create stamina bar visualization
        int filledBars = (int) Math.ceil((currentStamina / maxStamina) * 20);
        StringBuilder staminaBar = new StringBuilder("§a");
        for (int i = 0; i < 20; i++) {
            if (i < filledBars) {
                staminaBar.append("█");
            } else {
                staminaBar.append("§8█");
            }
        }
        
        // Send stamina info
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("⚡ ", NamedTextColor.YELLOW)
                .append(Component.text(target.getName() + "'s Stamina", NamedTextColor.GOLD)));
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("  Energy: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%.1f", currentStamina) + " / " + String.format("%.1f", maxStamina), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("  " + staminaBar.toString()));
        sender.sendMessage(Component.text("  Status: ").color(NamedTextColor.GRAY)
                .append(Component.text(staminaStatus)));
        sender.sendMessage(Component.text("  Location: ", NamedTextColor.GRAY)
                .append(Component.text(isOnBench ? "On Bench" : "On Court", 
                        isOnBench ? NamedTextColor.GREEN : NamedTextColor.YELLOW)));
        sender.sendMessage(Component.text("  Speed: ", NamedTextColor.GRAY)
                .append(Component.text((int)(stats.getSpeedModifier() * 100) + "%", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text(""));
    }
}
