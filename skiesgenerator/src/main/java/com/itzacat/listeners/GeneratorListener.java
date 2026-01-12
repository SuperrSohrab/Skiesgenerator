package com.itzacat.listeners;

import com.itzacat.Plugin;
import com.itzacat.generator.Generator;
import com.itzacat.generator.GeneratorTier;
import com.itzacat.gui.GeneratorMenu;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GeneratorListener implements Listener {
    private final Plugin plugin;

    public GeneratorListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        ItemStack item = event.getItemInHand();

        // Check if the item has generator metadata
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName() && meta.getDisplayName().contains("Generator")) {
                // This is a generator item
                GeneratorTier tier = GeneratorTier.fromMaterial(block.getType());
                if (tier != null) {
                    int maxStorage = plugin.getConfig().getInt("max-storage", 64);
                    int maxPerPlayer = plugin.getConfig().getInt("max-generators-per-player", 5);
                    if (maxPerPlayer > 0) {
                        int owned = plugin.getGeneratorManager().getGeneratorsByOwner(player.getUniqueId()).size();
                        if (owned >= maxPerPlayer) {
                            event.setCancelled(true);
                            player.sendMessage(ChatColor.RED + "You have reached the maximum number of generators (" + maxPerPlayer + ").");
                            return;
                        }
                    }

                    plugin.getGeneratorManager().createGenerator(
                        block.getLocation(),
                        tier,
                        player.getUniqueId(),
                        maxStorage
                    );
                    player.sendMessage(ChatColor.GREEN + "Generator placed successfully!");
                    plugin.getGeneratorStorage().saveData();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Generator generator = plugin.getGeneratorManager().getGenerator(block);

        if (generator != null) {
            // Prevent mining; require taking the generator via the GUI
            event.setCancelled(true);
            if (event.getPlayer() != null) {
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot mine generators. Open the generator GUI and use 'Take Generator' to remove it.");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        Generator generator = plugin.getGeneratorManager().getGenerator(block);

        if (generator != null && event.getAction().name().contains("RIGHT_CLICK")) {
            Player player = event.getPlayer();
            
            if (!generator.getOwner().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You don't own this generator!");
                event.setCancelled(true);
                return;
            }

            event.setCancelled(true);
            String dropMode = plugin.getConfig().getString("drop-mode", "collect");
            
            // Open collection menu for collect mode
            if (dropMode.equalsIgnoreCase("collect")) {
                new GeneratorMenu(plugin, generator).open(player);
            } else {
                // Open interaction menu for block mode
                new com.itzacat.gui.GeneratorInteractionMenu(plugin, generator).open(player);
            }
        }
    }

    private ItemStack createGeneratorItem(GeneratorTier tier) {
        ItemStack item = new ItemStack(tier.getBlockMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Tier " + tier.getTier() + " Generator");
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(ChatColor.GRAY + "Place this block to create a generator");
            lore.add(ChatColor.GRAY + "Produces: " + ChatColor.WHITE + tier.getDropMaterial().name());
            lore.add(ChatColor.GRAY + "Tier: " + ChatColor.YELLOW + tier.getTier());
            meta.setLore(lore);
            
            // Mark as generator block item with NBT data
            NamespacedKey key = new NamespacedKey(plugin, "generator_block");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            
            item.setItemMeta(meta);
        }
        return item;
    }
}
