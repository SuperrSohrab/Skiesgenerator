package com.itzacat.gui;

import com.itzacat.Plugin;
import com.itzacat.generator.Generator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratorMenu {
    private final Plugin plugin;
    private final Generator generator;
    private final Inventory inventory;

    // Track which generator an inventory belongs to
    private static final Map<Inventory, Generator> INVENTORY_MAP = new HashMap<>();

    public GeneratorMenu(Plugin plugin, Generator generator) {
        this.plugin = plugin;
        this.generator = generator;
        this.inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Generator Storage");
        populateMenu();
        INVENTORY_MAP.put(this.inventory, generator);
    }

    private void populateMenu() {
        // Fill border with glass panes
        ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            borderItem.setItemMeta(borderMeta);
        }

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderItem);
            inventory.setItem(i + 45, borderItem);
        }
        for (int i = 0; i < 54; i += 9) {
            inventory.setItem(i, borderItem);
            inventory.setItem(i + 8, borderItem);
        }

        // Generator info item
        ItemStack infoItem = new ItemStack(generator.getTier().getBlockMaterial());
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(ChatColor.GOLD + "Tier " + generator.getTier().getTier() + " Generator");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Tier: " + ChatColor.YELLOW + generator.getTier().getTier());
            lore.add(ChatColor.GRAY + "Produces: " + ChatColor.WHITE + generator.getTier().getDropMaterial().name());
            lore.add(ChatColor.GRAY + "Storage: " + ChatColor.WHITE + generator.getTotalItems() + "/" + generator.getMaxStorage());
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(4, infoItem);

        // Collect all button
        ItemStack collectItem = new ItemStack(Material.HOPPER);
        ItemMeta collectMeta = collectItem.getItemMeta();
        if (collectMeta != null) {
            collectMeta.setDisplayName(ChatColor.GREEN + "Collect All Items");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to collect all items");
            lore.add(ChatColor.GRAY + "Total items: " + ChatColor.WHITE + generator.getTotalItems());
            collectMeta.setLore(lore);
            collectItem.setItemMeta(collectMeta);
        }
        inventory.setItem(49, collectItem);

        // Upgrade button
        Generator nextTier = null;
        try {
            // Get next tier for display
            com.itzacat.generator.GeneratorTier currentTier = generator.getTier();
            com.itzacat.generator.GeneratorTier upgradeT = currentTier.getNextTier();
            
            ItemStack upgradeItem = new ItemStack(Material.ANVIL);
            ItemMeta upgradeMeta = upgradeItem.getItemMeta();
            if (upgradeMeta != null) {
                if (upgradeT != null) {
                    upgradeMeta.setDisplayName(ChatColor.AQUA + "Upgrade Generator");
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Current Tier: " + ChatColor.YELLOW + currentTier.name());
                    lore.add(ChatColor.GRAY + "Next Tier: " + ChatColor.YELLOW + upgradeT.name());
                    lore.add(ChatColor.GRAY + "Cost: " + ChatColor.GREEN + "$" + String.format("%.2f", upgradeT.getUpgradeCost()));
                    lore.add(ChatColor.GRAY + "Click to upgrade");
                    upgradeMeta.setLore(lore);
                } else {
                    upgradeMeta.setDisplayName(ChatColor.RED + "Max Tier Reached");
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "This generator is at max level");
                    upgradeMeta.setLore(lore);
                }
                upgradeItem.setItemMeta(upgradeMeta);
            }
            inventory.setItem(47, upgradeItem);
        } catch (Exception e) {
            // Handle error
        }

        // Take generator button (get the block in your inventory)
        ItemStack takeItem = new ItemStack(Material.CHEST);
        ItemMeta takeMeta = takeItem.getItemMeta();
        if (takeMeta != null) {
            takeMeta.setDisplayName(ChatColor.RED + "Take Generator");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Take this generator into your inventory");
            lore.add(ChatColor.GRAY + "Removes the generator from the world");
            takeMeta.setLore(lore);
            takeItem.setItemMeta(takeMeta);
        }
        inventory.setItem(46, takeItem);

        // Display collected items
        int slot = 10;
        for (ItemStack item : generator.getCollectedItems()) {
            if (slot >= 44 && slot % 9 == 0) slot++;
            if (slot >= 44 && slot % 9 == 8) slot++;
            if (slot >= 45) break;
            
            inventory.setItem(slot, item);
            slot++;
            if (slot % 9 == 0 || slot % 9 == 8) slot++;
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public Generator getGenerator() {
        return generator;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public static Generator getGeneratorByInventory(Inventory inventory) {
        return INVENTORY_MAP.get(inventory);
    }

    public static void unregisterInventory(Inventory inventory) {
        INVENTORY_MAP.remove(inventory);
    }
}
