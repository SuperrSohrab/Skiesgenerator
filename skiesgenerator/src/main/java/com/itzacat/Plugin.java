package com.itzacat;

import com.itzacat.commands.GeneratorCommand;
import com.itzacat.commands.GiveGeneratorCommand;
import com.itzacat.commands.SellGenCommand;
import com.itzacat.config.PricesConfig;
import com.itzacat.generator.GeneratorManager;
import com.itzacat.gui.GeneratorMenuListener;
import com.itzacat.gui.GeneratorInteractionMenuListener;
import com.itzacat.listeners.GeneratorListener;
import com.itzacat.listeners.ItemProtectionListener;
import com.itzacat.storage.GeneratorStorage;
import com.itzacat.tasks.GeneratorTask;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/*
 * skiesgenerator java plugin
 */
public class Plugin extends JavaPlugin {
  private static final Logger LOGGER = Logger.getLogger("skiesgenerator");
  
  private GeneratorManager generatorManager;
  private GeneratorStorage generatorStorage;
  private PricesConfig pricesConfig;
  private Economy economy;
  private GeneratorTask generatorTask;

  @Override
  public void onEnable() {
    // Save default config
    saveDefaultConfig();
    
    // Initialize managers
    generatorManager = new GeneratorManager();
    pricesConfig = new PricesConfig(this);
    generatorStorage = new GeneratorStorage(this);
    
    // Setup Vault economy
    if (getConfig().getBoolean("use-vault", true)) {
      if (!setupEconomy()) {
        LOGGER.warning("Vault not found! Economy features will be disabled.");
      }
    }
    
    // Register commands
    getCommand("sellgen").setExecutor(new SellGenCommand(this));
    getCommand("generator").setExecutor(new GeneratorCommand(this));
    getCommand("givegenerator").setExecutor(new GiveGeneratorCommand(this));
    getCommand("boostgen").setExecutor(new com.itzacat.commands.BoostGenCommand(this));
    
    // Register listeners
    getServer().getPluginManager().registerEvents(new GeneratorListener(this), this);
    getServer().getPluginManager().registerEvents(new ItemProtectionListener(this), this);
    getServer().getPluginManager().registerEvents(new GeneratorMenuListener(this), this);
    getServer().getPluginManager().registerEvents(new GeneratorInteractionMenuListener(this), this);
    
    // Start generator task
    int interval = getConfig().getInt("generation-interval", 100);
    generatorTask = new GeneratorTask(this);
    generatorTask.runTaskTimer(this, interval, interval);
    
    LOGGER.info("SkiesGenerator enabled successfully!");
  }

  @Override
  public void onDisable() {
    // Save generator data
    if (generatorStorage != null) {
      generatorStorage.saveData();
    }
    
    // Cancel generator task
    if (generatorTask != null) {
      generatorTask.cancel();
    }
    
    LOGGER.info("SkiesGenerator disabled");
  }
  
  private boolean setupEconomy() {
    if (getServer().getPluginManager().getPlugin("Vault") == null) {
      return false;
    }
    
    RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
      return false;
    }
    
    economy = rsp.getProvider();
    return economy != null;
  }
  
  public GeneratorManager getGeneratorManager() {
    return generatorManager;
  }
  
  public GeneratorStorage getGeneratorStorage() {
    return generatorStorage;
  }
  
  public PricesConfig getPricesConfig() {
    return pricesConfig;
  }
  
  public Economy getEconomy() {
    return economy;
  }
}