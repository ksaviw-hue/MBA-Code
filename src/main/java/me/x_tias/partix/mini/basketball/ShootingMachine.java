package me.x_tias.partix.mini.basketball;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collection;

public class ShootingMachine {

    private static final String MACHINE_ITEM_ID = "partix:shooting_machine";
    private static final float MACHINE_SCALE = 4.0f;

    private final Location baseLocation;
    private ItemDisplay machineEntity;

    public ShootingMachine(Location hoopCenter, double floorY, double courtCenterX, double courtCenterZ) {
        this.baseLocation = new Location(
                hoopCenter.getWorld(),
                courtCenterX,
                floorY,
                hoopCenter.getZ()
        );
        spawn();
    }

    private void spawn() {
        World world = baseLocation.getWorld();

        machineEntity = (ItemDisplay) world.spawnEntity(baseLocation, EntityType.ITEM_DISPLAY);
        machineEntity.setGravity(false);
        machineEntity.setPersistent(false);
        machineEntity.setInvulnerable(true);
        machineEntity.setSilent(true);
        machineEntity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);

        CustomStack customStack = CustomStack.getInstance(MACHINE_ITEM_ID);
        if (customStack != null) {
            machineEntity.setItemStack(customStack.getItemStack());
        } else {
            System.out.println("[Partix] WARNING: ItemsAdder item '" + MACHINE_ITEM_ID + "' not found!");
            machineEntity.setItemStack(new ItemStack(Material.IRON_BLOCK));
        }

        // Fixed scale, no rotation
        // FIXED display renders model JSON (8,8,8) at entity origin.
        // Model center is at JSON (0,~8,0), so it's at (-0.5, 0, -0.5) pre-scale.
        // With scale 4, that's (-2, 0, -2) offset. Compensate with translation.
        Transformation t = new Transformation(
                new Vector3f(2.0f, 0, 2.0f),
                new Quaternionf(),
                new Vector3f(MACHINE_SCALE, MACHINE_SCALE, MACHINE_SCALE),
                new Quaternionf()
        );
        machineEntity.setTransformation(t);
    }

    /**
     * No-op: model stays fixed under basket.
     */
    public void rotateTowardClosest(Collection<? extends Player> players) {
    }

    /**
     * Get the launcher position (at the machine base).
     */
    public Location getLauncherLocation() {
        return baseLocation.clone().add(0, 0.5, 0);
    }

    public void remove() {
        if (machineEntity != null && !machineEntity.isDead()) {
            machineEntity.remove();
        }
        machineEntity = null;
    }

    public boolean isAlive() {
        return machineEntity != null && !machineEntity.isDead();
    }
}
