package com.itzacat.commands;

import com.itzacat.Plugin;
import com.itzacat.generator.Generator;
import com.itzacat.listeners.ItemProtectionListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SellGenCommand implements CommandExecutor {
    private final Plugin plugin;

    public SellGenCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getConfig().getBoolean("use-vault", true)) {
            player.sendMessage(ChatColor.RED + "Economy system is disabled!");
            return true;
        }

        Economy economy = plugin.getEconomy();
        if (economy == null) {
            player.sendMessage(ChatColor.RED + "Economy system not found!");
            return true;
        }

        double totalValue = 0;
        int totalItems = 0;

        // Sell items from all generators owned by the player
        for (Generator generator : plugin.getGeneratorManager().getGeneratorsByOwner(player.getUniqueId())) {
            for (ItemStack item : generator.getCollectedItems()) {
                double price = plugin.getPricesConfig().getPrice(item.getType());
                double value = price * item.getAmount();
                totalValue += value;
                totalItems += item.getAmount();
            }
            generator.clearItems();
        }

        // Sell only generator items in player's inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir() && ItemProtectionListener.isGeneratorDrop(item)) {
                double price = plugin.getPricesConfig().getPrice(item.getType());
                if (price > 0) {
                    double value = price * item.getAmount();
                    totalValue += value;
                    totalItems += item.getAmount();
                    item.setAmount(0); // Remove the item from inventory
                }
            }
        }

        if (totalItems == 0) {
            player.sendMessage(ChatColor.YELLOW + "You have no items to sell!");
            return true;
        }

        economy.depositPlayer(player, totalValue);
        player.sendMessage(ChatColor.GREEN + "Sold " + totalItems + " items for $" + String.format("%.2f", totalValue));

        return true;
    }
}
