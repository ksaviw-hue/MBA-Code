package me.x_tias.partix.mini;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.x_tias.partix.Partix;
import me.x_tias.partix.mini.basketball.BasketballGame;
import me.x_tias.partix.mini.game.GoalGame;
import me.x_tias.partix.plugin.athlete.Athlete;
import me.x_tias.partix.plugin.athlete.AthleteManager;
import me.x_tias.partix.server.Place;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;


public class GamePlaceholderExpansion extends PlaceholderExpansion {
    @Override
    public String getAuthor() {
        return "l3st4t";
    }

    @Override
    public String getIdentifier() {
        return "basketballgame";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; //
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        Athlete ath = AthleteManager.getNoFallback(player.getUniqueId());
        if (ath == null) {
            return "N/A";
        }
        Place place = ath.getPlace();
        if (!(place instanceof BasketballGame current)) {
            return "N/A";
        }
        
        return switch (params.toLowerCase()) {
            case "home_team" -> PlainTextComponentSerializer.plainText().serialize(current.home.abrv);
            case "away_team" -> PlainTextComponentSerializer.plainText().serialize(current.away.abrv);
            case "home_score" -> current.homeScore + "";
            case "away_score" -> current.awayScore + "";
            case "time_remaining" -> current.getGameTime();
            case "quarter" -> current.getPeriodNameHud();
            case "shot_clock" -> (current.getShotClockTicks() / 20) + "";
            default -> null;
        };
    }
}
