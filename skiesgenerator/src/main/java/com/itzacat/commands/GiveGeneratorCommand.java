package com.itzacat.commands;

import com.itzacat.Plugin;
import com.itzacat.generator.GeneratorTier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class GiveGeneratorCommand implements CommandExecutor {
    private final Plugin plugin;

    public GiveGeneratorCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("skiesgenerator.give")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        // Usage: /givegenerator <player> <tier> [amount]
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /givegenerator <player> <tier> [amount]");
            sender.sendMessage(ChatColor.YELLOW + "Available tiers: " + getAvailableTiers());
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player '" + args[0] + "' is not online!");
            return true;
        }

        GeneratorTier tier;
        try {
            tier = GeneratorTier.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid tier! Available tiers: " + getAvailableTiers());
            return true;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1 || amount > 64) {
                    sender.sendMessage(ChatColor.RED + "Amount must be between 1 and 64!");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount! Must be a number.");
                return true;
            }
        }

        ItemStack generatorItem = createGeneratorItem(tier);
        generatorItem.setAmount(amount);
        target.getInventory().addItem(generatorItem);
        
        target.sendMessage(ChatColor.GREEN + "You received " + amount + "x " + tier.name() + " generator" + (amount > 1 ? "s" : "") + "!");
        sender.sendMessage(ChatColor.GREEN + "Gave " + amount + "x " + tier.name() + " generator" + (amount > 1 ? "s" : "") + " to " + target.getName());

        return true;
    }

    private ItemStack createGeneratorItem(GeneratorTier tier) {
        ItemStack item = new ItemStack(tier.getBlockMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Tier " + tier.getTier() + " Generator");
            List<String> lore = new ArrayList<>();
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

    private String getAvailableTiers() {
        StringBuilder sb = new StringBuilder();
        for (GeneratorTier tier : GeneratorTier.values()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(tier.name());
        }
        return sb.toString();
    }
}
