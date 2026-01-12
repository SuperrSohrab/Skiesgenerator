package com.itzacat.tasks;

import com.itzacat.Plugin;
import com.itzacat.generator.Generator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class GeneratorTask extends BukkitRunnable {
    private final Plugin plugin;

    public GeneratorTask(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        String dropMode = plugin.getConfig().getString("drop-mode", "collect");
        
        for (Generator generator : plugin.getGeneratorManager().getAllGenerators()) {
            Location location = generator.getLocation();
            Material dropMaterial = generator.getTier().getDropMaterial();
            int multiplier = generator.getBoosterMultiplier();
            int totalToGive = multiplier;

            // Respect max storage when in collect mode
            if (dropMode.equalsIgnoreCase("collect")) {
                int current = generator.getTotalItems();
                int space = generator.getMaxStorage() - current;
                if (space <= 0) continue;
                totalToGive = Math.min(totalToGive, space);
            }

            if (totalToGive <= 0) continue;

            ItemStack drop = new ItemStack(dropMaterial, totalToGive);
            // Mark the item as a generator drop
            markAsGeneratorDrop(drop);

            if (dropMode.equalsIgnoreCase("block")) {
                // Spawn item centered on top of the generator block and prevent it from flying
                Location dropLocation = location.clone().add(0.5, 1.0, 0.5);
                if (location.getWorld() != null) {
                    Item spawned = location.getWorld().dropItem(dropLocation, drop);
                    // Ensure it doesn't fly off
                    spawned.setVelocity(new Vector(0, 0, 0));
                    // Small pickup delay to avoid immediate pickup issues
                    spawned.setPickupDelay(plugin.getConfig().getInt("generator-pickup-delay", 40));
                }
            } else if (dropMode.equalsIgnoreCase("collect")) {
                // Add to generator's internal storage
                if (!generator.addItem(drop)) {
                    // Storage full, optionally notify the owner
                    if (Bukkit.getPlayer(generator.getOwner()) != null) {
                        // Could send a message here
                    }
                }
            }
        }
    }
    
    private void markAsGeneratorDrop(ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Use NBT data to mark the item as a drop
            NamespacedKey key = new NamespacedKey(plugin, "generator_drop");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            
            // Also add visible lore for players
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + "GEN" + ChatColor.RESET + ChatColor.DARK_GRAY + " [Generator Item]");
            meta.setLore(lore);
            
            item.setItemMeta(meta);
        }
    }
}
