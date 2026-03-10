/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.inventory.InventoryInteractEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package me.x_tias.partix.plugin.listener;

import me.x_tias.partix.Partix;
import me.x_tias.partix.mini.basketball.BasketballGame;
import me.x_tias.partix.mini.game.GoalGame;
import me.x_tias.partix.plugin.athlete.Athlete;
import me.x_tias.partix.plugin.athlete.AthleteManager;
import me.x_tias.partix.plugin.ball.Ball;
import me.x_tias.partix.plugin.ball.BallFactory;
import me.x_tias.partix.plugin.ball.BallType;
import me.x_tias.partix.plugin.ball.event.BallHitEntityEvent;
import me.x_tias.partix.plugin.ball.event.PlayerHitBallEvent;
import me.x_tias.partix.plugin.ball.event.PressDropKeyEvent;
import me.x_tias.partix.plugin.ball.event.PressLeftClickEvent;
import me.x_tias.partix.plugin.ball.event.PressRightClickEvent;
import me.x_tias.partix.plugin.ball.event.PressSwapKeyEvent;
import me.x_tias.partix.plugin.ball.types.Basketball;
import me.x_tias.partix.plugin.mechanic.Mechanic;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionListener
implements Listener {
    @EventHandler
    public void onHitBall(PlayerHitBallEvent e) {
        Ball ball = e.getBall();
        Player player = e.getPlayer();
        if (AthleteManager.get(player.getUniqueId()).isSpectating()) {
            return;
        }
        Mechanic.punch(player, ball);
    }

    @EventHandler
    public void onRightClick(PressRightClickEvent e) {
        Player player = e.getPlayer();
        Athlete athlete = e.getAthlete();
        if (athlete.getPlace() != null) {
            athlete.getPlace().clickItem(player, e.getItemStack());
        }
        if (athlete.isSpectating()) {
            return;
        }
        for (Ball ball : BallFactory.getNearby(player.getLocation(), 3.5)) {
            if (ball.getCurrentDamager() == null || !ball.getCurrentDamager().equals((Object)player)) continue;
            Mechanic.rightClick(player, ball, e.isThrownInBlock());
            break;
        }
    }

    @EventHandler
    public void onLeftClick(PressLeftClickEvent e) {
        Player player = e.getPlayer();
        if (!e.getAthlete().isSpectating()) {
            BallFactory.getNearest(player.getLocation(), 2.3).ifPresent(ball -> Mechanic.leftClick(player, ball, e.isThrownInBlock()));
        }
    }

    @EventHandler
    public void onBallHitPlayer(BallHitEntityEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof Player) {
            Player player = (Player)entity;
            e.setCancelled(Mechanic.collides(player, e.getBall()));
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PressDropKeyEvent e) {
        Player player = e.getPlayer();
        for (Ball ball : BallFactory.getNearby(player.getLocation(), 4.0)) {
            Basketball bball;
            if (!(ball instanceof Basketball) || (bball = (Basketball)ball).getCurrentDamager() == null) continue;
            if (!bball.canDunk(player)) {
                if (!player.isOnGround()) continue;
                final BasketballGame game = bball.getGame();
                final GoalGame.Team theirTeam = game.getTeamOf(player);
                for (int i = 0; i < player.getInventory().getSize(); ++i) {
                    ItemStack it = player.getInventory().getItem(i);
                    if (it == null || it.getType() != Material.POLISHED_BLACKSTONE_BUTTON) continue;
                    player.getInventory().setItem(i, null);
                    break;
                }
                player.updateInventory();
                Location spawnLoc = player.getEyeLocation().clone().add(0.0, -0.5, 0.0);
                final Ball fakeInboundBall = BallFactory.create(spawnLoc, BallType.BASKETBALL, game);
                fakeInboundBall.setVelocity(0.0, 0.1, 0.0);
                new BukkitRunnable(){

                    public void run() {
                        BallFactory.remove(fakeInboundBall);
                        game.callTimeout(theirTeam);
                    }
                }.runTaskLater((Plugin)Partix.getInstance(), 1L);
                e.setKeepItem(false);
                return;
            }
            if (!Mechanic.dropItem(player, ball, e.isThrownInBlock())) continue;
            if ((bball.getGame().getState().equals((Object)GoalGame.State.REGULATION) || bball.getGame().getState().equals((Object)GoalGame.State.OVERTIME)) && !bball.isShotAttemptRegistered()) {
                bball.getGame().onShotAttempt(player, false);
                bball.setShotAttemptRegistered(true);
            }
            e.setKeepItem(false);
            return;
        }
    }

    @EventHandler
    public void onPlayerSwapItemEvent(PressSwapKeyEvent e) {
        Player player = e.getPlayer();
        for (Ball ball : BallFactory.getNearby(player.getLocation(), 4.0)) {
            if (ball.getCurrentDamager() != null && Mechanic.swapItem(player, ball, e.isThrownInBlock())) break;
        }
    }

    @EventHandler
    public void onAttackWithBall(EntityDamageByEntityEvent e) {
        e.setCancelled(true);
        Object object = e.getDamager();
        if (object instanceof Player) {
            Player attacker = (Player)object;
            object = e.getEntity();
            if (object instanceof Player) {
                Player damaged = (Player)object;
                if (attacker.getLocation().distance(damaged.getLocation()) < 2.5) {
                    for (Ball ball : BallFactory.getNearby(attacker.getLocation(), 4.0)) {
                        if (ball.getCurrentDamager() == null) continue;
                        Mechanic.attack(attacker, damaged, ball);
                    }
                }
            }
        }
    }

    @EventHandler
    public void changeItems(InventoryInteractEvent e) {
        Player player;
        HumanEntity humanEntity = e.getWhoClicked();
        if (humanEntity instanceof Player && (player = (Player)humanEntity).getGameMode().equals((Object)GameMode.ADVENTURE)) {
            e.setCancelled(true);
        }
    }
}
