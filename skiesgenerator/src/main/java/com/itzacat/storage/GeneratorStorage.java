package com.itzacat.storage;

import com.itzacat.Plugin;
import com.itzacat.generator.Generator;
import com.itzacat.generator.GeneratorTier;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class GeneratorStorage {
    private final Plugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    public GeneratorStorage(Plugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    public void loadData() {
        if (dataFile == null) {
            dataFile = new File(plugin.getDataFolder(), "generators.yml");
        }
        
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create generators.yml!");
                e.printStackTrace();
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        // Load generators from file
        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(dataConfig.getString(key + ".uuid"));
                Location location = (Location) dataConfig.get(key + ".location");
                GeneratorTier tier = GeneratorTier.valueOf(dataConfig.getString(key + ".tier"));
                UUID owner = UUID.fromString(dataConfig.getString(key + ".owner"));
                int maxStorage = dataConfig.getInt(key + ".maxStorage", 64);
                
                Generator generator = new Generator(uuid, location, tier, owner, maxStorage);
                plugin.getGeneratorManager().createGenerator(location, tier, owner, maxStorage);
            } catch (Exception e) {
                plugin.getLogger().warning("Error loading generator: " + key);
            }
        }
    }

    public void saveData() {
        dataConfig = new YamlConfiguration();
        
        int index = 0;
        for (Generator generator : plugin.getGeneratorManager().getAllGenerators()) {
            String path = "generator-" + index;
            dataConfig.set(path + ".uuid", generator.getUuid().toString());
            dataConfig.set(path + ".location", generator.getLocation());
            dataConfig.set(path + ".tier", generator.getTier().name());
            dataConfig.set(path + ".owner", generator.getOwner().toString());
            dataConfig.set(path + ".maxStorage", generator.getMaxStorage());
            index++;
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save generators.yml!");
            e.printStackTrace();
        }
    }
}
