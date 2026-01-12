package com.itzacat.listeners;

import com.itzacat.Plugin;
import com.itzacat.generator.GeneratorTier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ItemProtectionListener implements Listener {
    private final Plugin plugin;

    public ItemProtectionListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        // Prevent crafting with generator items - check before the craft happens
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item != null && isGeneratorDrop(item)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCraftingTableClick(InventoryClickEvent event) {
        // Block crafting table interactions with generator items
        if (event.getView().getTopInventory().getType() == InventoryType.CRAFTING) {
            int raw = event.getRawSlot();
            boolean inCraftingTop = raw >= 0 && raw < event.getView().getTopInventory().getSize();

            if (inCraftingTop) {
                ItemStack cursor = event.getCursor();
                ItemStack current = event.getCurrentItem();
                
                // Block putting generator items in crafting slots
                if (cursor != null && isGeneratorDrop(cursor)) {
                    event.setCancelled(true);
                    if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
                        ((org.bukkit.entity.Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "You cannot craft with generator items!");
                    }
                    return;
                }
                
                // Block shift-clicking generator items into crafting
                if (event.isShiftClick() && current != null && isGeneratorDrop(current)) {
                    event.setCancelled(true);
                    if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
                        ((org.bukkit.entity.Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "You cannot craft with generator items!");
                    }
                    return;
                }
                
                // Block taking craft result if inputs have generator items
                if (raw == 0) { // Result slot in crafting grid
                    for (int i = 1; i < 10; i++) { // Check crafting grid (slots 1-9)
                        ItemStack gridItem = event.getView().getTopInventory().getItem(i);
                        if (gridItem != null && isGeneratorDrop(gridItem)) {
                            event.setCancelled(true);
                            if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
                                ((org.bukkit.entity.Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "You cannot craft with generator items!");
                            }
                            return;
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCraftingTableDrag(InventoryDragEvent event) {
        // Block dragging generator items into crafting table
        if (event.getView().getTopInventory().getType() == InventoryType.CRAFTING) {
            if (event.getOldCursor() != null && isGeneratorDrop(event.getOldCursor())) {
                int topSize = event.getView().getTopInventory().getSize();
                for (int slot : event.getRawSlots()) {
                    if (slot >= 0 && slot < topSize) {
                        event.setCancelled(true);
                        if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
                            ((org.bukkit.entity.Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "You cannot craft with generator items!");
                        }
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onMerchantClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getType() != InventoryType.MERCHANT) return;

        // Slots 0 and 1 are inputs, 2 is result in merchant top inventory
        int raw = event.getRawSlot();
        boolean inMerchantTop = raw >= 0 && raw < event.getView().getTopInventory().getSize();

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // Block shift-clicking generator items from player inventory into merchant
        if (event.isShiftClick() && current != null && isGeneratorDrop(current)) {
            event.setCancelled(true);
            sendTradeBlocked(event);
            return;
        }

        // Block any interaction with merchant input slots or result slot if generator items are involved
        if (inMerchantTop && (raw == 0 || raw == 1 || raw == 2)) {
            if ((cursor != null && isGeneratorDrop(cursor)) || (current != null && isGeneratorDrop(current))) {
                event.setCancelled(true);
                sendTradeBlocked(event);
                return;
            }
            
            // Also block clicking result slot if inputs contain generator items
            if (raw == 2) {
                ItemStack input1 = event.getView().getTopInventory().getItem(0);
                ItemStack input2 = event.getView().getTopInventory().getItem(1);
                if ((input1 != null && isGeneratorDrop(input1)) || (input2 != null && isGeneratorDrop(input2))) {
                    event.setCancelled(true);
                    sendTradeBlocked(event);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onMerchantDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getType() != InventoryType.MERCHANT) return;

        if (event.getOldCursor() != null && isGeneratorDrop(event.getOldCursor())) {
            int topSize = event.getView().getTopInventory().getSize();
            for (int slot : event.getRawSlots()) {
                if (slot >= 0 && slot < topSize) {
                    event.setCancelled(true);
                    sendTradeBlocked(event);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCraftItem(CraftItemEvent event) {
        // Prevent crafting when result or any input is a generator drop
        if (event.getInventory() == null) return;

        ItemStack result = event.getInventory().getResult();
        if (result != null && isGeneratorDrop(result)) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
                ((org.bukkit.entity.Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "You cannot craft with generator items!");
            }
            return;
        }

        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item != null && isGeneratorDrop(item)) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
                    ((org.bukkit.entity.Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "You cannot craft with generator items!");
                }
                return;
            }
        }
    }

    private void sendTradeBlocked(org.bukkit.event.inventory.InventoryInteractEvent event) {
        if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
            ((org.bukkit.entity.Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "You cannot trade generator items with villagers!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        // Prevent placing generator items
        ItemStack item = event.getItemInHand();
        // Block placing generator drops, but allow placing generator block items
        if (isGeneratorDrop(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place generator items!");
        }
    }
    
    /**
     * Check if an item is a generator item by checking its NBT data
     */
    public static boolean isGeneratorItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        // Check NBT data for generator item marker
        try {
            NamespacedKey key1 = new NamespacedKey(Bukkit.getPluginManager().getPlugin("SkiesGenerator"), "generator_item");
            NamespacedKey key2 = new NamespacedKey(Bukkit.getPluginManager().getPlugin("SkiesGenerator"), "generator_block");
            NamespacedKey key3 = new NamespacedKey(Bukkit.getPluginManager().getPlugin("SkiesGenerator"), "generator_drop");
            return meta.getPersistentDataContainer().has(key1, PersistentDataType.BYTE)
                    || meta.getPersistentDataContainer().has(key2, PersistentDataType.BYTE)
                    || meta.getPersistentDataContainer().has(key3, PersistentDataType.BYTE);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isGeneratorDrop(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        try {
            NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("SkiesGenerator"), "generator_drop");
            return meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isGeneratorBlockItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        try {
            NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("SkiesGenerator"), "generator_block");
            return meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
        } catch (Exception e) {
            return false;
        }
    }
}
