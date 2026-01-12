package com.itzacat.config;

import com.itzacat.Plugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PricesConfig {
    private final Plugin plugin;
    private File pricesFile;
    private FileConfiguration pricesConfig;
    private Map<Material, Double> prices;

    public PricesConfig(Plugin plugin) {
        this.plugin = plugin;
        this.prices = new HashMap<>();
        loadPrices();
    }

    public void loadPrices() {
        if (pricesFile == null) {
            pricesFile = new File(plugin.getDataFolder(), "prices.yml");
        }
        
        if (!pricesFile.exists()) {
            plugin.saveResource("prices.yml", false);
        }
        
        pricesConfig = YamlConfiguration.loadConfiguration(pricesFile);
        
        // Load prices from config
        for (String key : pricesConfig.getKeys(false)) {
            try {
                Material material = Material.valueOf(key.toUpperCase());
                double price = pricesConfig.getDouble(key);
                prices.put(material, price);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in prices.yml: " + key);
            }
        }
    }

    public void savePrices() {
        for (Map.Entry<Material, Double> entry : prices.entrySet()) {
            pricesConfig.set(entry.getKey().name(), entry.getValue());
        }
        
        try {
            pricesConfig.save(pricesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save prices.yml!");
            e.printStackTrace();
        }
    }

    public double getPrice(Material material) {
        return prices.getOrDefault(material, 0.0);
    }

    public void setPrice(Material material, double price) {
        prices.put(material, price);
        savePrices();
    }

    public Map<Material, Double> getAllPrices() {
        return new HashMap<>(prices);
    }
}
