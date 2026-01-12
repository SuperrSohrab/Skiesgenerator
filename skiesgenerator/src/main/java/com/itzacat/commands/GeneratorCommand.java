package com.itzacat.commands;

import com.itzacat.Plugin;
import com.itzacat.generator.Generator;
import com.itzacat.generator.GeneratorTier;
import com.itzacat.gui.GeneratorMenu;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GeneratorCommand implements CommandExecutor {
    private final Plugin plugin;

    public GeneratorCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /generator <give|upgrade|info>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                return handleGive(player, args);
            case "upgrade":
                return handleUpgrade(player);
            case "info":
                return handleInfo(player);
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /generator <give|upgrade|info>");
                return true;
        }
    }

    private boolean handleGive(Player player, String[] args) {
        if (!player.hasPermission("skiesgenerator.give")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /generator give <tier>");
            player.sendMessage(ChatColor.YELLOW + "Available tiers: " + getAvailableTiers());
            return true;
        }

        GeneratorTier tier;
        try {
            tier = GeneratorTier.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid tier! Available tiers: " + getAvailableTiers());
            return true;
        }

        ItemStack generatorItem = createGeneratorItem(tier);
        player.getInventory().addItem(generatorItem);
        player.sendMessage(ChatColor.GREEN + "Gave you a " + tier.name() + " generator!");

        return true;
    }

    private boolean handleUpgrade(Player player) {
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must be looking at a generator block!");
            return true;
        }

        Generator generator = plugin.getGeneratorManager().getGenerator(targetBlock);
        if (generator == null) {
            player.sendMessage(ChatColor.RED + "That's not a generator!");
            return true;
        }

        if (!generator.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You don't own this generator!");
            return true;
        }

        GeneratorTier nextTier = generator.getTier().getNextTier();
        if (nextTier == null) {
            player.sendMessage(ChatColor.RED + "This generator is already at maximum tier!");
            return true;
        }

        if (!plugin.getConfig().getBoolean("use-vault", true)) {
            player.sendMessage(ChatColor.RED + "Economy system is disabled!");
            return true;
        }

        Economy economy = plugin.getEconomy();
        if (economy == null) {
            player.sendMessage(ChatColor.RED + "Economy system not found!");
            return true;
        }

        double cost = nextTier.getUpgradeCost();
        if (economy.getBalance(player) < cost) {
            player.sendMessage(ChatColor.RED + "You need $" + String.format("%.2f", cost) + " to upgrade this generator!");
            return true;
        }

        economy.withdrawPlayer(player, cost);
        generator.setTier(nextTier);
        targetBlock.setType(nextTier.getBlockMaterial());
        player.sendMessage(ChatColor.GREEN + "Generator upgraded to " + nextTier.name() + " for $" + String.format("%.2f", cost));

        return true;
    }

    private boolean handleInfo(Player player) {
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must be looking at a generator block!");
            return true;
        }

        Generator generator = plugin.getGeneratorManager().getGenerator(targetBlock);
        if (generator == null) {
            player.sendMessage(ChatColor.RED + "That's not a generator!");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "=== Generator Info ===");
        player.sendMessage(ChatColor.YELLOW + "Tier: " + ChatColor.WHITE + generator.getTier().name());
        player.sendMessage(ChatColor.YELLOW + "Produces: " + ChatColor.WHITE + generator.getTier().getDropMaterial().name());
        player.sendMessage(ChatColor.YELLOW + "Storage: " + ChatColor.WHITE + generator.getTotalItems() + "/" + generator.getMaxStorage());
        
        GeneratorTier nextTier = generator.getTier().getNextTier();
        if (nextTier != null) {
            player.sendMessage(ChatColor.YELLOW + "Upgrade cost: " + ChatColor.WHITE + "$" + String.format("%.2f", nextTier.getUpgradeCost()));
        } else {
            player.sendMessage(ChatColor.GREEN + "Maximum tier reached!");
        }

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
