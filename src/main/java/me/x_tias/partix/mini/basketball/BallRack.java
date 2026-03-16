package me.x_tias.partix.mini.basketball;

import dev.lone.itemsadder.api.CustomStack;
import me.x_tias.partix.Partix;
import me.x_tias.partix.mini.game.GoalGame;
import me.x_tias.partix.plugin.ball.Ball;
import me.x_tias.partix.plugin.ball.BallFactory;
import me.x_tias.partix.plugin.ball.BallType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;

public class BallRack {

    private static final String RACK_ITEM_ID = "partix:ball_rack";
    private static final String BALL_ITEM_ID = "partix:basketball";
    private static final int ROWS = 2;
    private static final int BALLS_PER_ROW = 4;
    private static final float BALL_SCALE = 0.9f;

    // Static map: interaction entity UUID -> the BallRack it belongs to
    private static final Map<UUID, BallRack> interactionMap = new HashMap<>();

    private final Location location;
    private final float yaw;
    private final GoalGame game;
    private ItemDisplay rackEntity;
    private final List<ItemDisplay> ballEntities = new ArrayList<>();
    private Interaction rackInteraction;

    public BallRack(Location location, float yaw, GoalGame game) {
        this.location = location.clone();
        this.yaw = yaw;
        this.game = game;
        spawn();
    }

    private void spawn() {
        World world = location.getWorld();

        rackEntity = (ItemDisplay) world.spawnEntity(location, EntityType.ITEM_DISPLAY);
        rackEntity.setGravity(false);
        rackEntity.setPersistent(false);
        rackEntity.setInvulnerable(true);
        rackEntity.setSilent(true);
        rackEntity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);

        CustomStack rackStack = CustomStack.getInstance(RACK_ITEM_ID);
        if (rackStack != null) {
            rackEntity.setItemStack(rackStack.getItemStack());
        } else {
            rackEntity.setItemStack(new ItemStack(Material.IRON_BARS));
        }

        Transformation rackTransform = rackEntity.getTransformation();
        float rackScale = 1.5f;
        rackTransform.getScale().set(new Vector3f(rackScale, rackScale, rackScale));
        float totalYaw = (float) Math.toRadians(yaw + 180.0);
        rackTransform.getLeftRotation().set(new AxisAngle4f(totalYaw, 0f, 1f, 0f));
        rackEntity.setTransformation(rackTransform);

        Location rackLoc = location.clone();
        rackEntity.teleport(rackLoc);

        spawnBalls(world);

        // One big Interaction entity covering the whole rack
        rackInteraction = (Interaction) world.spawnEntity(location.clone().add(0, 0.5, 0), EntityType.INTERACTION);
        rackInteraction.setInteractionWidth(5.0f);
        rackInteraction.setInteractionHeight(2.5f);
        rackInteraction.setPersistent(false);
        interactionMap.put(rackInteraction.getUniqueId(), this);
    }

    private void spawnBalls(World world) {
        CustomStack ballStack = CustomStack.getInstance(BALL_ITEM_ID);
        ItemStack ballItem;
        if (ballStack != null) {
            ballItem = ballStack.getItemStack();
        } else {
            ballItem = new ItemStack(Material.ORANGE_CONCRETE);
        }

        double rad = Math.toRadians(yaw);
        double cosYaw = Math.cos(rad);
        double sinYaw = Math.sin(rad);

        double[] shelfHeights = {0.15, 1.2};

        double rackWidth = 4.0;
        double startOffset = -rackWidth / 2.0 + 0.5;
        double ballSpacing = rackWidth / 3.5;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < BALLS_PER_ROW; col++) {
                double alongRack = startOffset + col * ballSpacing;

                double dx = alongRack * cosYaw;
                double dz = alongRack * (-sinYaw);

                Location ballLoc = location.clone().add(dx, shelfHeights[row], dz);
                ballLoc.setYaw(yaw);

                ItemDisplay ballEntity = (ItemDisplay) world.spawnEntity(ballLoc, EntityType.ITEM_DISPLAY);
                ballEntity.setGravity(false);
                ballEntity.setPersistent(false);
                ballEntity.setInvulnerable(true);
                ballEntity.setSilent(true);
                ballEntity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                ballEntity.setItemStack(ballItem.clone());

                Transformation t = ballEntity.getTransformation();
                t.getScale().set(new Vector3f(BALL_SCALE, BALL_SCALE, BALL_SCALE));
                ballEntity.setTransformation(t);
                ballEntity.teleport(ballLoc);

                ballEntities.add(ballEntity);
            }
        }
    }

    /**
     * Remove one ball visually from the rack (last one first).
     * Returns true if a ball was available.
     */
    private boolean takeOneBall() {
        // Remove from the end (top shelf first)
        for (int i = ballEntities.size() - 1; i >= 0; i--) {
            ItemDisplay ball = ballEntities.get(i);
            if (ball != null && !ball.isDead()) {
                ball.remove();
                ballEntities.remove(i);
                return true;
            }
        }
        return false;
    }

    public void remove() {
        if (rackEntity != null && !rackEntity.isDead()) {
            rackEntity.remove();
        }
        for (ItemDisplay ball : ballEntities) {
            if (ball != null && !ball.isDead()) {
                ball.remove();
            }
        }
        ballEntities.clear();
        if (rackInteraction != null) {
            interactionMap.remove(rackInteraction.getUniqueId());
            if (!rackInteraction.isDead()) {
                rackInteraction.remove();
            }
        }
    }

    public static class InteractListener implements Listener {
        @EventHandler
        public void onAttackEntity(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player player)) return;
            BallRack rack = interactionMap.get(event.getEntity().getUniqueId());
            if (rack == null) return;

            event.setCancelled(true);

            if (!rack.takeOneBall()) return;

            Location spawnLoc = player.getLocation().add(0, 0.5, 0);
            Ball ball = BallFactory.create(spawnLoc, BallType.BASKETBALL, rack.game);
            // stealDelay starts at 5, so schedule pickup after it expires
            Bukkit.getScheduler().runTaskLater(Partix.getInstance(), () -> {
                if (player.isOnline()) {
                    ball.setVelocity(player, 0, 0, 0);
                }
            }, 6L);
        }
    }
}
