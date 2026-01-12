package com.itzacat.commands;

import com.itzacat.Plugin;
import com.itzacat.generator.Generator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class BoostGenCommand implements CommandExecutor {
    private final Plugin plugin;

    public BoostGenCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("skiesgenerator.boost")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /boostgen <player> <multiplier> <minutes>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found or not online.");
            return true;
        }

        int multiplier;
        int minutes;
        try {
            multiplier = Integer.parseInt(args[1]);
            minutes = Integer.parseInt(args[2]);
            if (multiplier < 1 || minutes < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Multiplier and minutes must be positive integers (minutes can be 0 for instant/no duration).");
            return true;
        }

        // Apply booster to all generators owned by target
        List<Generator> gens = plugin.getGeneratorManager().getGeneratorsByOwner(target.getUniqueId());
        if (gens.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Player has no generators to boost.");
            return true;
        }

        for (Generator g : gens) {
            g.setBooster(multiplier, minutes);
        }

        sender.sendMessage(ChatColor.GREEN + "Applied x" + multiplier + " booster for " + minutes + " minute(s) to " + target.getName() + "'s generators.");
        target.sendMessage(ChatColor.AQUA + "Your generators received a x" + multiplier + " booster for " + minutes + " minute(s)! ");

        return true;
    }
}
