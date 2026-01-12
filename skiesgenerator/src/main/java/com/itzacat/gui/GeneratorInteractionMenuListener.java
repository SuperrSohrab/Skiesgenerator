package com.itzacat.gui;

import com.itzacat.Plugin;
import com.itzacat.generator.Generator;
import com.itzacat.generator.GeneratorTier;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class GeneratorInteractionMenuListener implements Listener {
    private final Plugin plugin;

    public GeneratorInteractionMenuListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        if (event.getView().getTitle().equals(ChatColor.DARK_GRAY + "Generator Options")) {
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            // Check if player clicked the upgrade button
            if (clicked.getType() == Material.ANVIL) {
                Generator generator = GeneratorInteractionMenu.getGeneratorByInventory(event.getInventory());
                if (generator != null) {
                    handleUpgrade(player, generator);
                }
            }

            // Check if player clicked the take generator button
            if (clicked.getType() == Material.CHEST) {
                Generator generator = GeneratorInteractionMenu.getGeneratorByInventory(event.getInventory());
                if (generator != null) {
                    // Ensure player owns the generator
                    if (!generator.getOwner().equals(player.getUniqueId()) && !player.hasPermission("skiesgenerator.admin")) {
                        player.sendMessage(ChatColor.RED + "You don't own this generator!");
                        return;
                    }

                    // Create generator item (marked as block item)
                    ItemStack genItem = new ItemStack(generator.getTier().getBlockMaterial());
                    ItemMeta meta = genItem.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(ChatColor.GOLD + "Tier " + generator.getTier().getTier() + " Generator");
                        java.util.List<String> lore = new java.util.ArrayList<>();
                        lore.add(ChatColor.GRAY + "Place this block to create a generator");
                        lore.add(ChatColor.GRAY + "Produces: " + ChatColor.WHITE + generator.getTier().getDropMaterial().name());
                        lore.add(ChatColor.GRAY + "Tier: " + ChatColor.YELLOW + generator.getTier().getTier());
                        meta.setLore(lore);
                        NamespacedKey key = new NamespacedKey(plugin, "generator_block");
                        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte)1);
                        genItem.setItemMeta(meta);
                    }

                    // Try to give generator item to player
                    java.util.Map<Integer, ItemStack> leftovers = player.getInventory().addItem(genItem);
                    if (!leftovers.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "Not enough inventory space to take the generator!");
                        return;
                    }

                    // Transfer collected items to player (best effort)
                    for (ItemStack item : generator.getCollectedItems()) {
                        java.util.Map<Integer, ItemStack> left = player.getInventory().addItem(item);
                        if (!left.isEmpty()) {
                            // Drop any leftovers at player's location
                            for (ItemStack rem : left.values()) {
                                player.getWorld().dropItemNaturally(player.getLocation(), rem);
                            }
                        }
                    }

                    // Remove generator block from world, remove generator and save
                    if (generator.getLocation() != null && generator.getLocation().getBlock() != null && generator.getLocation().getWorld() != null) {
                        generator.getLocation().getBlock().setType(org.bukkit.Material.AIR);
                    }
                    plugin.getGeneratorManager().removeGenerator(generator.getLocation());
                    plugin.getGeneratorStorage().saveData();
                    player.sendMessage(ChatColor.GREEN + "You took the generator and its items have been moved to your inventory.");
                    player.closeInventory();
                }
            }
        }
    }

    private void handleUpgrade(Player player, Generator generator) {
        if (!player.hasPermission("skiesgenerator.upgrade")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to upgrade generators!");
            return;
        }

        GeneratorTier nextTier = generator.getTier().getNextTier();
        if (nextTier == null) {
            player.sendMessage(ChatColor.RED + "This generator is already at maximum tier!");
            return;
        }

        if (!plugin.getConfig().getBoolean("use-vault", true)) {
            player.sendMessage(ChatColor.RED + "Economy system is disabled!");
            return;
        }

        Economy economy = plugin.getEconomy();
        if (economy == null) {
            player.sendMessage(ChatColor.RED + "Economy system not found!");
            return;
        }

        double cost = nextTier.getUpgradeCost();
        if (economy.getBalance(player) < cost) {
            player.sendMessage(ChatColor.RED + "You need $" + String.format("%.2f", cost) + " to upgrade this generator!");
            return;
        }

        economy.withdrawPlayer(player, cost);
        generator.setTier(nextTier);
        generator.getLocation().getBlock().setType(nextTier.getBlockMaterial());
        
        player.sendMessage(ChatColor.GREEN + "Generator upgraded to " + nextTier.name() + " for $" + String.format("%.2f", cost));
        player.closeInventory();
        plugin.getGeneratorStorage().saveData();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ChatColor.DARK_GRAY + "Generator Options")) {
            GeneratorInteractionMenu.unregisterInventory(event.getInventory());
        }
    }
}
